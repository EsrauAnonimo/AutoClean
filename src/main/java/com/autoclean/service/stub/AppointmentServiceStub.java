package com.autoclean.service.stub;

import com.autoclean.contracts.AppointmentService;
import com.autoclean.model.Appointment;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación stub temporal para AppointmentService.
 * NOTA: Esta clase será reemplazada por la implementación de dev1 al fusionar ramas.
 */
public class AppointmentServiceStub implements AppointmentService {

    @Override
    public Appointment create(Appointment appointment) {
        throw new UnsupportedOperationException("Pendiente: implementado por dev1");
    }

    @Override
    public Appointment update(Appointment appointment) {
        throw new UnsupportedOperationException("Pendiente: implementado por dev1");
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("Pendiente: implementado por dev1");
    }

    @Override
    public Appointment findById(String id) {
        return null;
    }

    @Override
    public List<Appointment> findAll() {
        return new ArrayList<>();
    }

    @Override
    public void updateStatus(String appointmentId, String newStatus) {
        throw new UnsupportedOperationException("Pendiente: implementado por dev1");
    }
}
