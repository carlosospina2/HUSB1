package com.eventify.husb4.controller;

import com.eventify.husb4.model.Venue;
import com.eventify.husb4.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API REST para Venue — JSON, bajo /api/venues.
 * Incluye campo city (nuevo en HU-SB4) en todos los endpoints.
 */
@RestController
@RequestMapping("/api/venues")
@Tag(name = "Venues API",
     description = "CRUD REST de Venues. El campo 'city' es obligatorio en HU-SB4.")
public class VenueRestController {

    private final VenueService venueService;

    public VenueRestController(VenueService venueService) {
        this.venueService = venueService;
    }

    @Operation(summary = "Registrar un nuevo venue",
               description = "El campo city es obligatorio. Se usa como filtro de búsqueda de eventos.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Venue creado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos (name, city o capacity)")
    })
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Venue venue) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(venueService.save(venue));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @Operation(summary = "Listar todos los venues")
    @ApiResponse(responseCode = "200", description = "Lista de venues")
    @GetMapping
    public ResponseEntity<List<Venue>> list(
            @Parameter(description = "Filtro opcional por ciudad (parcial, case-insensitive)")
            @RequestParam(required = false) String city) {

        List<Venue> result = (city != null && !city.isBlank())
                ? venueService.findByCity(city)
                : venueService.findAll();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Buscar venue por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Venue encontrado"),
        @ApiResponse(responseCode = "404", description = "Venue no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Venue> getById(@PathVariable Long id) {
        return ResponseEntity.ok(venueService.findById(id));
    }

    @Operation(summary = "Actualizar un venue")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Venue actualizado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Venue no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Venue venue) {
        try {
            return ResponseEntity.ok(venueService.update(id, venue));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @Operation(summary = "Eliminar un venue (DELETE físico)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Venue eliminado"),
        @ApiResponse(responseCode = "404", description = "Venue no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        venueService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
