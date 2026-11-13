package com.autoclean.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.autoclean.dto.report.ReportResult;
import com.autoclean.model.Appointment;
import com.autoclean.model.AppointmentStatus;
import com.autoclean.model.CostEstimate;
import com.autoclean.model.CostLine;
import com.autoclean.model.Customer;
import com.autoclean.model.Employee;
import com.autoclean.model.ServiceType;
import com.autoclean.model.Vehicle;
import com.autoclean.repository.CostEstimateRepository;
import com.autoclean.service.impl.ReportServiceImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias de {@link ReportServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private CostEstimateRepository costEstimateRepository;

    @Mock
    private EntityManagerFactory entityManagerFactory;

    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(costEstimateRepository, entityManagerFactory);
    }

    @Test
    @DisplayName("servicesByPeriod retorna filas dentro del rango y excluye fechas fuera de periodo")
    void shouldReturnServicesWithinDateRange() {
        Appointment inRange = buildAppointment(
                UUID.randomUUID(),
                "Carlos Pérez",
                "Lavado Premium",
                new BigDecimal("60000.00"),
                AppointmentStatus.FINISHED,
                OffsetDateTime.of(2026, 10, 15, 10, 0, 0, 0, ZoneOffset.UTC)
        );
        Appointment outOfRange = buildAppointment(
                UUID.randomUUID(),
                "Ana Gómez",
                "Polichado",
                new BigDecimal("120000.00"),
                AppointmentStatus.PENDING,
                OffsetDateTime.of(2026, 11, 2, 10, 0, 0, 0, ZoneOffset.UTC)
        );
        mockAppointmentsQuery(List.of(inRange, outOfRange));

        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);
        ReportResult result = reportService.servicesByPeriod(from, to);

        assertEquals("Servicios por periodo (2026-10-01 al 2026-10-31)", result.title());
        assertEquals(1, result.rows().size());
        assertEquals(inRange.getId().toString(), result.rows().get(0)[0]);
        assertEquals("Carlos Pérez", result.rows().get(0)[1]);
        assertEquals("Lavado Premium", result.rows().get(0)[2]);
        assertEquals("FINISHED", result.rows().get(0)[3]);
    }

    @Test
    @DisplayName("revenueVsCost cruza citas del periodo con estimaciones por appointmentId y por serviceTypeId")
    void shouldReturnRevenueVsCostCrossingWithEstimates() {
        UUID appointmentId = UUID.randomUUID();
        Appointment appointment = buildAppointment(
                appointmentId,
                "Carlos Pérez",
                "Porcelanizado",
                new BigDecimal("150000.00"),
                AppointmentStatus.FINISHED,
                OffsetDateTime.of(2026, 10, 15, 14, 0, 0, 0, ZoneOffset.UTC)
        );
        mockAppointmentsQuery(List.of(appointment));

        CostEstimate estimate = new CostEstimate(
                appointment.getServiceType(),
                appointment,
                List.of(new CostLine("Sellador", new BigDecimal("50000.00"))),
                new BigDecimal("50000.00"),
                new BigDecimal("150000.00"),
                new BigDecimal("100000.00")
        );
        when(costEstimateRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(estimate));

        ReportResult result = reportService.revenueVsCost(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));

        assertEquals("Ingresos vs Costos (2026-10-01 al 2026-10-31)", result.title());
        assertEquals(1, result.rows().size());
        assertEquals("Porcelanizado", result.rows().get(0)[0]);
        assertEquals("150000.00", result.rows().get(0)[1]);
        assertEquals("50000.00", result.rows().get(0)[2]);
        assertEquals("100000.00", result.rows().get(0)[3]);
    }

    @Test
    @DisplayName("Cuando no hay registros en el periodo el título es exactamente 'Sin datos en el periodo'")
    void shouldReturnExactNoDataTitleWhenPeriodIsEmpty() {
        mockAppointmentsQuery(List.of());

        ReportResult servicesEmpty = reportService.servicesByPeriod(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
        ReportResult revenueEmpty = reportService.revenueVsCost(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

        assertEquals("Sin datos en el periodo", servicesEmpty.title());
        assertTrue(servicesEmpty.rows().isEmpty());
        assertEquals("Sin datos en el periodo", revenueEmpty.title());
        assertTrue(revenueEmpty.rows().isEmpty());
    }

    @Test
    @DisplayName("Cuando las fechas están invertidas (from > to) lanza IllegalArgumentException")
    void shouldRejectInvertedDateRange() {
        LocalDate from = LocalDate.of(2026, 10, 31);
        LocalDate to = LocalDate.of(2026, 10, 1);

        assertThrows(IllegalArgumentException.class, () -> reportService.validateDateRange(from, to));
        assertThrows(IllegalArgumentException.class, () -> reportService.servicesByPeriod(from, to));
        assertThrows(IllegalArgumentException.class, () -> reportService.revenueVsCost(from, to));
    }

    @Test
    @DisplayName("Fechas nulas lanzan IllegalArgumentException")
    void shouldRejectNullDates() {
        LocalDate valid = LocalDate.of(2026, 10, 1);

        assertThrows(IllegalArgumentException.class, () -> reportService.validateDateRange(null, valid));
        assertThrows(IllegalArgumentException.class, () -> reportService.servicesByPeriod(valid, null));
        assertThrows(IllegalArgumentException.class, () -> reportService.revenueVsCost(null, null));
    }

    @SuppressWarnings("unchecked")
    private void mockAppointmentsQuery(List<Appointment> result) {
        EntityManager entityManager = mock(EntityManager.class);
        TypedQuery<Appointment> query = mock(TypedQuery.class);
        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
        when(entityManager.createQuery(anyString(), eq(Appointment.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(result);
    }

    private Appointment buildAppointment(
            UUID id,
            String customerName,
            String serviceName,
            BigDecimal servicePrice,
            AppointmentStatus status,
            OffsetDateTime dateTime) {
        Customer customer = new Customer(customerName, "3001112233", "cliente@autoclean.com");
        customer.setId(UUID.randomUUID());

        ServiceType serviceType = new ServiceType(serviceName, "Descripción", servicePrice, 60);
        serviceType.setId(UUID.randomUUID());

        Vehicle vehicle = new Vehicle(customer, "ABC123", "Mazda", "3", 2022);
        Employee employee = new Employee("Luis Operario", "Detailing");

        Appointment appointment = new Appointment(customer, vehicle, employee, serviceType, dateTime, status);
        appointment.setId(id);
        return appointment;
    }
}
