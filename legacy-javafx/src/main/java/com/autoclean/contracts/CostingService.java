package com.autoclean.contracts;

import com.autoclean.model.CostEstimate;
import com.autoclean.model.CostLine;
import java.util.List;

/**
 * Contrato de servicio para el costeo de servicios y análisis de margen.
 */
public interface CostingService {

    /**
     * Calcula y genera una estimación de costo para un tipo de servicio y sus líneas de costo.
     */
    CostEstimate estimateCost(String serviceTypeId, List<CostLine> lines);

    /**
     * Calcula y genera una estimación de costo vinculada a un turno específico.
     */
    CostEstimate estimateCost(String serviceTypeId, String appointmentId, List<CostLine> lines);

    /**
     * Busca una estimación de costo por su identificador.
     */
    CostEstimate findById(String id);

    /**
     * Obtiene el listado de todas las estimaciones generadas.
     */
    List<CostEstimate> findAll();
}
