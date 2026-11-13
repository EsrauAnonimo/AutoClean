package com.autoclean.service.impl;

import com.autoclean.dto.report.ReportResult;
import com.autoclean.model.Appointment;
import com.autoclean.model.CostEstimate;
import com.autoclean.model.Customer;
import com.autoclean.model.ServiceType;
import com.autoclean.repository.CostEstimateRepository;
import com.autoclean.service.ReportService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de reportes gerenciales migrada desde {@code legacy-javafx}.
 */
@Service
public class ReportServiceImpl implements ReportService {

    public static final String NO_DATA_TITLE = "Sin datos en el periodo";

    private static final String APPOINTMENTS_IN_RANGE_JPQL = """
            SELECT a FROM Appointment a
            JOIN FETCH a.customer
            JOIN FETCH a.serviceType
            WHERE a.dateTime >= :start AND a.dateTime <= :end
            ORDER BY a.dateTime ASC
            """;

    private final CostEstimateRepository costEstimateRepository;
    private final EntityManagerFactory entityManagerFactory;

    /**
     * Inyección por constructor del repositorio de estimaciones y fábrica JPA.
     *
     * @param costEstimateRepository repositorio de estimaciones de costo
     * @param entityManagerFactory fábrica de gestores JPA para consultar citas con sus relaciones
     */
    public ReportServiceImpl(
            CostEstimateRepository costEstimateRepository,
            EntityManagerFactory entityManagerFactory) {
        if (costEstimateRepository == null) {
            throw new IllegalArgumentException("El repositorio de estimaciones de costo no puede ser nulo");
        }
        if (entityManagerFactory == null) {
            throw new IllegalArgumentException("La fábrica de EntityManager no puede ser nula");
        }
        this.costEstimateRepository = costEstimateRepository;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la fecha inicial");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResult servicesByPeriod(LocalDate from, LocalDate to) {
        validateDateRange(from, to);

        List<Appointment> appointments = findAppointmentsInPeriod(from, to);
        List<String[]> rows = new ArrayList<>();

        for (Appointment appointment : appointments) {
            Customer customer = appointment.getCustomer();
            ServiceType serviceType = appointment.getServiceType();

            String id = appointment.getId() != null ? appointment.getId().toString() : "";
            String customerName = customer != null && customer.getName() != null ? customer.getName() : "";
            String serviceName = serviceType != null && serviceType.getName() != null ? serviceType.getName() : "";
            String status = appointment.getStatus() != null ? appointment.getStatus().name() : "";
            String dateStr = appointment.getDateTime() != null ? appointment.getDateTime().toString() : "";

            rows.add(new String[]{id, customerName, serviceName, status, dateStr});
        }

        if (rows.isEmpty()) {
            return new ReportResult(NO_DATA_TITLE, LocalDateTime.now(), rows);
        }

        String title = "Servicios por periodo (" + from + " al " + to + ")";
        return new ReportResult(title, LocalDateTime.now(), rows);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResult revenueVsCost(LocalDate from, LocalDate to) {
        validateDateRange(from, to);

        List<Appointment> appointments = findAppointmentsInPeriod(from, to);
        List<String[]> rows = new ArrayList<>();

        if (!appointments.isEmpty()) {
            List<CostEstimate> estimates = costEstimateRepository.findAllByOrderByCreatedAtDesc();
            for (Appointment appointment : appointments) {
                CostEstimate estimate = findEstimateForAppointment(appointment, estimates);
                if (estimate != null) {
                    ServiceType serviceType = estimate.getServiceType() != null
                            ? estimate.getServiceType()
                            : appointment.getServiceType();
                    String serviceName = serviceType != null && serviceType.getName() != null
                            ? serviceType.getName()
                            : "";

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
            return new ReportResult(NO_DATA_TITLE, LocalDateTime.now(), rows);
        }

        String title = "Ingresos vs Costos (" + from + " al " + to + ")";
        return new ReportResult(title, LocalDateTime.now(), rows);
    }

    private List<Appointment> findAppointmentsInPeriod(LocalDate from, LocalDate to) {
        // Se amplía +/- 1 día en UTC en la consulta JPQL para cubrir desfases horarios y filtrar exacto por fecha local
        OffsetDateTime queryStart = from.minusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime queryEnd = to.plusDays(1).atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);

        List<Appointment> candidates = queryAppointments(queryStart, queryEnd);
        List<Appointment> inRange = new ArrayList<>();
        for (Appointment appointment : candidates) {
            if (appointment.getDateTime() != null) {
                LocalDate appointmentDate = appointment.getDateTime().toLocalDate();
                if (!appointmentDate.isBefore(from) && !appointmentDate.isAfter(to)) {
                    inRange.add(appointment);
                }
            }
        }
        return inRange;
    }

    private List<Appointment> queryAppointments(OffsetDateTime start, OffsetDateTime end) {
        EntityManager transactionalEm = EntityManagerFactoryUtils.getTransactionalEntityManager(entityManagerFactory);
        if (transactionalEm != null) {
            return executeAppointmentsQuery(transactionalEm, start, end);
        }
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            return executeAppointmentsQuery(em, start, end);
        }
    }

    private List<Appointment> executeAppointmentsQuery(EntityManager em, OffsetDateTime start, OffsetDateTime end) {
        TypedQuery<Appointment> query = em.createQuery(APPOINTMENTS_IN_RANGE_JPQL, Appointment.class);
        query.setParameter("start", start);
        query.setParameter("end", end);
        return query.getResultList();
    }

    private CostEstimate findEstimateForAppointment(Appointment appointment, List<CostEstimate> estimates) {
        if (estimates == null || estimates.isEmpty()) {
            return null;
        }
        UUID appointmentId = appointment.getId();
        if (appointmentId != null) {
            for (CostEstimate estimate : estimates) {
                if (estimate.getAppointment() != null && appointmentId.equals(estimate.getAppointment().getId())) {
                    return estimate;
                }
            }
        }
        UUID serviceTypeId = appointment.getServiceType() != null ? appointment.getServiceType().getId() : null;
        if (serviceTypeId != null) {
            for (CostEstimate estimate : estimates) {
                if (estimate.getServiceType() != null && serviceTypeId.equals(estimate.getServiceType().getId())) {
                    return estimate;
                }
            }
        }
        return null;
    }
}

