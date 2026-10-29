package com.autoclean.service.stub;

import com.autoclean.contracts.ReportService;
import com.autoclean.model.ReportResult;
import java.time.LocalDate;

/**
 * Implementación stub temporal para ReportService.
 * NOTA: Esta clase será reemplazada por la implementación de dev1 al fusionar ramas.
 */
public class ReportServiceStub implements ReportService {

    @Override
    public ReportResult getServicesByPeriod(LocalDate from, LocalDate to) {
        throw new UnsupportedOperationException("Pendiente: implementado por dev1");
    }

    @Override
    public ReportResult getIncomeVsCosts(LocalDate from, LocalDate to) {
        throw new UnsupportedOperationException("Pendiente: implementado por dev1");
    }
}
