package com.autoclean.rules.spec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Stub espejo de {@code CostingServiceImpl} en el sistema legacy.
 */
public class LegacyMirrorCostingSpec implements CostingSpec {

    private final Map<String, ServiceTypeSpec> serviceTypes = new HashMap<>();

    @Override
    public void registerServiceType(ServiceTypeSpec serviceType) {
        if (serviceType == null || serviceType.id() == null || serviceType.id().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de servicio y su identificador son obligatorios");
        }
        serviceTypes.put(serviceType.id().trim(), serviceType);
    }

    @Override
    public CostEstimateSpec estimateCost(String serviceTypeId, String appointmentId, List<CostLineSpec> lines) {
        if (serviceTypeId == null || serviceTypeId.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de servicio es obligatorio para realizar el costeo");
        }

        ServiceTypeSpec serviceType = serviceTypes.get(serviceTypeId.trim());
        if (serviceType == null) {
            throw new IllegalArgumentException("No se encontró el tipo de servicio con identificador: " + serviceTypeId);
        }

        double price = serviceType.price();
        double estimatedCost = 0.0;
        List<CostLineSpec> safeLines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
        for (CostLineSpec line : safeLines) {
            if (line != null) {
                estimatedCost += line.amount();
            }
        }

        double difference = price - estimatedCost;
        String id = UUID.randomUUID().toString();
        String normalizedAppointmentId = appointmentId != null ? appointmentId.trim() : null;

        return new CostEstimateSpec(id, serviceTypeId.trim(), normalizedAppointmentId, safeLines, estimatedCost, price, difference);
    }
}
