package com.autoclean.controller;

import com.autoclean.dto.report.ReportRequest;
import com.autoclean.dto.report.ReportResult;
import com.autoclean.service.ReportService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para consultar reportes de servicios e ingresos vs costos.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    /**
     * Inyección por constructor del servicio de reportes.
     *
     * @param reportService servicio generador de reportes
     */
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/services")
    public ResponseEntity<ReportResult> getServicesByPeriod(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        ReportRequest request = new ReportRequest(from, to);
        return ResponseEntity.ok(reportService.servicesByPeriod(request.from(), request.to()));
    }

    @GetMapping("/revenue")
    public ResponseEntity<ReportResult> getRevenueVsCost(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        ReportRequest request = new ReportRequest(from, to);
        return ResponseEntity.ok(reportService.revenueVsCost(request.from(), request.to()));
    }
}

