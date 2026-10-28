package com.autoclean;

import com.autoclean.contracts.CostingService;
import com.autoclean.model.CostEstimate;
import com.autoclean.model.CostLine;
import com.autoclean.model.ServiceType;
import com.autoclean.repository.CostEstimateRepository;
import com.autoclean.repository.ServiceTypeRepository;
import com.autoclean.service.impl.CostingServiceImpl;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el motor de costeo (CostingServiceImpl).
 */
public class CostingServiceImplTest {

    private CostingService costingService;
    private ServiceTypeRepository serviceTypeRepository;
    private CostEstimateRepository costEstimateRepository;

    @BeforeEach
    void setUp() {
        serviceTypeRepository = new ServiceTypeRepository();
        costEstimateRepository = new CostEstimateRepository();
        costingService = new CostingServiceImpl(costEstimateRepository, serviceTypeRepository);
    }

    @Test
    @DisplayName("Debe estimar costo con dos líneas y verificar estimatedCost, price y difference")
    void testEstimateCostWithTwoLines() {
        // Registrar tipo de servicio con precio base de 80000.0
        ServiceType serviceType = new ServiceType("srv-lavado", "Lavado Especial y Encerado", "Lavado detallado", 80000.0);
        serviceTypeRepository.save(serviceType);

        // Crear dos líneas de costo directo: insumos y mano de obra
        CostLine line1 = new CostLine("Champú con cera carnauba", 15000.0);
        CostLine line2 = new CostLine("Horas operario especialista", 25000.0);
        List<CostLine> lines = Arrays.asList(line1, line2);

        // Ejecutar estimación
        CostEstimate estimate = costingService.estimateCost("srv-lavado", lines);

        // Validaciones numéricas con delta de tolerancia
        assertNotNull(estimate);
        assertNotNull(estimate.getId());
        assertEquals("srv-lavado", estimate.getServiceTypeId());
        assertEquals(40000.0, estimate.getEstimatedCost(), 0.001); // 15000 + 25000
        assertEquals(80000.0, estimate.getPrice(), 0.001);
        assertEquals(40000.0, estimate.getDifference(), 0.001);   // 80000 - 40000

        // Verificar persistencia en el repositorio de estimaciones
        CostEstimate saved = costingService.findById(estimate.getId());
        assertNotNull(saved);
        assertEquals(estimate.getId(), saved.getId());
    }

    @Test
    @DisplayName("Debe estimar costo vinculado a un turno específico")
    void testEstimateCostWithAppointmentId() {
        ServiceType serviceType = new ServiceType("srv-pulido", "Porcelanizado", "Tratamiento cerámico", 250000.0);
        serviceTypeRepository.save(serviceType);

        CostLine line1 = new CostLine("Compuesto pulidor", 60000.0);
        CostLine line2 = new CostLine("Sellador cerámico", 40000.0);

        CostEstimate estimate = costingService.estimateCost("srv-pulido", "apt-999", Arrays.asList(line1, line2));

        assertNotNull(estimate);
        assertEquals("apt-999", estimate.getAppointmentId());
        assertEquals(100000.0, estimate.getEstimatedCost(), 0.001);
        assertEquals(250000.0, estimate.getPrice(), 0.001);
        assertEquals(150000.0, estimate.getDifference(), 0.001);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el tipo de servicio no existe")
    void testEstimateCostWithInvalidServiceThrowsException() {
        List<CostLine> lines = List.of(new CostLine("Insumo", 10000.0));
        assertThrows(IllegalArgumentException.class, () -> costingService.estimateCost("srv-inexistente", lines));
    }
}
