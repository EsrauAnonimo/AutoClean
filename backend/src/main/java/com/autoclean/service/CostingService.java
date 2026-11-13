package com.autoclean.service;

import com.autoclean.dto.costing.CostEstimateRequest;
import com.autoclean.dto.costing.CostEstimateResponse;
import com.autoclean.model.CostLine;
import java.util.List;
import java.util.UUID;

/**
 * Contrato del motor de costeo para estimar costos directos y margen bruto de servicios.
 */
public interface CostingService {

    /**
     * Calcula y guarda una estimación de costo a partir del DTO recibido desde la API.
     *
     * @param request datos de entrada con tipo de servicio, cita opcional y líneas de costo
     * @return estimación calculada con sus líneas asociadas
     */
    CostEstimateResponse estimateCost(CostEstimateRequest request);

    /**
     * Calcula y guarda una estimación de costo para un tipo de servicio, cita opcional y líneas de costo.
     *
     * @param serviceTypeId identificador del tipo de servicio
     * @param appointmentId identificador opcional de la cita asociada
     * @param lines líneas de costo directo
     * @return estimación calculada con sus líneas asociadas
     */
    CostEstimateResponse estimateCost(UUID serviceTypeId, UUID appointmentId, List<CostLine> lines);

    /**
     * Busca una estimación de costo por su identificador único.
     *
     * @param id identificador de la estimación
     * @return estimación encontrada
     */
    CostEstimateResponse findById(UUID id);

    /**
     * Lista todas las estimaciones de costo registradas ordenadas de la más reciente a la más antigua.
     *
     * @return lista de estimaciones en formato DTO
     */
    List<CostEstimateResponse> findAll();
}

