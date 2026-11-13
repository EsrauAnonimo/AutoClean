package com.autoclean.service;

import com.autoclean.dto.supply.SupplyRequest;
import com.autoclean.dto.supply.SupplyResponse;
import java.util.List;
import java.util.UUID;

/**
 * Contrato de servicio para la administración CRUD de insumos.
 */
public interface SupplyService {

    SupplyResponse create(SupplyRequest request);

    SupplyResponse update(UUID id, SupplyRequest request);

    void delete(UUID id);

    SupplyResponse findById(UUID id);

    List<SupplyResponse> findAll();
}

