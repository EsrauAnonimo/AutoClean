package com.autoclean.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.autoclean.rules.spec.LegacyMirrorReportSpec;
import com.autoclean.rules.spec.ReportSpec;
import com.autoclean.rules.spec.ReportSpec.AppointmentRecordSpec;
import com.autoclean.rules.spec.ReportSpec.ReportResultSpec;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Especificación de validación de rangos y título vacío en reportes (AGENTS.md §8 / ReportServiceImpl).
 */
class ReportDateRangeTest {

    private ReportSpec reportSpec;

    @BeforeEach
    void setUp() {
        reportSpec = new LegacyMirrorReportSpec();
    }

    @Test
    @DisplayName("Rango válido debe retornar registros coincidentes del periodo")
    void shouldReturnServicesWithinValidDateRange() {
        reportSpec.registerAppointment(new AppointmentRecordSpec(
                "a1", "Carlos Pérez", "Lavado Premium", "FINISHED",
                LocalDateTime.of(2026, 10, 15, 10, 0), 60000.0, 20000.0));

        ReportResultSpec servicesReport = reportSpec.servicesByPeriod(
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
        ReportResultSpec revenueReport = reportSpec.revenueVsCost(
                LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 15));

        assertEquals(1, servicesReport.rows().size());
        assertEquals("a1", servicesReport.rows().get(0)[0]);
        assertEquals(1, revenueReport.rows().size());
        assertEquals("40000.0", revenueReport.rows().get(0)[3]);
    }

    @Test
    @DisplayName("from > to debe lanzar IllegalArgumentException en ambos reportes")
    void shouldRejectInvertedDateRange() {
        LocalDate from = LocalDate.of(2026, 10, 31);
        LocalDate to = LocalDate.of(2026, 10, 1);

        assertThrows(IllegalArgumentException.class, () -> reportSpec.servicesByPeriod(from, to));
        assertThrows(IllegalArgumentException.class, () -> reportSpec.revenueVsCost(from, to));
    }

    @Test
    @DisplayName("Fechas nulas deben lanzar IllegalArgumentException")
    void shouldRejectNullDates() {
        LocalDate valid = LocalDate.of(2026, 10, 1);

        assertThrows(IllegalArgumentException.class, () -> reportSpec.servicesByPeriod(null, valid));
        assertThrows(IllegalArgumentException.class, () -> reportSpec.revenueVsCost(valid, null));
    }

    @Test
    @DisplayName("Sin registros en el periodo debe retornar título exacto 'Sin datos en el periodo'")
    void shouldReturnNoDataTitleWhenPeriodHasNoRecords() {
        LocalDate from = LocalDate.of(2026, 11, 1);
        LocalDate to = LocalDate.of(2026, 11, 30);

        ReportResultSpec servicesEmpty = reportSpec.servicesByPeriod(from, to);
        ReportResultSpec revenueEmpty = reportSpec.revenueVsCost(from, to);

        assertEquals("Sin datos en el periodo", servicesEmpty.title());
        assertTrue(servicesEmpty.rows().isEmpty());
        assertEquals("Sin datos en el periodo", revenueEmpty.title());
        assertTrue(revenueEmpty.rows().isEmpty());
    }
}
