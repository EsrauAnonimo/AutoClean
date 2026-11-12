package com.autoclean.service;

import com.autoclean.contracts.AppointmentService;
import com.autoclean.contracts.CostingService;
import com.autoclean.contracts.CustomerService;
import com.autoclean.contracts.EmployeeService;
import com.autoclean.contracts.ReportService;
import com.autoclean.contracts.ServiceTypeService;
import com.autoclean.contracts.VehicleService;
import com.autoclean.repository.AppointmentRepository;
import com.autoclean.repository.CostEstimateRepository;
import com.autoclean.repository.CustomerRepository;
import com.autoclean.repository.EmployeeRepository;
import com.autoclean.repository.ServiceTypeRepository;
import com.autoclean.repository.VehicleRepository;
import com.autoclean.service.impl.AppointmentServiceImpl;
import com.autoclean.service.impl.CostingServiceImpl;
import com.autoclean.service.impl.CustomerServiceImpl;
import com.autoclean.service.impl.EmployeeServiceImpl;
import com.autoclean.service.impl.ReportServiceImpl;
import com.autoclean.service.impl.ServiceCatalogServiceImpl;
import com.autoclean.service.impl.VehicleServiceImpl;

/**
 * Fábrica centralizada de servicios (ServiceFactory).
 * Provee las implementaciones reales de la capa de dominio y lógica de negocio.
 * Esta clase reemplaza a la versión de stubs de la rama leader al momento de la fusión.
 */
public final class ServiceFactory {

    // Repositorios compartidos en memoria para persistencia consistente entre módulos
    private static final CustomerRepository CUSTOMER_REPOSITORY = new CustomerRepository();
    private static final VehicleRepository VEHICLE_REPOSITORY = new VehicleRepository();
    private static final ServiceTypeRepository SERVICE_TYPE_REPOSITORY = new ServiceTypeRepository();
    private static final EmployeeRepository EMPLOYEE_REPOSITORY = new EmployeeRepository();
    private static final AppointmentRepository APPOINTMENT_REPOSITORY = new AppointmentRepository();
    private static final CostEstimateRepository COST_ESTIMATE_REPOSITORY = new CostEstimateRepository();

    // Instancias reales de los servicios
    private static final CustomerService CUSTOMER_SERVICE = new CustomerServiceImpl(CUSTOMER_REPOSITORY);
    private static final VehicleService VEHICLE_SERVICE = new VehicleServiceImpl(VEHICLE_REPOSITORY);
    private static final ServiceTypeService SERVICE_TYPE_SERVICE = new ServiceCatalogServiceImpl(SERVICE_TYPE_REPOSITORY);
    private static final EmployeeService EMPLOYEE_SERVICE = new EmployeeServiceImpl(EMPLOYEE_REPOSITORY);
    private static final AppointmentService APPOINTMENT_SERVICE = new AppointmentServiceImpl(APPOINTMENT_REPOSITORY);
    private static final CostingService COSTING_SERVICE = new CostingServiceImpl(COST_ESTIMATE_REPOSITORY, SERVICE_TYPE_REPOSITORY);
    private static final ReportService REPORT_SERVICE = new ReportServiceImpl(APPOINTMENT_REPOSITORY, CUSTOMER_REPOSITORY, SERVICE_TYPE_REPOSITORY, COST_ESTIMATE_REPOSITORY);

    private ServiceFactory() {
        // Constructor privado para clase utilitaria
    }

    public static CustomerService customerService() {
        return CUSTOMER_SERVICE;
    }

    public static CustomerService getCustomerService() {
        return CUSTOMER_SERVICE;
    }

    public static VehicleService vehicleService() {
        return VEHICLE_SERVICE;
    }

    public static VehicleService getVehicleService() {
        return VEHICLE_SERVICE;
    }

    public static ServiceTypeService serviceCatalogService() {
        return SERVICE_TYPE_SERVICE;
    }

    public static ServiceTypeService serviceTypeService() {
        return SERVICE_TYPE_SERVICE;
    }

    public static ServiceTypeService getServiceTypeService() {
        return SERVICE_TYPE_SERVICE;
    }

    public static EmployeeService employeeService() {
        return EMPLOYEE_SERVICE;
    }

    public static EmployeeService getEmployeeService() {
        return EMPLOYEE_SERVICE;
    }

    public static AppointmentService appointmentService() {
        return APPOINTMENT_SERVICE;
    }

    public static AppointmentService getAppointmentService() {
        return APPOINTMENT_SERVICE;
    }

    public static CostingService costingService() {
        return COSTING_SERVICE;
    }

    public static CostingService getCostingService() {
        return COSTING_SERVICE;
    }

    public static ReportService reportService() {
        return REPORT_SERVICE;
    }

    public static ReportService getReportService() {
        return REPORT_SERVICE;
    }
}
