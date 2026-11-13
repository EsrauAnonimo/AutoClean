package com.autoclean.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.autoclean.dto.supply.SupplyRequest;
import com.autoclean.dto.supply.SupplyResponse;
import com.autoclean.model.Supply;
import com.autoclean.repository.SupplyRepository;
import com.autoclean.service.impl.SupplyServiceImpl;
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
 * Pruebas unitarias para la lógica CRUD y validaciones de inventario de {@link SupplyServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class SupplyServiceImplTest {

    @Mock
    private SupplyRepository supplyRepository;

    private SupplyServiceImpl supplyService;

    @BeforeEach
    void setUp() {
        supplyService = new SupplyServiceImpl(supplyRepository);
    }

    @Test
    @DisplayName("Crea un insumo válido correctamente")
    void shouldCreateSupplySuccessfully() {
        UUID generatedId = UUID.randomUUID();
        when(supplyRepository.save(any(Supply.class))).thenAnswer(invocation -> {
            Supply saved = invocation.getArgument(0);
            saved.setId(generatedId);
            return saved;
        });

        SupplyResponse response = supplyService.create(new SupplyRequest("  Shampoo Neutro  ", 25, "  litros "));

        assertNotNull(response);
        assertEquals(generatedId, response.id());
        assertEquals("Shampoo Neutro", response.name());
        assertEquals(25, response.stock());
        assertEquals("litros", response.unit());
        verify(supplyRepository).save(any(Supply.class));
    }

    @Test
    @DisplayName("Permite stock igual a cero (sin inventario disponible)")
    void shouldAllowZeroStock() {
        when(supplyRepository.save(any(Supply.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SupplyResponse response = supplyService.create(new SupplyRequest("Cera Carnauba", 0, "unidades"));

        assertEquals(0, response.stock());
        verify(supplyRepository).save(any(Supply.class));
    }

    @Test
    @DisplayName("Rechaza stock negativo al crear o actualizar")
    void shouldRejectNegativeStock() {
        SupplyRequest invalidRequest = new SupplyRequest("Microfibra", -1, "unidades");

        assertThrows(IllegalArgumentException.class, () -> supplyService.create(invalidRequest));
        verify(supplyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rechaza nombre o unidad vacíos")
    void shouldRejectBlankNameOrUnit() {
        assertThrows(IllegalArgumentException.class,
                () -> supplyService.create(new SupplyRequest("   ", 5, "unidades")));
        assertThrows(IllegalArgumentException.class,
                () -> supplyService.create(new SupplyRequest("Desengrasante", 5, "  ")));
        verify(supplyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Actualiza un insumo existente")
    void shouldUpdateExistingSupply() {
        UUID id = UUID.randomUUID();
        Supply existing = new Supply("Cera", 10, "litros");
        existing.setId(id);

        when(supplyRepository.findById(id)).thenReturn(Optional.of(existing));
        when(supplyRepository.save(any(Supply.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SupplyResponse updated = supplyService.update(id, new SupplyRequest("Cera Cerámica", 15, "botellas"));

        assertEquals(id, updated.id());
        assertEquals("Cera Cerámica", updated.name());
        assertEquals(15, updated.stock());
        assertEquals("botellas", updated.unit());
    }

    @Test
    @DisplayName("Rechaza actualización si el insumo no existe")
    void shouldRejectUpdateWhenSupplyMissing() {
        UUID missingId = UUID.randomUUID();
        when(supplyRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> supplyService.update(missingId, new SupplyRequest("Cera", 5, "unidades")));
        verify(supplyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Elimina un insumo existente y rechaza eliminación de id inexistente")
    void shouldDeleteExistingAndRejectMissingSupply() {
        UUID existingId = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();

        when(supplyRepository.existsById(existingId)).thenReturn(true);
        when(supplyRepository.existsById(missingId)).thenReturn(false);

        supplyService.delete(existingId);
        verify(supplyRepository).deleteById(existingId);

        assertThrows(IllegalArgumentException.class, () -> supplyService.delete(missingId));
        verify(supplyRepository, never()).deleteById(missingId);
    }

    @Test
    @DisplayName("Consulta insumo por id y lista ordenada")
    void shouldFindByIdAndFindAllOrdered() {
        UUID id = UUID.randomUUID();
        Supply s1 = new Supply("Abrillantador", 8, "litros");
        s1.setId(id);
        Supply s2 = new Supply("Paño", 30, "unidades");
        s2.setId(UUID.randomUUID());

        when(supplyRepository.findById(id)).thenReturn(Optional.of(s1));
        when(supplyRepository.findAllByOrderByNameAsc()).thenReturn(List.of(s1, s2));

        assertEquals("Abrillantador", supplyService.findById(id).name());
        assertEquals(2, supplyService.findAll().size());
    }
}

