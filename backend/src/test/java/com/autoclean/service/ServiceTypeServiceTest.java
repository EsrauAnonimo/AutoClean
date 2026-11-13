package com.autoclean.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.autoclean.dto.servicetype.ServiceTypeRequestDto;
import com.autoclean.dto.servicetype.ServiceTypeResponseDto;
import com.autoclean.exception.ResourceNotFoundException;
import com.autoclean.model.ServiceType;
import com.autoclean.repository.ServiceTypeRepository;
import com.autoclean.service.impl.ServiceTypeServiceImpl;
import java.math.BigDecimal;
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
 * Pruebas unitarias para la lógica de negocio de ServiceTypeService.
 */
@ExtendWith(MockitoExtension.class)
class ServiceTypeServiceTest {

    @Mock
    private ServiceTypeRepository serviceTypeRepository;

    private ServiceTypeService serviceTypeService;

    @BeforeEach
    void setUp() {
        serviceTypeService = new ServiceTypeServiceImpl(serviceTypeRepository);
    }

    @Test
    @DisplayName("Debe crear un tipo de servicio exitosamente")
    void shouldCreateServiceTypeSuccessfully() {
        UUID generatedId = UUID.randomUUID();
        ServiceTypeRequestDto request = new ServiceTypeRequestDto(
                "Lavado General",
                "Lavado exterior e interior",
                new BigDecimal("35000.00"),
                45
        );

        when(serviceTypeRepository.save(any(ServiceType.class))).thenAnswer(invocation -> {
            ServiceType st = invocation.getArgument(0);
            st.setId(generatedId);
            return st;
        });

        ServiceTypeResponseDto response = serviceTypeService.create(request);

        assertNotNull(response);
        assertEquals(generatedId, response.id());
        assertEquals("Lavado General", response.name());
        assertEquals(new BigDecimal("35000.00"), response.price());
        assertEquals(45, response.durationMin());
        verify(serviceTypeRepository).save(any(ServiceType.class));
    }

    @Test
    @DisplayName("Debe rechazar nombre vacío al crear tipo de servicio")
    void shouldRejectBlankNameOnCreate() {
        ServiceTypeRequestDto request = new ServiceTypeRequestDto(
                "   ",
                "Descripción",
                new BigDecimal("20000.00"),
                30
        );

        assertThrows(IllegalArgumentException.class, () -> serviceTypeService.create(request));
        verify(serviceTypeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe rechazar precio negativo al crear tipo de servicio")
    void shouldRejectNegativePriceOnCreate() {
        ServiceTypeRequestDto request = new ServiceTypeRequestDto(
                "Polichado",
                "Descripción",
                new BigDecimal("-5000.00"),
                60
        );

        assertThrows(IllegalArgumentException.class, () -> serviceTypeService.create(request));
        verify(serviceTypeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe actualizar un tipo de servicio existente")
    void shouldUpdateServiceTypeSuccessfully() {
        UUID id = UUID.randomUUID();
        ServiceType existing = new ServiceType(id, "Lavado Simple", "Viejo", new BigDecimal("25000.00"), 30);
        ServiceTypeRequestDto updateRequest = new ServiceTypeRequestDto(
                "Lavado Premium",
                "Nuevo detalle",
                new BigDecimal("40000.00"),
                50
        );

        when(serviceTypeRepository.findById(id)).thenReturn(Optional.of(existing));
        when(serviceTypeRepository.save(any(ServiceType.class))).thenAnswer(inv -> inv.getArgument(0));

        ServiceTypeResponseDto updated = serviceTypeService.update(id, updateRequest);

        assertEquals("Lavado Premium", updated.name());
        assertEquals("Nuevo detalle", updated.description());
        assertEquals(new BigDecimal("40000.00"), updated.price());
        assertEquals(50, updated.durationMin());
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException al actualizar un servicio inexistente")
    void shouldThrowWhenUpdatingNonExistentServiceType() {
        UUID nonExistentId = UUID.randomUUID();
        ServiceTypeRequestDto request = new ServiceTypeRequestDto(
                "Servicio X",
                null,
                new BigDecimal("10000.00"),
                20
        );

        when(serviceTypeRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> serviceTypeService.update(nonExistentId, request));
    }

    @Test
    @DisplayName("Debe eliminar un tipo de servicio existente")
    void shouldDeleteServiceTypeSuccessfully() {
        UUID id = UUID.randomUUID();
        ServiceType existing = new ServiceType(id, "Desinfección", "Ozono", new BigDecimal("50000.00"), 30);

        when(serviceTypeRepository.findById(id)).thenReturn(Optional.of(existing));

        serviceTypeService.delete(id);

        verify(serviceTypeRepository).delete(existing);
    }

    @Test
    @DisplayName("Debe buscar un tipo de servicio por ID")
    void shouldFindServiceTypeById() {
        UUID id = UUID.randomUUID();
        ServiceType existing = new ServiceType(id, "Porcelanizado", "Brillo espejo", new BigDecimal("180000.00"), 120);

        when(serviceTypeRepository.findById(id)).thenReturn(Optional.of(existing));

        ServiceTypeResponseDto found = serviceTypeService.findById(id);

        assertNotNull(found);
        assertEquals(id, found.id());
        assertEquals("Porcelanizado", found.name());
    }

    @Test
    @DisplayName("Debe listar todos los tipos de servicio")
    void shouldFindAllServiceTypes() {
        List<ServiceType> list = List.of(
                new ServiceType(UUID.randomUUID(), "S1", "D1", new BigDecimal("10000.00"), 30),
                new ServiceType(UUID.randomUUID(), "S2", "D2", new BigDecimal("20000.00"), 45)
        );

        when(serviceTypeRepository.findAll()).thenReturn(list);

        List<ServiceTypeResponseDto> all = serviceTypeService.findAll();

        assertEquals(2, all.size());
    }
}
