package com.autoclean.controller;

import com.autoclean.dto.servicetype.ServiceTypeRequestDto;
import com.autoclean.dto.servicetype.ServiceTypeResponseDto;
import com.autoclean.service.ServiceTypeService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la gestión del catálogo de tipos de servicio.
 * Provee endpoints CRUD y expone exclusivamente DTOs.
 */
@RestController
@RequestMapping("/api/service-types")
public class ServiceTypeController {

    private final ServiceTypeService serviceTypeService;

    /**
     * Inyección por constructor del servicio de tipos de servicio.
     *
     * @param serviceTypeService contrato de servicio inyectado por Spring
     */
    public ServiceTypeController(ServiceTypeService serviceTypeService) {
        this.serviceTypeService = serviceTypeService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceTypeResponseDto>> findAll() {
        return ResponseEntity.ok(serviceTypeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceTypeResponseDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceTypeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ServiceTypeResponseDto> create(@Valid @RequestBody ServiceTypeRequestDto request) {
        ServiceTypeResponseDto created = serviceTypeService.create(request);
        URI location = URI.create("/api/service-types/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceTypeResponseDto> update(@PathVariable UUID id, @Valid @RequestBody ServiceTypeRequestDto request) {
        return ResponseEntity.ok(serviceTypeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        serviceTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
