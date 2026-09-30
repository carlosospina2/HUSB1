package com.eventify.husb3.controller;

import com.eventify.husb3.service.EventService;
import com.eventify.husb3.service.VenueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests MockMvc para AdminDashboardController.
 *
 * Verifica:
 *   1. GET /admin → 200 OK, vista "admin/dashboard".
 *   2. Modelo contiene "totalEvents" y "totalVenues" correctos.
 *   3. Con BD vacía → contadores en 0.
 */
@WebMvcTest(AdminDashboardController.class)
@DisplayName("AdminDashboardController — Tests MockMvc")
class AdminDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private VenueService venueService;

    // -------------------------------------------------------------------------
    // 1. GET /admin con datos — Escenario 4 (Validación de Navegación)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /admin debe retornar 200, vista dashboard y contadores en el modelo")
    void dashboard_debeRetornar200YContadores() throws Exception {
        // Arrange
        when(eventService.listarTodos()).thenReturn(List.of(
                com.eventify.husb3.model.Event.builder().id(1L)
                        .nombre("E1").fecha(java.time.LocalDate.now()).build(),
                com.eventify.husb3.model.Event.builder().id(2L)
                        .nombre("E2").fecha(java.time.LocalDate.now()).build()
        ));
        when(venueService.listarTodos()).thenReturn(List.of(
                com.eventify.husb3.model.Venue.builder().id(1L)
                        .nombre("V1").capacidad(100).build()
        ));

        // Act & Assert
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                // Escenario 4: el Model contiene los atributos necesarios para la vista
                .andExpect(model().attributeExists("totalEvents"))
                .andExpect(model().attributeExists("totalVenues"))
                .andExpect(model().attribute("totalEvents", is(2)))
                .andExpect(model().attribute("totalVenues", is(1)));
    }

    // -------------------------------------------------------------------------
    // 2. GET /admin sin datos — contadores en 0
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /admin sin datos debe mostrar contadores en 0")
    void dashboard_sinDatos_debeRetornarContadoresEnCero() throws Exception {
        when(eventService.listarTodos()).thenReturn(Collections.emptyList());
        when(venueService.listarTodos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attribute("totalEvents", is(0)))
                .andExpect(model().attribute("totalVenues", is(0)));
    }
}
