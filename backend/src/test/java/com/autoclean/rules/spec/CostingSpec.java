package com.autoclean.rules.spec;

import java.util.List;

/**
 * Contrato espejo del motor de costeo directo del sistema legacy.
 */
public interface CostingSpec {

    /**
     * Representa un tipo de servicio en catálogo con precio validado.
     */
    record ServiceTypeSpec(String id, String name, double price) {
        public ServiceTypeSpec {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre del servicio no puede estar vacío");
            }
            if (price < 0) {
                throw new IllegalArgumentException("El precio del servicio no puede ser negativo");
            }
        }
    }

    /**
     * Representa una línea de costo directo con monto no negativo.
     */
    record CostLineSpec(String concept, double amount) {
        public CostLineSpec {
            if (concept == null || concept.trim().isEmpty()) {
                throw new IllegalArgumentException("El concepto no puede estar vacío");
            }
            if (amount < 0) {
                throw new IllegalArgumentException("El monto no puede ser negativo");
            }
        }
    }

    /**
     * Resultado del cálculo de costeo directo.
     */
    record CostEstimateSpec(
            String id,
            String serviceTypeId,
            String appointmentId,
            List<CostLineSpec> lines,
            double estimatedCost,
            double price,
            double difference) {
    }

    /**
     * Registra un tipo de servicio en el catálogo de prueba.
     *
     * @param serviceType tipo de servicio válido
     */
    void registerServiceType(ServiceTypeSpec serviceType);

    /**
     * Calcula y almacena la estimación de costos para un servicio y turno opcional.
     *
     * @param serviceTypeId identificador del servicio
     * @param appointmentId identificador de cita opcional
     * @param lines líneas de costo directo
     * @return estimación calculada con UUID
     */
    CostEstimateSpec estimateCost(String serviceTypeId, String appointmentId, List<CostLineSpec> lines);
}
