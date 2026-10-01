package com.eventify.husb4.repository;

import com.eventify.husb4.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para Category.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Búsqueda insensible a mayúsculas por nombre (parcial).
     * Escenario 6: "rock" encuentra "Conciertos de ROCK".
     */
    List<Category> findByNameContainingIgnoreCase(String name);

    Optional<Category> findByNameIgnoreCase(String name);
}
