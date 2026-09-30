package com.eventify.husb3.controller;

import com.eventify.husb3.model.Venue;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests MockMvc para AdminVenueController.
 *
 * Misma estrategia que AdminEventControllerTest:
 *   @WebMvcTest carga solo la capa web.
 *   @MockBean reemplaza VenueService con un doble Mockito.
 *
 * Escenarios cubiertos:
 *   1. GET /admin/venues         → 200 OK, vista list, modelo con "venues".
 *   2. GET /admin/venues vacío   → 200 OK, lista vacía.
 *   3. GET /admin/venues/nuevo   → 200 OK, vista form, "venue" sin ID.
 *   4. POST /admin/venues válido → redirect /admin/venues (PRG).
 *   5. POST /admin/venues inválido → 200 OK, formulario con "error".
 *   6. GET /admin/venues/{id}/editar → 200 OK, venue cargado.
 *   7. POST /admin/venues/{id}/eliminar → redirect /admin/venues.
 */
@WebMvcTest(AdminVenueController.class)
@DisplayName("AdminVenueController — Tests MockMvc")
class AdminVenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VenueService venueService;

    // -------------------------------------------------------------------------
    // 1. GET /admin/venues — listado con datos
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /admin/venues debe retornar 200, vista list y atributo 'venues'")
    void listar_debeRetornar200YVistaList() throws Exception {
        List<Venue> venues = List.of(
                Venue.builder().id(1L).nombre("Venue A").capacidad(500).build(),
                Venue.builder().id(2L).nombre("Venue B").capacidad(1000).build()
        );
        when(venueService.listarTodos()).thenReturn(venues);

        mockMvc.perform(get("/admin/venues"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/venues/list"))
                .andExpect(model().attributeExists("venues"))
                .andExpect(model().attribute("venues", hasSize(2)));
    }

    // -------------------------------------------------------------------------
    // 2. GET /admin/venues — catálogo vacío
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /admin/venues vacío debe retornar 200 con lista vacía en el modelo")
    void listar_sinVenues_debeRetornar200ConListaVacia() throws Exception {
        when(venueService.listarTodos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/venues"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/venues/list"))
                .andExpect(model().attributeExists("venues"))
                .andExpect(model().attribute("venues", hasSize(0)));
    }

    // -------------------------------------------------------------------------
    // 3. GET /admin/venues/nuevo — formulario vacío
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /admin/venues/nuevo debe retornar 200, vista form y 'venue' sin ID")
    void mostrarFormulario_debeRetornarVistaFormConVenueVacio() throws Exception {
        mockMvc.perform(get("/admin/venues/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/venues/form"))
                .andExpect(model().attributeExists("venue"))
                .andExpect(model().attribute("venue",
                        hasProperty("id", nullValue())));
    }

    // -------------------------------------------------------------------------
    // 4. POST /admin/venues — registro exitoso → PRG
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /admin/venues con datos válidos debe redirigir al listado")
    void guardar_conDatosValidos_debeRedirigir() throws Exception {
        Venue guardado = Venue.builder().id(1L).nombre("Nuevo Venue").capacidad(300).build();
        when(venueService.registrar(any(Venue.class))).thenReturn(guardado);

        mockMvc.perform(post("/admin/venues")
                        .param("nombre", "Nuevo Venue")
                        .param("direccion", "Calle 1 # 2-3")
                        .param("capacidad", "300"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/venues"));

        verify(venueService, times(1)).registrar(any(Venue.class));
    }

    // -------------------------------------------------------------------------
    // 5. POST /admin/venues — capacidad inválida → formulario con error
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /admin/venues con capacidad inválida debe volver al formulario con error")
    void guardar_conCapacidadInvalida_debeVolverAlFormularioConError() throws Exception {
        when(venueService.registrar(any(Venue.class)))
                .thenThrow(new IllegalArgumentException("La capacidad debe ser mayor que cero."));

        mockMvc.perform(post("/admin/venues")
                        .param("nombre", "Venue sin capacidad")
                        .param("capacidad", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/venues/form"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error",
                        containsString("capacidad")));
    }

    // -------------------------------------------------------------------------
    // 6. GET /admin/venues/{id}/editar — formulario pre-rellenado
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /admin/venues/{id}/editar debe retornar 200 con el venue en el modelo")
    void mostrarFormularioEdicion_debeRetornarVenueEnModelo() throws Exception {
        Venue venue = Venue.builder().id(1L).nombre("Venue Existente").capacidad(2000).build();
        when(venueService.buscarPorId(1L)).thenReturn(venue);

        mockMvc.perform(get("/admin/venues/1/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/venues/form"))
                .andExpect(model().attributeExists("venue"))
                .andExpect(model().attribute("venue",
                        hasProperty("id", is(1L))))
                .andExpect(model().attribute("venue",
                        hasProperty("nombre", is("Venue Existente"))));
    }

    // -------------------------------------------------------------------------
    // 7. POST /admin/venues/{id}/eliminar → PRG
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /admin/venues/{id}/eliminar debe redirigir al listado")
    void eliminar_debeRedirigirAlListado() throws Exception {
        doNothing().when(venueService).eliminar(1L);

        mockMvc.perform(post("/admin/venues/1/eliminar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/venues"));

        verify(venueService, times(1)).eliminar(1L);
    }
}
