package com.autoclean.controller;

import com.autoclean.dto.supply.SupplyRequest;
import com.autoclean.dto.supply.SupplyResponse;
import com.autoclean.service.SupplyService;
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
 * Controlador REST para la gestión CRUD de insumos en {@code /api/supplies}.
 */
@RestController
@RequestMapping("/api/supplies")
public class SupplyController {

    private final SupplyService supplyService;

    /**
     * Inyección por constructor del servicio de insumos.
     *
     * @param supplyService servicio de gestión de insumos
     */
    public SupplyController(SupplyService supplyService) {
        this.supplyService = supplyService;
    }

    @GetMapping
    public ResponseEntity<List<SupplyResponse>> findAll() {
        return ResponseEntity.ok(supplyService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplyResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(supplyService.findById(id));
    }

    @PostMapping
    public ResponseEntity<SupplyResponse> create(@Valid @RequestBody SupplyRequest request) {
        SupplyResponse created = supplyService.create(request);
        URI location = URI.create("/api/supplies/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplyResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody SupplyRequest request) {
        return ResponseEntity.ok(supplyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        supplyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

