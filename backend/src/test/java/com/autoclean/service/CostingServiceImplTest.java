package com.autoclean.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.autoclean.dto.costing.CostEstimateRequest;
import com.autoclean.dto.costing.CostEstimateResponse;
import com.autoclean.dto.costing.CostLineRequest;
import com.autoclean.model.Appointment;
import com.autoclean.model.CostEstimate;
import com.autoclean.model.CostLine;
import com.autoclean.model.ServiceType;
import com.autoclean.repository.CostEstimateRepository;
import com.autoclean.repository.ServiceTypeRepository;
import com.autoclean.service.impl.CostingServiceImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias del motor de costeo ({@link CostingServiceImpl}).
 */
@ExtendWith(MockitoExtension.class)
class CostingServiceImplTest {

    @Mock
    private CostEstimateRepository costEstimateRepository;

    @Mock
    private ServiceTypeRepository serviceTypeRepository;

    @Mock
    private EntityManagerFactory entityManagerFactory;

    private CostingServiceImpl costingService;

    private UUID serviceTypeId;
    private ServiceType serviceType;

    @BeforeEach
    void setUp() {
        costingService = new CostingServiceImpl(costEstimateRepository, serviceTypeRepository, entityManagerFactory);
        serviceTypeId = UUID.randomUUID();
        serviceType = new ServiceType("Porcelanizado", "Detailing completo", new BigDecimal("150000.00"), 120);
        serviceType.setId(serviceTypeId);
    }

    @Test
    @DisplayName("Suma líneas de costo y calcula difference = price - estimatedCost")
    void shouldSumCostLinesAndCalculatePositiveDifference() {
        UUID generatedEstimateId = UUID.randomUUID();
        when(serviceTypeRepository.findById(serviceTypeId)).thenReturn(Optional.of(serviceType));
        when(costEstimateRepository.save(any(CostEstimate.class))).thenAnswer(invocation -> {
            CostEstimate estimate = invocation.getArgument(0);
            estimate.setId(generatedEstimateId);
            return estimate;
        });

        CostEstimateRequest request = new CostEstimateRequest(
                serviceTypeId,
                null,
                List.of(
                        new CostLineRequest("Sellador cerámico", new BigDecimal("45000.00")),
                        new CostLineRequest("Mano de obra", new BigDecimal("35000.00"))
                )
        );

        CostEstimateResponse response = costingService.estimateCost(request);

        assertNotNull(response);
        assertEquals(generatedEstimateId, response.id());
        assertEquals(serviceTypeId, response.serviceTypeId());
        assertNull(response.appointmentId());
        assertEquals(new BigDecimal("80000.00"), response.estimatedCost());
        assertEquals(new BigDecimal("150000.00"), response.price());
        assertEquals(new BigDecimal("70000.00"), response.difference());
        assertEquals(2, response.lines().size());
        verify(costEstimateRepository).save(any(CostEstimate.class));
    }

    @Test
    @DisplayName("Permite difference negativo cuando el costo estimado supera el precio del servicio")
    void shouldAllowNegativeDifferenceWhenEstimatedCostExceedsPrice() {
        when(serviceTypeRepository.findById(serviceTypeId)).thenReturn(Optional.of(serviceType));
        when(costEstimateRepository.save(any(CostEstimate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<CostLine> lines = List.of(
                new CostLine("Insumo importado", new BigDecimal("120000.00")),
                new CostLine("Horas extra", new BigDecimal("60000.00"))
        );

        CostEstimateResponse response = costingService.estimateCost(serviceTypeId, null, lines);

        assertNull(response.appointmentId());
        assertEquals(new BigDecimal("180000.00"), response.estimatedCost());
        assertEquals(new BigDecimal("150000.00"), response.price());
        assertEquals(new BigDecimal("-30000.00"), response.difference());
    }

    @Test
    @DisplayName("Asocia la cita opcional cuando appointmentId existe en persistencia")
    void shouldAssociateOptionalAppointmentWhenFound() {
        UUID appointmentId = UUID.randomUUID();
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);

        EntityManager entityManager = mock(EntityManager.class);
        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
        when(entityManager.find(Appointment.class, appointmentId)).thenReturn(appointment);
        when(serviceTypeRepository.findById(serviceTypeId)).thenReturn(Optional.of(serviceType));
        when(costEstimateRepository.save(any(CostEstimate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CostEstimateResponse response = costingService.estimateCost(
                new CostEstimateRequest(serviceTypeId, appointmentId, List.of(new CostLineRequest("Shampoo", new BigDecimal("10000.00"))))
        );

        assertEquals(appointmentId, response.appointmentId());
        assertEquals(new BigDecimal("10000.00"), response.estimatedCost());
        assertEquals(new BigDecimal("140000.00"), response.difference());
    }

    @Test
    @DisplayName("Calcula costo estimado cero cuando no se envían líneas")
    void shouldHandleEmptyLinesAsZeroEstimatedCost() {
        when(serviceTypeRepository.findById(serviceTypeId)).thenReturn(Optional.of(serviceType));
        when(costEstimateRepository.save(any(CostEstimate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CostEstimateResponse response = costingService.estimateCost(serviceTypeId, null, null);

        assertEquals(BigDecimal.ZERO, response.estimatedCost());
        assertEquals(new BigDecimal("150000.00"), response.difference());
        assertEquals(0, response.lines().size());
    }

    @Test
    @DisplayName("Rechaza montos negativos en la solicitud de líneas de costo")
    void shouldRejectNegativeAmountInRequest() {
        CostEstimateRequest request = new CostEstimateRequest(
                serviceTypeId,
                null,
                List.of(new CostLineRequest("Insumo inválido", new BigDecimal("-500.00")))
        );

        assertThrows(IllegalArgumentException.class, () -> costingService.estimateCost(request));
        verify(costEstimateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rechaza líneas de dominio con monto negativo")
    void shouldRejectNegativeAmountInDomainCostLine() {
        when(serviceTypeRepository.findById(serviceTypeId)).thenReturn(Optional.of(serviceType));
        CostLine invalidLine = mock(CostLine.class);
        when(invalidLine.getAmount()).thenReturn(new BigDecimal("-1.00"));

        assertThrows(
                IllegalArgumentException.class,
                () -> costingService.estimateCost(serviceTypeId, null, Collections.singletonList(invalidLine))
        );
        verify(costEstimateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rechaza un tipo de servicio inexistente con IllegalArgumentException")
    void shouldRejectUnknownServiceType() {
        when(serviceTypeRepository.findById(serviceTypeId)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> costingService.estimateCost(serviceTypeId, null, List.of())
        );
        verify(costEstimateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rechaza una cita inexistente cuando se especifica appointmentId")
    void shouldRejectUnknownAppointmentId() {
        UUID missingAppointmentId = UUID.randomUUID();
        EntityManager entityManager = mock(EntityManager.class);
        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
        when(entityManager.find(Appointment.class, missingAppointmentId)).thenReturn(null);
        when(serviceTypeRepository.findById(serviceTypeId)).thenReturn(Optional.of(serviceType));

        assertThrows(
                IllegalArgumentException.class,
                () -> costingService.estimateCost(serviceTypeId, missingAppointmentId, List.of())
        );
        verify(costEstimateRepository, never()).save(any());
    }
}

