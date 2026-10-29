package com.autoclean.service.impl;

import com.autoclean.contracts.ReportService;
import com.autoclean.model.Appointment;
import com.autoclean.model.CostEstimate;
import com.autoclean.model.Customer;
import com.autoclean.model.ReportResult;
import com.autoclean.model.ServiceType;
import com.autoclean.repository.AppointmentRepository;
import com.autoclean.repository.CostEstimateRepository;
import com.autoclean.repository.CustomerRepository;
import com.autoclean.repository.ServiceTypeRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del servicio de generación de reportes gerenciales.
 */
public class ReportServiceImpl implements ReportService {

    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final CostEstimateRepository costEstimateRepository;

    /**
     * Constructor por defecto con repositorios independientes.
     */
    public ReportServiceImpl() {
        this(new AppointmentRepository(), new CustomerRepository(), new ServiceTypeRepository(), new CostEstimateRepository());
    }

    /**
     * Constructor completo con inyección de dependencias para compartir datos con otros servicios.
     *
     * @param appointmentRepository repositorio de turnos
     * @param customerRepository repositorio de clientes
     * @param serviceTypeRepository repositorio de tipos de servicio
     * @param costEstimateRepository repositorio de estimaciones de costo
     */
    public ReportServiceImpl(AppointmentRepository appointmentRepository,
                             CustomerRepository customerRepository,
                             ServiceTypeRepository serviceTypeRepository,
                             CostEstimateRepository costEstimateRepository) {
        if (appointmentRepository == null || customerRepository == null ||
            serviceTypeRepository == null || costEstimateRepository == null) {
            throw new IllegalArgumentException("Los repositorios para ReportService no pueden ser nulos");
        }
        this.appointmentRepository = appointmentRepository;
        this.customerRepository = customerRepository;
        this.serviceTypeRepository = serviceTypeRepository;
        this.costEstimateRepository = costEstimateRepository;
    }

    @Override
    public ReportResult getServicesByPeriod(LocalDate from, LocalDate to) {
        return servicesByPeriod(from, to);
    }

    @Override
    public ReportResult getIncomeVsCosts(LocalDate from, LocalDate to) {
        return revenueVsCost(from, to);
    }

    /**
     * Genera el reporte de servicios ejecutados o programados en el rango de fechas.
     * Columnas: id | cliente | servicio | estado | fecha
     *
     * @param from fecha inicial
     * @param to fecha final
     * @return resultado del reporte con filas tabuladas
     */
    public ReportResult servicesByPeriod(LocalDate from, LocalDate to) {
        validateDateRange(from, to);

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        List<String[]> rows = new ArrayList<>();

        for (Appointment app : appointmentRepository.findAll()) {
            if (app.getDateTime() != null &&
                !app.getDateTime().isBefore(start) &&
                !app.getDateTime().isAfter(end)) {

                // Resolución de nombres asociados a los identificadores foráneos
                Customer customer = customerRepository.findById(app.getCustomerId());
                String customerName = customer != null ? customer.getName() : (app.getCustomerId() != null ? app.getCustomerId() : "");

                ServiceType serviceType = serviceTypeRepository.findById(app.getServiceTypeId());
                String serviceName = serviceType != null ? serviceType.getName() : (app.getServiceTypeId() != null ? app.getServiceTypeId() : "");

                String status = app.getStatus() != null ? app.getStatus() : "";
                String dateStr = app.getDateTime().toString();

                rows.add(new String[]{
                    app.getId() != null ? app.getId() : "",
                    customerName,
                    serviceName,
                    status,
                    dateStr
                });
            }
        }

        if (rows.isEmpty()) {
            return new ReportResult("Sin datos en el periodo", LocalDateTime.now(), rows);
        }

        String title = "Servicios por periodo (" + from + " al " + to + ")";
        return new ReportResult(title, LocalDateTime.now(), rows);
    }

    /**
     * Genera el reporte comparativo de ingresos versus costos en el rango de fechas.
     * Columnas: servicio | precio | costoEstimado | diferencia
     *
     * @param from fecha inicial
     * @param to fecha final
     * @return resultado del reporte con filas tabuladas
     */
    public ReportResult revenueVsCost(LocalDate from, LocalDate to) {
        validateDateRange(from, to);

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        List<String[]> rows = new ArrayList<>();

        for (Appointment app : appointmentRepository.findAll()) {
            if (app.getDateTime() != null &&
                !app.getDateTime().isBefore(start) &&
                !app.getDateTime().isAfter(end)) {

                // Buscar estimación vinculada específicamente a la cita o al tipo de servicio
                CostEstimate estimate = findEstimateForAppointment(app);
                if (estimate != null) {
                    ServiceType serviceType = serviceTypeRepository.findById(estimate.getServiceTypeId());
                    String serviceName = serviceType != null ? serviceType.getName() : estimate.getServiceTypeId();

                    rows.add(new String[]{
                        serviceName,
                        String.valueOf(estimate.getPrice()),
                        String.valueOf(estimate.getEstimatedCost()),
                        String.valueOf(estimate.getDifference())
                    });
                }
            }
        }

        if (rows.isEmpty()) {
            return new ReportResult("Sin datos en el periodo", LocalDateTime.now(), rows);
        }

        String title = "Ingresos vs Costos (" + from + " al " + to + ")";
        return new ReportResult(title, LocalDateTime.now(), rows);
    }

    /**
     * Valida que las fechas no sean nulas y que el rango sea coherente.
     */
    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la fecha inicial");
        }
    }

    /**
     * Busca la estimación de costo asociada a una cita, primero por appointmentId y luego por serviceTypeId.
     */
    private CostEstimate findEstimateForAppointment(Appointment app) {
        if (app.getId() != null) {
            for (CostEstimate ce : costEstimateRepository.findAll()) {
                if (app.getId().equals(ce.getAppointmentId())) {
                    return ce;
                }
            }
        }
        if (app.getServiceTypeId() != null) {
            for (CostEstimate ce : costEstimateRepository.findAll()) {
                if (app.getServiceTypeId().equals(ce.getServiceTypeId())) {
                    return ce;
                }
            }
        }
        return null;
    }
}
