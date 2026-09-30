package com.eventify.husb3.controller;

import com.eventify.husb3.model.Event;
import com.eventify.husb3.model.Venue;
import com.eventify.husb3.service.EventService;
import com.eventify.husb3.service.VenueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración de vistas para AdminEventController.
 *
 * @WebMvcTest(AdminEventController.class):
 *   Levanta solo la capa web (controllers, filtros, Thymeleaf).
 *   NO levanta el contexto completo de Spring ni la BD.
 *   Los services se reemplazan con @MockBean (Mockito).
 *
 * MockMvc:
 *   Simula peticiones HTTP sin servidor real.
 *   Permite verificar: status, nombre de vista, atributos del Model.
 *
 * Escenarios cubiertos:
 *   1. GET /admin/events         → 200 OK, vista "admin/events/list", modelo con "events".
 *   2. GET /admin/events vacío   → 200 OK, lista vacía en el modelo (Escenario 2).
 *   3. GET /admin/events/nuevo   → 200 OK, vista "admin/events/form", modelo con "event" vacío.
 *   4. POST /admin/events válido → redirect a /admin/events (Patrón PRG, Escenario 3).
 *   5. POST /admin/events inválido → 200 OK, vuelve al formulario con "error" en el modelo.
 *   6. GET /admin/events/{id}/editar → 200 OK, vista form con el evento cargado.
 *   7. POST /admin/events/{id}/eliminar → redirect a /admin/events.
 */
@WebMvcTest(AdminEventController.class)
@DisplayName("AdminEventController — Tests MockMvc")
class AdminEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private VenueService venueService;

    // -------------------------------------------------------------------------
    // 1. GET /admin/events — listado con datos (Escenario 1)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /admin/events debe retornar 200, vista list y atributo 'events'")
    void listar_debeRetornar200YVistaList() throws Exception {
        // Arrange — el service retorna 2 eventos
        List<Event> eventos = List.of(
                Event.builder().id(1L).nombre("Evento A")
                        .fecha(LocalDate.of(2025, 9, 1)).build(),
                Event.builder().id(2L).nombre("Evento B")
                        .fecha(LocalDate.of(2025, 10, 1)).build()
        );
        when(eventService.listarTodos()).thenReturn(eventos);
        when(venueService.listarTodos()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/admin/events"))
                // Escenario 4: status 200 OK
                .andExpect(status().isOk())
                // Nombre de la vista Thymeleaf
                .andExpect(view().name("admin/events/list"))
                // El Model contiene "events" con 2 elementos
                .andExpect(model().attributeExists("events"))
                .andExpect(model().attribute("events", hasSize(2)));
    }

    // -------------------------------------------------------------------------
    // 2. GET /admin/events — catálogo vacío (Escenario 2)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /admin/events con lista vacía debe retornar 200 y modelo con lista vacía")
    void listar_sinEventos_debeRetornar200ConListaVacia() throws Exception {
        // Arrange — repositorio vacío
        when(eventService.listarTodos()).thenReturn(Collections.emptyList());
        when(venueService.listarTodos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/events"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/events/list"))
                // El modelo tiene "events" pero como lista vacía
                .andExpect(model().attributeExists("events"))
                .andExpect(model().attribute("events", hasSize(0)));
    }

    // -------------------------------------------------------------------------
    // 3. GET /admin/events/nuevo — mostrar formulario vacío
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /admin/events/nuevo debe retornar 200, vista form y 'event' vacío en el modelo")
    void mostrarFormulario_debeRetornar200YVistaForm() throws Exception {
        when(venueService.listarTodos()).thenReturn(List.of());

        mockMvc.perform(get("/admin/events/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/events/form"))
                // El modelo debe contener un objeto "event" para th:object
                .andExpect(model().attributeExists("event"))
                // El objeto "event" nuevo no tiene ID
                .andExpect(model().attribute("event",
                        hasProperty("id", nullValue())));
    }

    // -------------------------------------------------------------------------
    // 4. POST /admin/events — registro exitoso → PRG redirect (Escenario 3)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /admin/events con datos válidos debe guardar y redirigir al listado")
    void guardar_conDatosValidos_debeRedirigirAlListado() throws Exception {
        // Arrange — el service guarda sin lanzar excepción
        Event eventoGuardado = Event.builder().id(1L).nombre("Nuevo Evento")
                .fecha(LocalDate.of(2025, 12, 1)).build();
        when(eventService.registrar(any(Event.class))).thenReturn(eventoGuardado);

        // Act & Assert
        mockMvc.perform(post("/admin/events")
                        .param("nombre", "Nuevo Evento")
                        .param("fecha", "2025-12-01")
                        .param("descripcion", "Descripción del evento"))
                // Patrón PRG: redirect con 302
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/events"));

        // Verificar que el service fue invocado exactamente una vez
        verify(eventService, times(1)).registrar(any(Event.class));
    }

    // -------------------------------------------------------------------------
    // 5. POST /admin/events — datos inválidos → vuelve al formulario con error
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /admin/events con nombre vacío debe volver al formulario con 'error'")
    void guardar_conNombreVacio_debeVolverAlFormularioConError() throws Exception {
        // Arrange — el service lanza excepción de validación
        when(eventService.registrar(any(Event.class)))
                .thenThrow(new IllegalArgumentException("El nombre del evento es obligatorio."));
        when(venueService.listarTodos()).thenReturn(List.of());

        mockMvc.perform(post("/admin/events")
                        .param("nombre", "")           // nombre vacío
                        .param("fecha", "2025-12-01"))
                // 200 OK — vuelve al formulario (no redirect)
                .andExpect(status().isOk())
                .andExpect(view().name("admin/events/form"))
                // El modelo tiene el atributo "error" con el mensaje
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error",
                        containsString("obligatorio")));
    }

    // -------------------------------------------------------------------------
    // 6. GET /admin/events/{id}/editar — formulario pre-rellenado
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /admin/events/{id}/editar debe retornar 200 con el evento cargado")
    void mostrarFormularioEdicion_debeRetornarEventoEnModelo() throws Exception {
        Event evento = Event.builder().id(1L).nombre("Evento Existente")
                .fecha(LocalDate.of(2025, 9, 15)).build();
        when(eventService.buscarPorId(1L)).thenReturn(evento);
        when(venueService.listarTodos()).thenReturn(List.of());

        mockMvc.perform(get("/admin/events/1/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/events/form"))
                .andExpect(model().attributeExists("event"))
                // El evento en el modelo tiene el ID correcto
                .andExpect(model().attribute("event",
                        hasProperty("id", is(1L))))
                .andExpect(model().attribute("event",
                        hasProperty("nombre", is("Evento Existente"))));
    }

    // -------------------------------------------------------------------------
    // 7. POST /admin/events/{id}/eliminar → PRG redirect
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /admin/events/{id}/eliminar debe eliminar y redirigir al listado")
    void eliminar_debeRedirigirAlListado() throws Exception {
        doNothing().when(eventService).eliminar(1L);

        mockMvc.perform(post("/admin/events/1/eliminar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/events"));

        verify(eventService, times(1)).eliminar(1L);
    }
}
