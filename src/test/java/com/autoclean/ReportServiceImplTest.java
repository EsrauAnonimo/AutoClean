package com.autoclean;

import com.autoclean.contracts.ReportService;
import com.autoclean.model.Appointment;
import com.autoclean.model.CostEstimate;
import com.autoclean.model.CostLine;
import com.autoclean.model.Customer;
import com.autoclean.model.ReportResult;
import com.autoclean.model.ServiceType;
import com.autoclean.repository.AppointmentRepository;
import com.autoclean.repository.CostEstimateRepository;
import com.autoclean.repository.CustomerRepository;
import com.autoclean.repository.ServiceTypeRepository;
import com.autoclean.service.impl.ReportServiceImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el servicio de reportes gerenciales (ReportServiceImpl).
 */
public class ReportServiceImplTest {

    private ReportService reportService;
    private AppointmentRepository appointmentRepository;
    private CustomerRepository customerRepository;
    private ServiceTypeRepository serviceTypeRepository;
    private CostEstimateRepository costEstimateRepository;

    @BeforeEach
    void setUp() {
        appointmentRepository = new AppointmentRepository();
        customerRepository = new CustomerRepository();
        serviceTypeRepository = new ServiceTypeRepository();
        costEstimateRepository = new CostEstimateRepository();
        reportService = new ReportServiceImpl(appointmentRepository, customerRepository, serviceTypeRepository, costEstimateRepository);
    }

    @Test
    @DisplayName("Debe retornar título 'Sin datos en el periodo' y lista vacía cuando no hay citas")
    void testServicesByPeriodEmpty() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        ReportResult result = reportService.getServicesByPeriod(from, to);

        assertNotNull(result);
        assertEquals("Sin datos en el periodo", result.getTitle());
        assertTrue(result.getRows().isEmpty());
    }

    @Test
    @DisplayName("Debe generar reporte de servicios por periodo con formato de filas correcto")
    void testServicesByPeriodWithData() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        // Registrar cliente y servicio
        Customer customer = customerRepository.save(new Customer("c1", "Pedro Gómez", "3001112233", "pedro@test.com"));
        ServiceType service = serviceTypeRepository.save(new ServiceType("s1", "Lavado General", "Lavado", 30000.0));

        // Registrar cita dentro del rango
        Appointment appointment = new Appointment("a1", customer.getId(), "v1", service.getId(), "e1",
                LocalDateTime.of(2026, 10, 15, 10, 30), "FINISHED");
        appointmentRepository.save(appointment);

        ReportResult result = reportService.getServicesByPeriod(from, to);

        assertNotNull(result);
        assertEquals(1, result.getRows().size());

        // Verificar columnas: id | cliente | servicio | estado | fecha
        String[] row = result.getRows().get(0);
        assertEquals("a1", row[0]);
        assertEquals("Pedro Gómez", row[1]);
        assertEquals("Lavado General", row[2]);
        assertEquals("FINISHED", row[3]);
        assertTrue(row[4].contains("2026-10-15"));
    }

    @Test
    @DisplayName("Debe generar reporte de ingresos vs costos con formato de filas correcto")
    void testRevenueVsCostWithData() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        ServiceType service = serviceTypeRepository.save(new ServiceType("s1", "Porcelanizado", "Detallado", 200000.0));
        Appointment appointment = new Appointment("a1", "c1", "v1", service.getId(), "e1",
                LocalDateTime.of(2026, 10, 20, 9, 0), "FINISHED");
        appointmentRepository.save(appointment);

        CostEstimate estimate = new CostEstimate("ce1", service.getId(), "a1",
                List.of(new CostLine("Sellador", 50000.0)), 50000.0, 200000.0, 150000.0);
        costEstimateRepository.save(estimate);

        ReportResult result = reportService.getIncomeVsCosts(from, to);

        assertNotNull(result);
        assertEquals(1, result.getRows().size());

        // Columnas: servicio | precio | costoEstimado | diferencia
        String[] row = result.getRows().get(0);
        assertEquals("Porcelanizado", row[0]);
        assertEquals("200000.0", row[1]);
        assertEquals("50000.0", row[2]);
        assertEquals("150000.0", row[3]);
    }
}

