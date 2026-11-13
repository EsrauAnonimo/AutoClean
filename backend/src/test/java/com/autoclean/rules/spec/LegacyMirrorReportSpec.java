package com.autoclean.rules.spec;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stub espejo de {@code ReportServiceImpl} en el sistema legacy.
 */
public class LegacyMirrorReportSpec implements ReportSpec {

    private final List<AppointmentRecordSpec> records = new ArrayList<>();

    @Override
    public void registerAppointment(AppointmentRecordSpec record) {
        if (record == null) {
            throw new IllegalArgumentException("El registro del turno no puede ser nulo");
        }
        records.add(record);
    }

    @Override
    public ReportResultSpec servicesByPeriod(LocalDate from, LocalDate to) {
        validateDateRange(from, to);

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        List<String[]> rows = new ArrayList<>();

        for (AppointmentRecordSpec rec : records) {
            if (rec.dateTime() != null && !rec.dateTime().isBefore(start) && !rec.dateTime().isAfter(end)) {
                rows.add(new String[]{
                        rec.id() != null ? rec.id() : "",
                        rec.customerName() != null ? rec.customerName() : "",
                        rec.serviceName() != null ? rec.serviceName() : "",
                        rec.status() != null ? rec.status() : "",
                        rec.dateTime().toString()
                });
            }
        }

        if (rows.isEmpty()) {
            return new ReportResultSpec("Sin datos en el periodo", LocalDateTime.now(), rows);
        }
        return new ReportResultSpec("Servicios por periodo (" + from + " al " + to + ")", LocalDateTime.now(), rows);
    }

    @Override
    public ReportResultSpec revenueVsCost(LocalDate from, LocalDate to) {
        validateDateRange(from, to);

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        List<String[]> rows = new ArrayList<>();

        for (AppointmentRecordSpec rec : records) {
            if (rec.dateTime() != null && !rec.dateTime().isBefore(start) && !rec.dateTime().isAfter(end)) {
                double difference = rec.price() - rec.estimatedCost();
                rows.add(new String[]{
                        rec.serviceName() != null ? rec.serviceName() : "",
                        String.valueOf(rec.price()),
                        String.valueOf(rec.estimatedCost()),
                        String.valueOf(difference)
                });
            }
        }

        if (rows.isEmpty()) {
            return new ReportResultSpec("Sin datos en el periodo", LocalDateTime.now(), rows);
        }
        return new ReportResultSpec("Ingresos vs Costos (" + from + " al " + to + ")", LocalDateTime.now(), rows);
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la fecha inicial");
        }
    }
}
