package com.eventify.husb1.service;

import com.eventify.husb1.model.Venue;
import com.eventify.husb1.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para VenueService.
 *
 * Misma estrategia que EventServiceTest:
 *   - Sin contexto de Spring (@ExtendWith(MockitoExtension.class)).
 *   - VenueRepository mockeado con Mockito.
 *   - VenueService instanciado con @InjectMocks (inyección por constructor).
 *
 * Escenarios cubiertos:
 *   1. Camino Feliz   — registro exitoso con datos válidos.
 *   2. Camino Error   — nombre vacío rechazado antes del repositorio.
 *   3. Camino Error   — capacidad nula rechazada.
 *   4. Camino Error   — capacidad <= 0 rechazada.
 *   5. Caso de Borde  — listarTodos retorna lista vacía sin errores.
 *   6. Consulta       — listarTodos retorna los venues del repositorio.
 *   7. Búsqueda       — buscarPorId retorna el venue correcto.
 *   8. Búsqueda Error — buscarPorId lanza excepción si no existe.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VenueService — Tests Unitarios")
class VenueServiceTest {

    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private VenueService venueService;

    private Venue venueValido;

    @BeforeEach
    void setUp() {
        venueValido = Venue.builder()
                .nombre("Centro de Convenciones")
                .direccion("Calle 26 # 59-41")
                .capacidad(5000)
                .build();
    }

    // -------------------------------------------------------------------------
    // Escenario 1 — Registro exitoso (Camino Feliz)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Debe registrar un venue válido y retornar el objeto con ID asignado")
    void registrar_conDatosValidos_debeRetornarVenueConId() {
        // Arrange
        Venue venueGuardado = Venue.builder()
                .id(1L)
                .nombre(venueValido.getNombre())
                .direccion(venueValido.getDireccion())
                .capacidad(venueValido.getCapacidad())
                .build();

        when(venueRepository.save(any(Venue.class))).thenReturn(venueGuardado);

        // Act
        Venue resultado = venueService.registrar(venueValido);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("Centro de Convenciones");
        assertThat(resultado.getCapacidad()).isEqualTo(5000);

        verify(venueRepository, times(1)).save(any(Venue.class));
    }

    // -------------------------------------------------------------------------
    // Escenario 2 — Nombre vacío (Camino de Error)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el nombre está vacío")
    void registrar_conNombreVacio_debeLanzarExcepcion() {
        // Arrange
        Venue venueInvalido = Venue.builder()
                .nombre("")            // nombre vacío — debe ser rechazado
                .direccion("Calle 1")
                .capacidad(100)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> venueService.registrar(venueInvalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre");

        // El repositorio NUNCA debe ser invocado si la validación falla
        verify(venueRepository, never()).save(any(Venue.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el nombre es nulo")
    void registrar_conNombreNulo_debeLanzarExcepcion() {
        // Arrange
        Venue venueInvalido = Venue.builder()
                .nombre(null)
                .capacidad(100)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> venueService.registrar(venueInvalido))
                .isInstanceOf(IllegalArgumentException.class);

        verify(venueRepository, never()).save(any(Venue.class));
    }

    // -------------------------------------------------------------------------
    // Escenario 2 — Capacidad inválida (Camino de Error)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando la capacidad es cero")
    void registrar_conCapacidadCero_debeLanzarExcepcion() {
        // Arrange
        Venue venueInvalido = Venue.builder()
                .nombre("Venue sin capacidad")
                .capacidad(0)          // cero no es válido
                .build();

        // Act & Assert
        assertThatThrownBy(() -> venueService.registrar(venueInvalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("capacidad");

        verify(venueRepository, never()).save(any(Venue.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando la capacidad es negativa")
    void registrar_conCapacidadNegativa_debeLanzarExcepcion() {
        // Arrange
        Venue venueInvalido = Venue.builder()
                .nombre("Venue inválido")
                .capacidad(-50)        // negativo no es válido
                .build();

        // Act & Assert
        assertThatThrownBy(() -> venueService.registrar(venueInvalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("capacidad");

        verify(venueRepository, never()).save(any(Venue.class));
    }

    // -------------------------------------------------------------------------
    // Escenario 3 — Catálogo vacío (Caso de Borde)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay venues registrados")
    void listarTodos_sinVenues_debeRetornarListaVacia() {
        // Arrange
        when(venueRepository.findAll()).thenReturn(List.of());

        // Act
        List<Venue> resultado = venueService.listarTodos();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();

        verify(venueRepository, times(1)).findAll();
    }

    // -------------------------------------------------------------------------
    // Consulta con datos
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Debe retornar todos los venues del repositorio")
    void listarTodos_conVenues_debeRetornarLista() {
        // Arrange
        List<Venue> venuesEsperados = List.of(
                Venue.builder().id(1L).nombre("Venue A").capacidad(500).build(),
                Venue.builder().id(2L).nombre("Venue B").capacidad(1000).build()
        );
        when(venueRepository.findAll()).thenReturn(venuesEsperados);

        // Act
        List<Venue> resultado = venueService.listarTodos();

        // Assert
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Venue A");
        assertThat(resultado.get(1).getCapacidad()).isEqualTo(1000);
    }

    // -------------------------------------------------------------------------
    // buscarPorId — éxito
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Debe retornar el venue correcto dado un ID existente")
    void buscarPorId_conIdExistente_debeRetornarVenue() {
        // Arrange
        Venue venueEsperado = Venue.builder()
                .id(1L).nombre("Centro de Convenciones").capacidad(5000).build();

        when(venueRepository.findById(1L)).thenReturn(Optional.of(venueEsperado));

        // Act
        Venue resultado = venueService.buscarPorId(1L);

        // Assert
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("Centro de Convenciones");
    }

    // -------------------------------------------------------------------------
    // buscarPorId — no existe
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el ID no existe")
    void buscarPorId_conIdInexistente_debeLanzarExcepcion() {
        // Arrange
        when(venueRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> venueService.buscarPorId(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }
}
