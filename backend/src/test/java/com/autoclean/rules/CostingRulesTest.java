package com.autoclean.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.autoclean.rules.spec.CostingSpec;
import com.autoclean.rules.spec.CostingSpec.CostEstimateSpec;
import com.autoclean.rules.spec.CostingSpec.CostLineSpec;
import com.autoclean.rules.spec.CostingSpec.ServiceTypeSpec;
import com.autoclean.rules.spec.LegacyMirrorCostingSpec;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Especificación de las reglas del motor de costeo (AGENTS.md §8 / CostingServiceImpl).
 */
class CostingRulesTest {

    private CostingSpec costingSpec;

    @BeforeEach
    void setUp() {
        costingSpec = new LegacyMirrorCostingSpec();
        costingSpec.registerServiceType(new ServiceTypeSpec("srv-1", "Porcelanizado", 150000.0));
    }

    @Test
    @DisplayName("estimatedCost debe ser la suma de montos y difference = price - estimatedCost")
    void shouldSumCostLinesAndCalculatePositiveDifference() {
        List<CostLineSpec> lines = List.of(
                new CostLineSpec("Sellador cerámico", 45000.0),
                new CostLineSpec("Mano de obra", 35000.0));

        CostEstimateSpec result = costingSpec.estimateCost("srv-1", "app-1", lines);

        assertNotNull(result.id());
        assertEquals("app-1", result.appointmentId());
        assertEquals(80000.0, result.estimatedCost(), 0.001);
        assertEquals(150000.0, result.price(), 0.001);
        assertEquals(70000.0, result.difference(), 0.001);
    }

    @Test
    @DisplayName("difference puede ser negativo cuando estimatedCost supera a price y appointmentId es opcional")
    void shouldAllowNegativeDifferenceAndOptionalAppointment() {
        List<CostLineSpec> lines = List.of(
                new CostLineSpec("Insumo importado", 120000.0),
                new CostLineSpec("Horas extra", 60000.0));

        CostEstimateSpec result = costingSpec.estimateCost("srv-1", null, lines);

        assertNull(result.appointmentId());
        assertEquals(180000.0, result.estimatedCost(), 0.001);
        assertEquals(-30000.0, result.difference(), 0.001);
    }

    @Test
    @DisplayName("Precio negativo en ServiceType debe lanzar IllegalArgumentException")
    void shouldRejectNegativeServiceTypePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new ServiceTypeSpec("srv-bad", "Lavado inválido", -1000.0));
    }

    @Test
    @DisplayName("Monto negativo en CostLine debe lanzar IllegalArgumentException")
    void shouldRejectNegativeCostLineAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> new CostLineSpec("Insumo negativo", -500.0));
    }

    @Test
    @DisplayName("Servicio inexistente al costear debe lanzar IllegalArgumentException")
    void shouldRejectUnknownServiceTypeOnEstimate() {
        assertThrows(IllegalArgumentException.class,
                () -> costingSpec.estimateCost("srv-inexistente", null, List.of()));
    }
}
