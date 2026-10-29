package com.autoclean.service.impl;

import com.autoclean.contracts.CostingService;
import com.autoclean.model.CostEstimate;
import com.autoclean.model.CostLine;
import com.autoclean.model.ServiceType;
import com.autoclean.repository.CostEstimateRepository;
import com.autoclean.repository.ServiceTypeRepository;
import java.util.List;
import java.util.UUID;

/**
 * Implementación del motor de costeo para servicios automotrices.
 */
public class CostingServiceImpl implements CostingService {

    private final CostEstimateRepository costEstimateRepository;
    private final ServiceTypeRepository serviceTypeRepository;

    /**
     * Constructor por defecto utilizando repositorios en memoria estándar.
     */
    public CostingServiceImpl() {
        this(new CostEstimateRepository(), new ServiceTypeRepository());
    }

    /**
     * Constructor con inyección de repositorios.
     *
     * @param costEstimateRepository repositorio de estimaciones
     * @param serviceTypeRepository repositorio del catálogo de servicios
     */
    public CostingServiceImpl(CostEstimateRepository costEstimateRepository, ServiceTypeRepository serviceTypeRepository) {
        if (costEstimateRepository == null) {
            throw new IllegalArgumentException("El repositorio de estimaciones no puede ser nulo");
        }
        if (serviceTypeRepository == null) {
            throw new IllegalArgumentException("El repositorio de tipos de servicio no puede ser nulo");
        }
        this.costEstimateRepository = costEstimateRepository;
        this.serviceTypeRepository = serviceTypeRepository;
    }

    @Override
    public CostEstimate estimateCost(String serviceTypeId, List<CostLine> lines) {
        return estimateCost(serviceTypeId, null, lines);
    }

    @Override
    public CostEstimate estimateCost(String serviceTypeId, String appointmentId, List<CostLine> lines) {
        if (serviceTypeId == null || serviceTypeId.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de servicio es obligatorio para realizar el costeo");
        }

        ServiceType serviceType = serviceTypeRepository.findById(serviceTypeId.trim());
        if (serviceType == null) {
            throw new IllegalArgumentException("No se encontró el tipo de servicio con identificador: " + serviceTypeId);
        }

        double price = serviceType.getPrice();

        // Sumatoria de montos de cada línea de costo directo (materiales, insumos, mano de obra)
        double estimatedCost = 0.0;
        if (lines != null) {
            for (CostLine line : lines) {
                if (line != null) {
                    estimatedCost += line.getAmount();
                }
            }
        }

        // Regla contable: difference NO es ganancia neta.
        // Es el margen de contribución bruto entre el precio cobrado y los costos directos estimados,
        // sin considerar costos fijos de operación, arriendos, servicios públicos ni cargas impositivas.
        double difference = price - estimatedCost;

        String id = UUID.randomUUID().toString();
        CostEstimate estimate = new CostEstimate(id, serviceTypeId.trim(), appointmentId, lines, estimatedCost, price, difference);
        return costEstimateRepository.save(estimate);
    }

    @Override
    public CostEstimate findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        return costEstimateRepository.findById(id);
    }

    @Override
    public List<CostEstimate> findAll() {
        return costEstimateRepository.findAll();
    }
}

