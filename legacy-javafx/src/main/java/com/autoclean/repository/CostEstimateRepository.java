package com.autoclean.repository;

import com.autoclean.model.CostEstimate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Repositorio en memoria para la entidad CostEstimate.
 * Persistencia en memoria; al cerrar la app se pierde. Para el proyecto final se reemplazará por BD.
 */
public class CostEstimateRepository {

    // Almacenamiento en memoria usando LinkedHashMap para preservar orden de inserción
    private final Map<String, CostEstimate> storage = new LinkedHashMap<>();

    /**
     * Guarda o actualiza una estimación de costo. Si no tiene ID, se autogenera con UUID.
     *
     * @param costEstimate estimación a guardar
     * @return estimación guardada con su ID asignado
     */
    public CostEstimate save(CostEstimate costEstimate) {
        if (costEstimate == null) {
            throw new IllegalArgumentException("La estimación de costos no puede ser nula");
        }
        if (costEstimate.getId() == null || costEstimate.getId().trim().isEmpty()) {
            costEstimate.setId(UUID.randomUUID().toString());
        }
        storage.put(costEstimate.getId(), costEstimate);
        return costEstimate;
    }

    /**
     * Busca una estimación de costo por su identificador.
     *
     * @param id identificador de la estimación
     * @return estimación encontrada o null si no existe
     */
    public CostEstimate findById(String id) {
        if (id == null) {
            return null;
        }
        return storage.get(id);
    }

    /**
     * Retorna todas las estimaciones de costo registradas.
     *
     * @return lista de estimaciones
     */
    public List<CostEstimate> findAll() {
        return new ArrayList<>(storage.values());
    }

    /**
     * Elimina una estimación de costo por su identificador.
     *
     * @param id identificador de la estimación a eliminar
     */
    public void delete(String id) {
        if (id != null) {
            storage.remove(id);
        }
    }
}
