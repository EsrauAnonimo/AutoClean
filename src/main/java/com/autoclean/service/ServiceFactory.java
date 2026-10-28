package com.autoclean.service;

import com.autoclean.contracts.AppointmentService;
import com.autoclean.contracts.CostingService;
import com.autoclean.contracts.CustomerService;
import com.autoclean.contracts.EmployeeService;
import com.autoclean.contracts.ReportService;
import com.autoclean.contracts.ServiceTypeService;
import com.autoclean.contracts.VehicleService;
import com.autoclean.service.stub.AppointmentServiceStub;
import com.autoclean.service.stub.CostingServiceStub;
import com.autoclean.service.stub.CustomerServiceStub;
import com.autoclean.service.stub.EmployeeServiceStub;
import com.autoclean.service.stub.ReportServiceStub;
import com.autoclean.service.stub.ServiceTypeServiceStub;
import com.autoclean.service.stub.VehicleServiceStub;

/**
 * Fábrica centralizada de servicios (ServiceFactory).
 * Provee instancias de los contratos de servicio para la capa de presentación (UI).
 * 
 * NOTA CRÍTICA DE INTEGRACIÓN:
 * En esta rama 'leader', los métodos retornan stubs temporales para permitir la compilación
 * y prueba de las vistas Swing de forma aislada sin romper el EDT.
 * 
 * AL FUSIONAR CON LA RAMA 'dev1':
 * - Se conservará la versión de dev1 de ServiceFactory y com/autoclean/service/impl.
 * - Se eliminará por completo el paquete com/autoclean/service/stub.
 */
public final class ServiceFactory {

    private static final CustomerService CUSTOMER_SERVICE = new CustomerServiceStub();
    private static final VehicleService VEHICLE_SERVICE = new VehicleServiceStub();
    private static final ServiceTypeService SERVICE_TYPE_SERVICE = new ServiceTypeServiceStub();
    private static final EmployeeService EMPLOYEE_SERVICE = new EmployeeServiceStub();
    private static final AppointmentService APPOINTMENT_SERVICE = new AppointmentServiceStub();
    private static final CostingService COSTING_SERVICE = new CostingServiceStub();
    private static final ReportService REPORT_SERVICE = new ReportServiceStub();

    private ServiceFactory() {
        // Constructor privado para clase utilitaria
    }

    public static CustomerService getCustomerService() {
        return CUSTOMER_SERVICE;
    }

    public static VehicleService getVehicleService() {
        return VEHICLE_SERVICE;
    }

    public static ServiceTypeService getServiceTypeService() {
        return SERVICE_TYPE_SERVICE;
    }

    public static EmployeeService getEmployeeService() {
        return EMPLOYEE_SERVICE;
    }

    public static AppointmentService getAppointmentService() {
        return APPOINTMENT_SERVICE;
    }

    public static CostingService getCostingService() {
        return COSTING_SERVICE;
    }

    public static ReportService getReportService() {
        return REPORT_SERVICE;
    }
}
