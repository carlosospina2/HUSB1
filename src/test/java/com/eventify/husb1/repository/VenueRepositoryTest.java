package com.eventify.husb1.repository;

import com.eventify.husb1.model.Venue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Pruebas de integración para VenueRepository usando @DataJpaTest.
 *
 * @DataJpaTest:
 *   - Levanta solo el contexto JPA (Hibernate + H2 en memoria).
 *   - NO carga controllers, services ni beans de la aplicación completa.
 *   - Cada test corre en una transacción que se revierte al terminar,
 *     garantizando aislamiento total entre pruebas.
 *
 * Escenarios cubiertos:
 *   1. save()         → la entidad se persiste y obtiene ID.
 *   2. findById()     → recupera la entidad guardada.
 *   3. findAll()      → retorna todos los registros.
 *   4. Paginación     → page=0, size=1 retorna exactamente 1 resultado.
 *   5. Derived Query  → findByNombreContainingIgnoreCase busca correctamente.
 *   6. update (save)  → modificar y guardar actualiza el registro.
 *   7. deleteById()   → el registro ya no existe tras el borrado.
 *   8. existsById()   → true cuando existe, false cuando no.
 */
@DataJpaTest
@DisplayName("VenueRepository — @DataJpaTest")
class VenueRepositoryTest {

    @Autowired
    private VenueRepository venueRepository;

    @BeforeEach
    void limpiar() {
        venueRepository.deleteAll();
    }

    // -------------------------------------------------------------------------
    // 1. save → persiste y genera ID
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("save() debe persistir el venue y asignar un ID")
    void save_debeGenerarId() {
        Venue venue = Venue.builder()
                .nombre("Auditorio Nacional")
                .direccion("Av. Reforma 50")
                .capacidad(3000)
                .build();

        Venue guardado = venueRepository.save(venue);

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getId()).isGreaterThan(0L);
        assertThat(guardado.getNombre()).isEqualTo("Auditorio Nacional");
    }

    // -------------------------------------------------------------------------
    // 2. findById → recupera lo guardado
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findById() debe retornar el venue guardado")
    void findById_debeRetornarVenue() {
        Venue guardado = venueRepository.save(Venue.builder()
                .nombre("Coliseo Live")
                .direccion("Calle 63 # 24-09")
                .capacidad(10000)
                .build());

        Optional<Venue> resultado = venueRepository.findById(guardado.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Coliseo Live");
        assertThat(resultado.get().getCapacidad()).isEqualTo(10000);
    }

    // -------------------------------------------------------------------------
    // 3. findById → vacío cuando no existe
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findById() debe retornar Optional vacío para ID inexistente")
    void findById_conIdInexistente_debeRetornarVacio() {
        Optional<Venue> resultado = venueRepository.findById(9999L);
        assertThat(resultado).isEmpty();
    }

    // -------------------------------------------------------------------------
    // 4. findAll + count
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findAll() debe retornar todos los venues guardados")
    void findAll_debeRetornarTodos() {
        venueRepository.save(Venue.builder().nombre("Venue A").capacidad(500).build());
        venueRepository.save(Venue.builder().nombre("Venue B").capacidad(800).build());
        venueRepository.save(Venue.builder().nombre("Venue C").capacidad(1200).build());

        assertThat(venueRepository.findAll()).hasSize(3);
        assertThat(venueRepository.count()).isEqualTo(3);
    }

    // -------------------------------------------------------------------------
    // 5. Paginación: Escenario 3
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findAll(Pageable) debe retornar exactamente size=1 con metadatos correctos")
    void findAll_paginado_debeRetornarPaginaCorrecta() {
        // Guardar 3 venues
        venueRepository.save(Venue.builder().nombre("Alpha").capacidad(100).build());
        venueRepository.save(Venue.builder().nombre("Beta").capacidad(200).build());
        venueRepository.save(Venue.builder().nombre("Gamma").capacidad(300).build());

        // Solicitar página 0, tamaño 1, ordenado por nombre ASC
        PageRequest pageable = PageRequest.of(0, 1, Sort.by("nombre").ascending());
        Page<Venue> pagina = venueRepository.findAll(pageable);

        assertThat(pagina.getContent()).hasSize(1);          // exactamente 1 en esta página
        assertThat(pagina.getTotalElements()).isEqualTo(3);   // total en BD
        assertThat(pagina.getTotalPages()).isEqualTo(3);      // 3 páginas de tamaño 1
        assertThat(pagina.getContent().get(0).getNombre()).isEqualTo("Alpha"); // orden ASC
    }

    // -------------------------------------------------------------------------
    // 6. Derived Query — findByNombreContainingIgnoreCase
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByNombreContainingIgnoreCase debe encontrar coincidencias parciales")
    void findByNombreContaining_debeEncontrarCoincidencias() {
        venueRepository.save(Venue.builder().nombre("Teatro Nacional").capacidad(2000).build());
        venueRepository.save(Venue.builder().nombre("teatro mayor").capacidad(1800).build());
        venueRepository.save(Venue.builder().nombre("Auditorio Central").capacidad(500).build());

        PageRequest pageable = PageRequest.of(0, 10);
        Page<Venue> resultado = venueRepository
                .findByNombreContainingIgnoreCase("teatro", pageable);

        // Debe encontrar los 2 venues con "teatro" (mayúsculas o minúsculas)
        assertThat(resultado.getTotalElements()).isEqualTo(2);
        assertThat(resultado.getContent())
                .extracting(Venue::getNombre)
                .anyMatch(n -> n.toLowerCase().contains("teatro"));
    }

    @Test
    @DisplayName("findByNombreContainingIgnoreCase debe retornar vacío si no hay coincidencias")
    void findByNombreContaining_sinCoincidencias_debeRetornarVacio() {
        venueRepository.save(Venue.builder().nombre("Auditorio Norte").capacidad(400).build());

        Page<Venue> resultado = venueRepository
                .findByNombreContainingIgnoreCase("XXXXXX", PageRequest.of(0, 10));

        assertThat(resultado.getContent()).isEmpty();
        assertThat(resultado.getTotalElements()).isZero();
    }

    // -------------------------------------------------------------------------
    // 7. Update (save sobre entidad existente)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("save() sobre entidad existente debe actualizar los campos")
    void save_sobreExistente_debeActualizar() {
        Venue guardado = venueRepository.save(
                Venue.builder().nombre("Nombre Original").capacidad(100).build());

        guardado.setNombre("Nombre Actualizado");
        guardado.setCapacidad(999);
        Venue actualizado = venueRepository.save(guardado);

        assertThat(actualizado.getId()).isEqualTo(guardado.getId()); // mismo ID
        assertThat(actualizado.getNombre()).isEqualTo("Nombre Actualizado");
        assertThat(actualizado.getCapacidad()).isEqualTo(999);
        assertThat(venueRepository.count()).isEqualTo(1); // no se creó uno nuevo
    }

    // -------------------------------------------------------------------------
    // 8. deleteById — Escenario 4
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deleteById() debe eliminar el venue; existsById() retorna false después")
    void deleteById_debeEliminarVenue() {
        Venue guardado = venueRepository.save(
                Venue.builder().nombre("Venue a eliminar").capacidad(50).build());
        Long id = guardado.getId();

        assertThat(venueRepository.existsById(id)).isTrue();

        venueRepository.deleteById(id);

        assertThat(venueRepository.existsById(id)).isFalse();
        assertThat(venueRepository.findById(id)).isEmpty();
    }
}
