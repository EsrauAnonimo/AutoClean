package com.autoclean.service.impl;

import com.autoclean.dto.costing.CostEstimateRequest;
import com.autoclean.dto.costing.CostEstimateResponse;
import com.autoclean.dto.costing.CostLineRequest;
import com.autoclean.exception.ResourceNotFoundException;
import com.autoclean.model.Appointment;
import com.autoclean.model.CostEstimate;
import com.autoclean.model.CostLine;
import com.autoclean.model.ServiceType;
import com.autoclean.repository.CostEstimateRepository;
import com.autoclean.repository.ServiceTypeRepository;
import com.autoclean.service.CostingService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del motor de costeo migrada desde la lógica de negocio legacy.
 */
@Service
public class CostingServiceImpl implements CostingService {

    private final CostEstimateRepository costEstimateRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final EntityManagerFactory entityManagerFactory;

    /**
     * Constructor principal usado en pruebas unitarias sin resolución JPA de citas.
     *
     * @param costEstimateRepository repositorio JPA de estimaciones de costo
     * @param serviceTypeRepository repositorio JPA de tipos de servicio
     */
    public CostingServiceImpl(
            CostEstimateRepository costEstimateRepository,
            ServiceTypeRepository serviceTypeRepository) {
        this(costEstimateRepository, serviceTypeRepository, null);
    }

    /**
     * Inyección por constructor utilizada por Spring Boot en ejecución completa.
     *
     * @param costEstimateRepository repositorio JPA de estimaciones de costo
     * @param serviceTypeRepository repositorio JPA de tipos de servicio
     * @param entityManagerFactory fábrica de gestores JPA para resolver citas opcionales sin acoplarse a otro módulo
     */
    @Autowired
    public CostingServiceImpl(
            CostEstimateRepository costEstimateRepository,
            ServiceTypeRepository serviceTypeRepository,
            EntityManagerFactory entityManagerFactory) {
        if (costEstimateRepository == null) {
            throw new IllegalArgumentException("El repositorio de estimaciones no puede ser nulo");
        }
        if (serviceTypeRepository == null) {
            throw new IllegalArgumentException("El repositorio de tipos de servicio no puede ser nulo");
        }
        this.costEstimateRepository = costEstimateRepository;
        this.serviceTypeRepository = serviceTypeRepository;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    @Transactional
    public CostEstimateResponse estimateCost(CostEstimateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de costeo es obligatoria");
        }
        List<CostLine> domainLines = toDomainLines(request.lines());
        return estimateCost(request.serviceTypeId(), request.appointmentId(), domainLines);
    }

    @Override
    @Transactional
    public CostEstimateResponse estimateCost(UUID serviceTypeId, UUID appointmentId, List<CostLine> lines) {
        if (serviceTypeId == null) {
            throw new IllegalArgumentException("El tipo de servicio es obligatorio para realizar el costeo");
        }

        ServiceType serviceType = serviceTypeRepository.findById(serviceTypeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el tipo de servicio con identificador: " + serviceTypeId));

        BigDecimal price = serviceType.getPrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio del tipo de servicio no puede ser negativo");
        }

        Appointment appointment = resolveAppointment(appointmentId);

        // Sumatoria de montos de cada línea de costo directo (materiales, insumos, mano de obra)
        BigDecimal estimatedCost = BigDecimal.ZERO;
        List<CostLine> sanitizedLines = new ArrayList<>();
        if (lines != null) {
            for (CostLine line : lines) {
                if (line == null) {
                    continue;
                }
                BigDecimal amount = line.getAmount();
                if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("El monto de la línea de costo no puede ser negativo");
                }
                sanitizedLines.add(new CostLine(line.getConcept(), amount));
                estimatedCost = estimatedCost.add(amount);
            }
        }

        // Regla contable: difference NO es ganancia neta; es el margen bruto frente a costos directos
        BigDecimal difference = price.subtract(estimatedCost);

        CostEstimate estimate = new CostEstimate(serviceType, appointment, sanitizedLines, estimatedCost, price, difference);
        CostEstimate saved = costEstimateRepository.save(estimate);
        return CostEstimateResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CostEstimateResponse findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador de la estimación es obligatorio");
        }
        CostEstimate estimate = costEstimateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la estimación de costo con id: " + id));
        return CostEstimateResponse.from(estimate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CostEstimateResponse> findAll() {
        return costEstimateRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(CostEstimateResponse::from)
                .toList();
    }

    private List<CostLine> toDomainLines(List<CostLineRequest> lineRequests) {
        if (lineRequests == null || lineRequests.isEmpty()) {
            return List.of();
        }
        List<CostLine> domainLines = new ArrayList<>(lineRequests.size());
        for (CostLineRequest item : lineRequests) {
            if (item == null) {
                continue;
            }
            if (item.amount() == null || item.amount().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El monto de la línea de costo no puede ser negativo");
            }
            domainLines.add(new CostLine(item.concept(), item.amount()));
        }
        return domainLines;
    }

    private Appointment resolveAppointment(UUID appointmentId) {
        if (appointmentId == null) {
            return null;
        }
        if (entityManagerFactory == null) {
            throw new IllegalStateException("No hay EntityManagerFactory configurado para resolver la cita: " + appointmentId);
        }
        EntityManager transactionalEm = EntityManagerFactoryUtils.getTransactionalEntityManager(entityManagerFactory);
        if (transactionalEm != null) {
            return findAppointmentOrThrow(transactionalEm, appointmentId);
        }
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            return findAppointmentOrThrow(em, appointmentId);
        }
    }

    private Appointment findAppointmentOrThrow(EntityManager em, UUID appointmentId) {
        Appointment found = em.find(Appointment.class, appointmentId);
        if (found == null) {
            throw new IllegalArgumentException("No se encontró la cita con identificador: " + appointmentId);
        }
        return found;
    }
}

