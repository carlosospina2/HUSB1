package com.eventify.husb4.controller;

import com.eventify.husb4.model.Venue;
import com.eventify.husb4.service.VenueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests MockMvc para AdminVenueController.
 * Escenarios: listado, listado vacío, formulario, guardar, error, editar, eliminar.
 */
@WebMvcTest(AdminVenueController.class)
@DisplayName("AdminVenueController — Tests MockMvc")
class AdminVenueControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean  VenueService venueService;

    @Test
    @DisplayName("GET /admin/venues retorna 200, vista list y atributo 'venues'")
    void listar_retorna200() throws Exception {
        when(venueService.findAll()).thenReturn(List.of(
                Venue.builder().id(1L).name("Venue A").city("Bogotá").capacity(500).build(),
                Venue.builder().id(2L).name("Venue B").city("Medellín").capacity(800).build()
        ));

        mockMvc.perform(get("/admin/venues"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/venues/list"))
                .andExpect(model().attributeExists("venues"))
                .andExpect(model().attribute("venues", hasSize(2)));
    }

    @Test
    @DisplayName("GET /admin/venues vacío retorna lista vacía en modelo")
    void listar_vacio_retornaListaVacia() throws Exception {
        when(venueService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/venues"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("venues", hasSize(0)));
    }

    @Test
    @DisplayName("GET /admin/venues/nuevo retorna formulario con venue sin ID")
    void formulario_retornaVenueVacio() throws Exception {
        mockMvc.perform(get("/admin/venues/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/venues/form"))
                .andExpect(model().attributeExists("venue"))
                .andExpect(model().attribute("venue", hasProperty("id", nullValue())));
    }

    @Test
    @DisplayName("POST /admin/venues válido redirige al listado (PRG)")
    void guardar_valido_redirige() throws Exception {
        when(venueService.save(any(Venue.class))).thenReturn(
                Venue.builder().id(1L).name("Nuevo").city("Bogotá").capacity(200).build());

        mockMvc.perform(post("/admin/venues")
                        .param("name",     "Nuevo Venue")
                        .param("city",     "Bogotá")
                        .param("capacity", "200"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/venues"));
    }

    @Test
    @DisplayName("POST /admin/venues con ciudad vacía retorna formulario con error")
    void guardar_invalido_retornaError() throws Exception {
        when(venueService.save(any(Venue.class)))
                .thenThrow(new IllegalArgumentException("La ciudad del venue es obligatoria."));

        mockMvc.perform(post("/admin/venues")
                        .param("name",     "Venue sin ciudad")
                        .param("city",     "")
                        .param("capacity", "100"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/venues/form"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", containsString("ciudad")));
    }

    @Test
    @DisplayName("GET /admin/venues/{id}/editar retorna el venue en el modelo")
    void editar_retornaVenue() throws Exception {
        when(venueService.findById(1L)).thenReturn(
                Venue.builder().id(1L).name("Venue Test").city("Cali").capacity(1000).build());

        mockMvc.perform(get("/admin/venues/1/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/venues/form"))
                .andExpect(model().attribute("venue", hasProperty("city", is("Cali"))));
    }

    @Test
    @DisplayName("POST /admin/venues/{id}/eliminar redirige al listado")
    void eliminar_redirige() throws Exception {
        doNothing().when(venueService).delete(1L);

        mockMvc.perform(post("/admin/venues/1/eliminar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/venues"));

        verify(venueService, times(1)).delete(1L);
    }
}
