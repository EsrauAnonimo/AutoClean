package com.autoclean.repository;

import com.autoclean.model.Supply;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para la gestión de insumos ({@code insumos}).
 */
public interface SupplyRepository extends JpaRepository<Supply, UUID> {

    /**
     * Lista los insumos ordenados alfabéticamente por nombre.
     *
     * @return lista ordenada de insumos
     */
    List<Supply> findAllByOrderByNameAsc();
}

