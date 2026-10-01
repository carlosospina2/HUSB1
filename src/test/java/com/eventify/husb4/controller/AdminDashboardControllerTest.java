package com.eventify.husb4.controller;

import com.eventify.husb4.service.CategoryService;
import com.eventify.husb4.service.EventService;
import com.eventify.husb4.service.VenueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests MockMvc para AdminDashboardController.
 *
 * Escenario 4: verifica que el Model contiene los contadores necesarios para la vista.
 */
@WebMvcTest(AdminDashboardController.class)
@DisplayName("AdminDashboardController — Tests MockMvc")
class AdminDashboardControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean EventService    eventService;
    @MockBean VenueService    venueService;
    @MockBean CategoryService categoryService;

    @Test
    @DisplayName("GET /admin retorna 200, vista dashboard y contadores correctos")
    void dashboard_conDatos_retornaContadores() throws Exception {
        when(eventService.countActive()).thenReturn(190L);
        when(venueService.findAll()).thenReturn(List.of(
                com.eventify.husb4.model.Venue.builder().id(1L).name("V").city("B").capacity(100).build(),
                com.eventify.husb4.model.Venue.builder().id(2L).name("W").city("M").capacity(200).build()
        ));
        when(categoryService.findAll()).thenReturn(List.of(
                com.eventify.husb4.model.Category.builder().id(1L).name("Conciertos").build(),
                com.eventify.husb4.model.Category.builder().id(2L).name("Talleres").build(),
                com.eventify.husb4.model.Category.builder().id(3L).name("Deportes").build()
        ));

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                // Escenario 4: el Model tiene los atributos necesarios para la vista
                .andExpect(model().attributeExists("totalEvents"))
                .andExpect(model().attributeExists("totalVenues"))
                .andExpect(model().attributeExists("totalCategories"))
                .andExpect(model().attribute("totalEvents",     is(190L)))
                .andExpect(model().attribute("totalVenues",     is(2)))
                .andExpect(model().attribute("totalCategories", is(3)));
    }

    @Test
    @DisplayName("GET /admin sin datos retorna contadores en 0")
    void dashboard_sinDatos_retornaContadoresEnCero() throws Exception {
        when(eventService.countActive()).thenReturn(0L);
        when(venueService.findAll()).thenReturn(Collections.emptyList());
        when(categoryService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attribute("totalEvents",     is(0L)))
                .andExpect(model().attribute("totalVenues",     is(0)))
                .andExpect(model().attribute("totalCategories", is(0)));
    }
}
