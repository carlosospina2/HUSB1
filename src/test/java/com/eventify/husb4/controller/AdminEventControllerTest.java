package com.eventify.husb4.controller;

import com.eventify.husb4.dto.EventSummaryDTO;
import com.eventify.husb4.model.Category;
import com.eventify.husb4.model.Venue;
import com.eventify.husb4.service.CategoryService;
import com.eventify.husb4.service.EventService;
import com.eventify.husb4.service.VenueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests MockMvc para AdminEventController.
 *
 * @WebMvcTest → solo capa web, sin BD, sin Flyway.
 * @MockBean   → reemplaza EventService, VenueService y CategoryService.
 *
 * Escenarios cubiertos:
 *   1. GET /admin/events → 200, vista list, atributos en Model.
 *   2. GET /admin/events vacío → 200, lista vacía (Escenario 2).
 *   3. GET filtrado por city → 200, filtro en modelo.
 *   4. GET /admin/events/nuevo → 200, vista form, event sin ID.
 *   5. POST válido → redirect (PRG, Escenario 3).
 *   6. POST inválido → 200, formulario con error.
 *   7. GET /{id}/editar → 200, vista form con evento cargado.
 *   8. POST /{id}/eliminar → redirect (Soft Delete).
 */
@WebMvcTest(AdminEventController.class)
@DisplayName("AdminEventController — Tests MockMvc")
class AdminEventControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean EventService    eventService;
    @MockBean VenueService    venueService;
    @MockBean CategoryService categoryService;

    // -------------------------------------------------------------------------
    // 1. GET /admin/events — listado con datos (Escenario 1)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /admin/events debe retornar 200, vista list y atributo 'events'")
    void listar_conDatos_debeRetornar200() throws Exception {
        List<EventSummaryDTO> dtos = List.of(
                new EventSummaryDTO(1L, "Evento A", LocalDate.of(2025,9,1), "Venue X", "Bogotá"),
                new EventSummaryDTO(2L, "Evento B", LocalDate.of(2025,10,1), "Venue Y", "Medellín")
        );
        when(eventService.search(isNull(), isNull(), eq(0), anyInt()))
                .thenReturn(new SliceImpl<>(dtos));
        when(categoryService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/events"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/events/list"))
                .andExpect(model().attributeExists("events"))
                .andExpect(model().attribute("events", hasSize(2)));
    }

    // -------------------------------------------------------------------------
    // 2. GET /admin/events — catálogo vacío (Escenario 2)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /admin/events sin datos retorna 200 con lista vacía en modelo")
    void listar_vacio_debeRetornarListaVacia() throws Exception {
        when(eventService.search(isNull(), isNull(), eq(0), anyInt()))
                .thenReturn(new SliceImpl<>(Collections.emptyList()));
        when(categoryService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/events"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/events/list"))
                .andExpect(model().attribute("events", hasSize(0)));
    }

    // -------------------------------------------------------------------------
    // 3. GET /admin/events?city=Bogotá — filtro en modelo (Escenario 3 y 6)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /admin/events?city=Bogotá debe pasar el filtro al modelo")
    void listar_conFiltroCity_debePasarFiltroAlModelo() throws Exception {
        when(eventService.search(eq("Bogotá"), isNull(), eq(0), anyInt()))
                .thenReturn(new SliceImpl<>(List.of(
                        new EventSummaryDTO(1L, "Tech Summit", LocalDate.now(), "CC Bogotá", "Bogotá")
                )));
        when(categoryService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/events").param("city", "Bogotá"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/events/list"))
                // Escenario 3: el filtro persiste en el modelo para los links de navegación
                .andExpect(model().attribute("city", "Bogotá"))
                .andExpect(model().attribute("events", hasSize(1)));
    }

    // -------------------------------------------------------------------------
    // 4. GET /admin/events/nuevo — formulario vacío (Escenario 4)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /admin/events/nuevo retorna 200, vista form y 'event' sin ID")
    void formulario_nuevo_debeRetornar200ConEventoVacio() throws Exception {
        when(venueService.findAll()).thenReturn(List.of(
                Venue.builder().id(1L).name("Venue Test").city("Bogotá").capacity(100).build()
        ));
        when(categoryService.findAll()).thenReturn(List.of(
                Category.builder().id(1L).name("Conciertos").build()
        ));

        mockMvc.perform(get("/admin/events/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/events/form"))
                // Escenario 4: el modelo contiene los atributos para la vista
                .andExpect(model().attributeExists("event"))
                .andExpect(model().attributeExists("venues"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attribute("event",
                        hasProperty("id", nullValue())));
    }

    // -------------------------------------------------------------------------
    // 5. POST /admin/events — registro exitoso → PRG (Escenario 3)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /admin/events válido debe guardar y redirigir al listado (PRG)")
    void guardar_valido_debeRedirigir() throws Exception {
        when(eventService.save(anyString(), any(), anyString(), anyLong(), any()))
                .thenReturn(com.eventify.husb4.model.Event.builder()
                        .id(1L).name("Nuevo").date(LocalDate.now())
                        .venue(Venue.builder().id(1L).name("V").city("Bogotá").capacity(100).build())
                        .build());

        mockMvc.perform(post("/admin/events")
                        .param("name",        "Nuevo Evento")
                        .param("date",        "2025-12-01")
                        .param("description", "Descripción")
                        .param("venueId",     "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/events"));

        verify(eventService, times(1))
                .save(anyString(), any(), anyString(), anyLong(), any());
    }

    // -------------------------------------------------------------------------
    // 6. POST /admin/events — validación falla → formulario con error
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /admin/events con nombre vacío retorna formulario con 'error'")
    void guardar_invalido_debeVolverAlFormulario() throws Exception {
        when(eventService.save(anyString(), any(), any(), anyLong(), any()))
                .thenThrow(new IllegalArgumentException("El nombre del evento es obligatorio."));
        when(venueService.findAll()).thenReturn(List.of());
        when(categoryService.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/admin/events")
                        .param("name",    "")
                        .param("date",    "2025-12-01")
                        .param("venueId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/events/form"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", containsString("obligatorio")));
    }

    // -------------------------------------------------------------------------
    // 7. GET /admin/events/{id}/editar — formulario con datos
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /admin/events/{id}/editar retorna 200 con el evento en el modelo")
    void editar_debeRetornarEventoEnModelo() throws Exception {
        com.eventify.husb4.model.Event event = com.eventify.husb4.model.Event.builder()
                .id(1L).name("Evento Existente").date(LocalDate.of(2025, 9, 15))
                .venue(Venue.builder().id(1L).name("V").city("Bogotá").capacity(100).build())
                .categories(new java.util.HashSet<>())
                .build();

        when(eventService.findWithDetailsById(1L)).thenReturn(event);
        when(venueService.findAll()).thenReturn(List.of());
        when(categoryService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/events/1/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/events/form"))
                .andExpect(model().attributeExists("event"))
                .andExpect(model().attribute("event",
                        hasProperty("id", is(1L))))
                .andExpect(model().attribute("event",
                        hasProperty("name", is("Evento Existente"))));
    }

    // -------------------------------------------------------------------------
    // 8. POST /admin/events/{id}/eliminar — Soft Delete → redirect
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /admin/events/{id}/eliminar debe hacer softDelete y redirigir")
    void softDelete_debeRedirigir() throws Exception {
        doNothing().when(eventService).softDelete(1L);

        mockMvc.perform(post("/admin/events/1/eliminar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/events"));

        // Verifica que se llamó softDelete (no delete físico)
        verify(eventService, times(1)).softDelete(1L);
    }
}
