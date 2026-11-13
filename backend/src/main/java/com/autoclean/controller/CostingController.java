package com.autoclean.controller;

import com.autoclean.dto.costing.CostEstimateRequest;
import com.autoclean.dto.costing.CostEstimateResponse;
import com.autoclean.service.CostingService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST del motor de costeo para calcular y consultar estimaciones de servicios.
 */
@RestController
@RequestMapping("/api/costing")
public class CostingController {

    private final CostingService costingService;

    /**
     * Inyección por constructor del servicio de costeo.
     *
     * @param costingService servicio de dominio de costeo
     */
    public CostingController(CostingService costingService) {
        this.costingService = costingService;
    }

    @PostMapping("/estimate")
    public ResponseEntity<CostEstimateResponse> estimate(@Valid @RequestBody CostEstimateRequest request) {
        CostEstimateResponse created = costingService.estimateCost(request);
        URI location = URI.create("/api/costing/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<List<CostEstimateResponse>> findAll() {
        return ResponseEntity.ok(costingService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CostEstimateResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(costingService.findById(id));
    }
}

