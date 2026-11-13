package com.autoclean.service.impl;

import com.autoclean.dto.servicetype.ServiceTypeRequestDto;
import com.autoclean.dto.servicetype.ServiceTypeResponseDto;
import com.autoclean.exception.ResourceNotFoundException;
import com.autoclean.model.ServiceType;
import com.autoclean.repository.ServiceTypeRepository;
import com.autoclean.service.ServiceTypeService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de la lógica de negocio para la administración del catálogo de tipos de servicio.
 * Replica las validaciones y reglas del sistema legacy (ServiceTypeServiceImpl / ServiceType.java).
 */
@Service
@Transactional
public class ServiceTypeServiceImpl implements ServiceTypeService {

    private final ServiceTypeRepository serviceTypeRepository;

    /**
     * Inyección por constructor del repositorio de tipos de servicio.
     *
     * @param serviceTypeRepository repositorio Spring Data JPA
     */
    public ServiceTypeServiceImpl(ServiceTypeRepository serviceTypeRepository) {
        if (serviceTypeRepository == null) {
            throw new IllegalArgumentException("El repositorio de tipos de servicio no puede ser nulo");
        }
        this.serviceTypeRepository = serviceTypeRepository;
    }

    @Override
    public ServiceTypeResponseDto create(ServiceTypeRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("La información del tipo de servicio no puede ser nula");
        }

        // Replica validaciones de negocio de legacy-javafx/.../model/ServiceType.java:44,63
        ServiceType serviceType = new ServiceType(
                null,
                request.name(),
                request.description(),
                request.price(),
                request.durationMin() != null ? request.durationMin() : 30
        );

        ServiceType saved = serviceTypeRepository.save(serviceType);
        return toDto(saved);
    }

    @Override
    public ServiceTypeResponseDto update(UUID id, ServiceTypeRequestDto request) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del tipo de servicio no puede ser nulo");
        }
        if (request == null) {
            throw new IllegalArgumentException("La información del tipo de servicio a actualizar no puede ser nula");
        }

        // Validación de existencia previa (ServiceCatalogServiceImpl.java:46-48)
        ServiceType existing = serviceTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un tipo de servicio con el identificador: " + id));

        existing.setName(request.name());
        existing.setDescription(request.description());
        existing.setPrice(request.price());
        if (request.durationMin() != null) {
            existing.setDurationMin(request.durationMin());
        }

        ServiceType updated = serviceTypeRepository.save(existing);
        return toDto(updated);
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del tipo de servicio no puede ser nulo");
        }
        // Validación de existencia previa antes de eliminar
        ServiceType existing = serviceTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un tipo de servicio con el identificador: " + id));

        serviceTypeRepository.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceTypeResponseDto findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del tipo de servicio no puede ser nulo");
        }
        ServiceType serviceType = serviceTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un tipo de servicio con el identificador: " + id));

        return toDto(serviceType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceTypeResponseDto> findAll() {
        return serviceTypeRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    private ServiceTypeResponseDto toDto(ServiceType entity) {
        return new ServiceTypeResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getDurationMin(),
                entity.getCreatedAt()
        );
    }
}
