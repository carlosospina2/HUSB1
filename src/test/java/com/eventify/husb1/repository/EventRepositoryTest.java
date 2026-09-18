package com.eventify.husb1.repository;

import com.eventify.husb1.model.Event;
import com.eventify.husb1.model.Venue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Pruebas de integración para EventRepository usando @DataJpaTest.
 *
 * Se inyecta también VenueRepository para crear venues de apoyo
 * que sirvan de FK en los eventos (venueId real en BD).
 *
 * Escenarios cubiertos:
 *   1. save()                         → persiste y genera ID.
 *   2. findById()                     → recupera evento guardado.
 *   3. findById() ID inexistente      → Optional vacío.
 *   4. Paginación (Escenario 3)       → page=0, size=2 de 5 registros.
 *   5. findByNombreContaining         → búsqueda parcial case-insensitive.
 *   6. findByVenueId                  → filtra por venue correctamente.
 *   7. Update via save                → modifica campos de evento existente.
 *   8. deleteById (Escenario 4)       → elimina y confirma ausencia.
 */
@DataJpaTest
@DisplayName("EventRepository — @DataJpaTest")
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    private Venue venueA;
    private Venue venueB;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        venueRepository.deleteAll();

        venueA = venueRepository.save(
                Venue.builder().nombre("Venue A").capacidad(1000).build());
        venueB = venueRepository.save(
                Venue.builder().nombre("Venue B").capacidad(500).build());
    }

    // -------------------------------------------------------------------------
    // 1. save → persiste y genera ID
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("save() debe persistir el evento y asignar un ID generado por la BD")
    void save_debeGenerarId() {
        Event event = Event.builder()
                .nombre("Tech Summit 2025")
                .fecha(LocalDate.of(2025, 9, 20))
                .descripcion("Cumbre de tecnología")
                .venueId(venueA.getId())
                .build();

        Event guardado = eventRepository.save(event);

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getId()).isGreaterThan(0L);
        assertThat(guardado.getNombre()).isEqualTo("Tech Summit 2025");
    }

    // -------------------------------------------------------------------------
    // 2. findById → recupera el evento guardado
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findById() debe retornar el evento persistido")
    void findById_debeRetornarEvento() {
        Event guardado = eventRepository.save(Event.builder()
                .nombre("Feria del Libro")
                .fecha(LocalDate.of(2025, 4, 23))
                .venueId(venueA.getId())
                .build());

        Optional<Event> resultado = eventRepository.findById(guardado.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Feria del Libro");
        assertThat(resultado.get().getVenueId()).isEqualTo(venueA.getId());
    }

    // -------------------------------------------------------------------------
    // 3. findById → vacío para ID inexistente
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findById() debe retornar Optional vacío para ID 9999")
    void findById_conIdInexistente_debeRetornarVacio() {
        assertThat(eventRepository.findById(9999L)).isEmpty();
    }

    // -------------------------------------------------------------------------
    // 4. Paginación — Escenario 3
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findAll(Pageable) con page=0 size=2 debe retornar 2 de 5 con metadatos correctos")
    void findAll_paginado_debeRetornarPaginaCorrecta() {
        // Insertar 5 eventos
        for (int i = 1; i <= 5; i++) {
            eventRepository.save(Event.builder()
                    .nombre("Evento " + i)
                    .fecha(LocalDate.of(2025, i, 10))
                    .venueId(venueA.getId())
                    .build());
        }

        PageRequest pageable = PageRequest.of(0, 2, Sort.by("nombre").ascending());
        Page<Event> pagina = eventRepository.findAll(pageable);

        assertThat(pagina.getContent()).hasSize(2);        // 2 en esta página
        assertThat(pagina.getTotalElements()).isEqualTo(5); // 5 en total en BD
        assertThat(pagina.getTotalPages()).isEqualTo(3);    // ceil(5/2) = 3 páginas
        assertThat(pagina.getNumber()).isEqualTo(0);        // es la página 0
    }

    // -------------------------------------------------------------------------
    // 5. Derived Query — findByNombreContainingIgnoreCase
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByNombreContainingIgnoreCase debe encontrar coincidencias parciales")
    void findByNombreContaining_debeEncontrarCoincidencias() {
        eventRepository.save(Event.builder()
                .nombre("Conferencia de Java").fecha(LocalDate.now()).venueId(venueA.getId()).build());
        eventRepository.save(Event.builder()
                .nombre("CONFERENCIA de Spring").fecha(LocalDate.now()).venueId(venueA.getId()).build());
        eventRepository.save(Event.builder()
                .nombre("Festival de Música").fecha(LocalDate.now()).venueId(venueB.getId()).build());

        Page<Event> resultado = eventRepository
                .findByNombreContainingIgnoreCase("conferencia", PageRequest.of(0, 10));

        // Debe encontrar 2 (mayúsculas/minúsculas indistinto)
        assertThat(resultado.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByNombreContainingIgnoreCase debe retornar vacío sin coincidencias")
    void findByNombreContaining_sinCoincidencias_debeRetornarVacio() {
        eventRepository.save(Event.builder()
                .nombre("Evento Normal").fecha(LocalDate.now()).venueId(venueA.getId()).build());

        Page<Event> resultado = eventRepository
                .findByNombreContainingIgnoreCase("ZZZZZ", PageRequest.of(0, 10));

        assertThat(resultado.getContent()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // 6. Derived Query — findByVenueId
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByVenueId debe retornar solo los eventos del venue solicitado")
    void findByVenueId_debeRetornarEventosDelVenue() {
        eventRepository.save(Event.builder()
                .nombre("Evento Venue A - 1").fecha(LocalDate.now()).venueId(venueA.getId()).build());
        eventRepository.save(Event.builder()
                .nombre("Evento Venue A - 2").fecha(LocalDate.now()).venueId(venueA.getId()).build());
        eventRepository.save(Event.builder()
                .nombre("Evento Venue B - 1").fecha(LocalDate.now()).venueId(venueB.getId()).build());

        List<Event> eventosA = eventRepository.findByVenueId(venueA.getId());
        List<Event> eventosB = eventRepository.findByVenueId(venueB.getId());

        assertThat(eventosA).hasSize(2);
        assertThat(eventosB).hasSize(1);
        assertThat(eventosA).extracting(Event::getVenueId)
                .containsOnly(venueA.getId());
    }

    // -------------------------------------------------------------------------
    // 7. Update via save
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("save() sobre entidad existente debe actualizar los campos sin crear duplicado")
    void save_sobreExistente_debeActualizar() {
        Event guardado = eventRepository.save(Event.builder()
                .nombre("Nombre Original")
                .fecha(LocalDate.of(2025, 1, 1))
                .venueId(venueA.getId())
                .build());

        guardado.setNombre("Nombre Actualizado");
        guardado.setFecha(LocalDate.of(2025, 12, 31));
        Event actualizado = eventRepository.save(guardado);

        assertThat(actualizado.getId()).isEqualTo(guardado.getId());
        assertThat(actualizado.getNombre()).isEqualTo("Nombre Actualizado");
        assertThat(actualizado.getFecha()).isEqualTo(LocalDate.of(2025, 12, 31));
        assertThat(eventRepository.count()).isEqualTo(1); // no se creó duplicado
    }

    // -------------------------------------------------------------------------
    // 8. deleteById — Escenario 4
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deleteById() debe eliminar el evento; findById() retorna vacío después")
    void deleteById_debeEliminarEvento() {
        Event guardado = eventRepository.save(Event.builder()
                .nombre("Evento a eliminar")
                .fecha(LocalDate.now())
                .venueId(venueA.getId())
                .build());
        Long id = guardado.getId();

        assertThat(eventRepository.existsById(id)).isTrue();

        eventRepository.deleteById(id);

        assertThat(eventRepository.existsById(id)).isFalse();
        assertThat(eventRepository.findById(id)).isEmpty();
    }
}
