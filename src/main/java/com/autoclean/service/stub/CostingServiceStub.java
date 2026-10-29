package com.autoclean.service.stub;

import com.autoclean.contracts.CostingService;
import com.autoclean.model.CostEstimate;
import com.autoclean.model.CostLine;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación stub temporal para CostingService.
 * NOTA: Esta clase será reemplazada por la implementación de dev1 al fusionar ramas.
 */
public class CostingServiceStub implements CostingService {

    @Override
    public CostEstimate estimateCost(String serviceTypeId, List<CostLine> lines) {
        throw new UnsupportedOperationException("Pendiente: implementado por dev1");
    }

    @Override
    public CostEstimate estimateCost(String serviceTypeId, String appointmentId, List<CostLine> lines) {
        throw new UnsupportedOperationException("Pendiente: implementado por dev1");
    }

    @Override
    public CostEstimate findById(String id) {
        return null;
    }

    @Override
    public List<CostEstimate> findAll() {
        return new ArrayList<>();
    }
}
