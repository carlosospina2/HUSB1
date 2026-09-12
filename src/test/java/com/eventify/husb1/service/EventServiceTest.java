package com.eventify.husb1.service;

import com.eventify.husb1.model.Event;
import com.eventify.husb1.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para EventService.
 *
 * @ExtendWith(MockitoExtension.class):
 *   Activa Mockito sin levantar el contexto de Spring.
 *   El contexto NO se carga — las pruebas son puras y rápidas.
 *
 * @Mock:
 *   Crea un doble de prueba (mock) de EventRepository.
 *   Ninguna operación real de lista/memoria se ejecuta.
 *
 * @InjectMocks:
 *   Crea una instancia real de EventService inyectando el mock
 *   del repositorio por constructor — coherente con el patrón del proyecto.
 *
 * Escenarios cubiertos:
 *   1. Camino Feliz   — registro exitoso con datos válidos.
 *   2. Camino Error   — nombre vacío lanza excepción antes del repositorio.
 *   3. Camino Error   — fecha nula lanza excepción antes del repositorio.
 *   4. Caso de Borde  — listarTodos retorna lista vacía sin errores.
 *   5. Consulta       — listarTodos retorna los eventos del repositorio.
 *   6. Búsqueda       — buscarPorId retorna el evento correcto.
 *   7. Búsqueda Error — buscarPorId lanza excepción si no existe.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EventService — Tests Unitarios")
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    private Event eventoValido;

    @BeforeEach
    void setUp() {
        eventoValido = Event.builder()
                .nombre("Tech Conference 2025")
                .fecha(LocalDate.of(2025, 9, 15))
                .descripcion("Conferencia de tecnología")
                .venueId(1L)
                .build();
    }

    // -------------------------------------------------------------------------
    // Escenario 1 — Registro exitoso (Camino Feliz)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Debe registrar un evento válido y retornar el objeto con ID asignado")
    void registrar_conDatosValidos_debeRetornarEventoConId() {
        // Arrange — el mock simula que el repositorio asigna ID=1
        Event eventoGuardado = Event.builder()
                .id(1L)
                .nombre(eventoValido.getNombre())
                .fecha(eventoValido.getFecha())
                .descripcion(eventoValido.getDescripcion())
                .venueId(eventoValido.getVenueId())
                .build();

        when(eventRepository.save(any(Event.class))).thenReturn(eventoGuardado);

        // Act
        Event resultado = eventService.registrar(eventoValido);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("Tech Conference 2025");

        // Verificar que el repositorio fue invocado exactamente una vez
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    // -------------------------------------------------------------------------
    // Escenario 2 — Nombre vacío (Camino de Error)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el nombre está vacío")
    void registrar_conNombreVacio_debeLanzarExcepcion() {
        // Arrange
        Event eventoInvalido = Event.builder()
                .nombre("")          // nombre vacío — debe ser rechazado
                .fecha(LocalDate.now())
                .descripcion("Sin nombre")
                .build();

        // Act & Assert
        assertThatThrownBy(() -> eventService.registrar(eventoInvalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre");

        // El repositorio NUNCA debe ser invocado si la validación falla
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el nombre es nulo")
    void registrar_conNombreNulo_debeLanzarExcepcion() {
        // Arrange
        Event eventoInvalido = Event.builder()
                .nombre(null)
                .fecha(LocalDate.now())
                .build();

        // Act & Assert
        assertThatThrownBy(() -> eventService.registrar(eventoInvalido))
                .isInstanceOf(IllegalArgumentException.class);

        verify(eventRepository, never()).save(any(Event.class));
    }

    // -------------------------------------------------------------------------
    // Escenario 2 — Fecha nula (Camino de Error)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando la fecha es nula")
    void registrar_conFechaNula_debeLanzarExcepcion() {
        // Arrange
        Event eventoInvalido = Event.builder()
                .nombre("Evento sin fecha")
                .fecha(null)           // fecha nula — debe ser rechazada
                .build();

        // Act & Assert
        assertThatThrownBy(() -> eventService.registrar(eventoInvalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fecha");

        verify(eventRepository, never()).save(any(Event.class));
    }

    // -------------------------------------------------------------------------
    // Escenario 3 — Catálogo vacío (Caso de Borde)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay eventos registrados")
    void listarTodos_sinEventos_debeRetornarListaVacia() {
        // Arrange — el repositorio retorna lista vacía
        when(eventRepository.findAll()).thenReturn(List.of());

        // Act
        List<Event> resultado = eventService.listarTodos();

        // Assert — lista vacía, nunca null
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();

        verify(eventRepository, times(1)).findAll();
    }

    // -------------------------------------------------------------------------
    // Consulta con datos
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Debe retornar todos los eventos del repositorio")
    void listarTodos_conEventos_debeRetornarLista() {
        // Arrange
        List<Event> eventosEsperados = List.of(
                Event.builder().id(1L).nombre("Evento A").fecha(LocalDate.now()).build(),
                Event.builder().id(2L).nombre("Evento B").fecha(LocalDate.now()).build()
        );
        when(eventRepository.findAll()).thenReturn(eventosEsperados);

        // Act
        List<Event> resultado = eventService.listarTodos();

        // Assert
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Evento A");
        assertThat(resultado.get(1).getNombre()).isEqualTo("Evento B");
    }

    // -------------------------------------------------------------------------
    // buscarPorId — éxito
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Debe retornar el evento correcto dado un ID existente")
    void buscarPorId_conIdExistente_debeRetornarEvento() {
        // Arrange
        Event eventoEsperado = Event.builder()
                .id(1L).nombre("Tech Conference 2025")
                .fecha(LocalDate.of(2025, 9, 15)).build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(eventoEsperado));

        // Act
        Event resultado = eventService.buscarPorId(1L);

        // Assert
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("Tech Conference 2025");
    }

    // -------------------------------------------------------------------------
    // buscarPorId — no existe
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el ID no existe")
    void buscarPorId_conIdInexistente_debeLanzarExcepcion() {
        // Arrange
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> eventService.buscarPorId(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }
}
