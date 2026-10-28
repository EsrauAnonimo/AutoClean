package com.autoclean.repository;

import com.autoclean.model.Appointment;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Repositorio en memoria para la entidad Appointment.
 * Persistencia en memoria; al cerrar la app se pierde. Para el proyecto final se reemplazará por BD.
 */
public class AppointmentRepository {

    // Almacenamiento en memoria usando LinkedHashMap para preservar orden de inserción
    private final Map<String, Appointment> storage = new LinkedHashMap<>();

    /**
     * Guarda o actualiza un turno. Si no tiene ID, se autogenera con UUID.
     *
     * @param appointment turno a guardar
     * @return turno guardado con su ID asignado
     */
    public Appointment save(Appointment appointment) {
        if (appointment == null) {
            throw new IllegalArgumentException("El turno no puede ser nulo");
        }
        if (appointment.getId() == null || appointment.getId().trim().isEmpty()) {
            appointment.setId(UUID.randomUUID().toString());
        }
        storage.put(appointment.getId(), appointment);
        return appointment;
    }

    /**
     * Busca un turno por su identificador.
     *
     * @param id identificador del turno
     * @return turno encontrado o null si no existe
     */
    public Appointment findById(String id) {
        if (id == null) {
            return null;
        }
        return storage.get(id);
    }

    /**
     * Retorna todos los turnos registrados.
     *
     * @return lista de turnos
     */
    public List<Appointment> findAll() {
        return new ArrayList<>(storage.values());
    }

    /**
     * Elimina un turno por su identificador.
     *
     * @param id identificador del turno a eliminar
     */
    public void delete(String id) {
        if (id != null) {
            storage.remove(id);
        }
    }
}
