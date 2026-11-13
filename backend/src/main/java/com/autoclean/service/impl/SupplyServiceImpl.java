package com.autoclean.service.impl;

import com.autoclean.dto.supply.SupplyRequest;
import com.autoclean.dto.supply.SupplyResponse;
import com.autoclean.model.Supply;
import com.autoclean.repository.SupplyRepository;
import com.autoclean.service.SupplyService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio CRUD de insumos con validaciones de inventario.
 */
@Service
public class SupplyServiceImpl implements SupplyService {

    private final SupplyRepository supplyRepository;

    /**
     * Inyección por constructor del repositorio de insumos.
     *
     * @param supplyRepository repositorio JPA de insumos
     */
    public SupplyServiceImpl(SupplyRepository supplyRepository) {
        if (supplyRepository == null) {
            throw new IllegalArgumentException("El repositorio de insumos no puede ser nulo");
        }
        this.supplyRepository = supplyRepository;
    }

    @Override
    @Transactional
    public SupplyResponse create(SupplyRequest request) {
        validateRequest(request);
        Supply supply = new Supply(request.name(), request.stock(), request.unit());
        return SupplyResponse.from(supplyRepository.save(supply));
    }

    @Override
    @Transactional
    public SupplyResponse update(UUID id, SupplyRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del insumo no puede ser nulo");
        }
        validateRequest(request);
        Supply existing = findExisting(id);
        existing.setName(request.name());
        existing.setStock(request.stock());
        existing.setUnit(request.unit());
        return SupplyResponse.from(supplyRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del insumo no puede ser nulo");
        }
        if (!supplyRepository.existsById(id)) {
            throw new IllegalArgumentException("No existe un insumo con el identificador: " + id);
        }
        supplyRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplyResponse findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del insumo no puede ser nulo");
        }
        return SupplyResponse.from(findExisting(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplyResponse> findAll() {
        return supplyRepository.findAllByOrderByNameAsc().stream()
                .map(SupplyResponse::from)
                .toList();
    }

    private Supply findExisting(UUID id) {
        return supplyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un insumo con el identificador: " + id));
    }

    private void validateRequest(SupplyRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("El insumo no puede ser nulo");
        }
        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del insumo no puede estar vacío");
        }
        if (request.stock() == null || request.stock() < 0) {
            throw new IllegalArgumentException("El stock del insumo no puede ser negativo");
        }
        if (request.unit() == null || request.unit().trim().isEmpty()) {
            throw new IllegalArgumentException("La unidad de medida no puede estar vacía");
        }
    }
}

