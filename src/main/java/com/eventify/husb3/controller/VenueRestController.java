package com.eventify.husb3.controller;

import com.eventify.husb3.model.Venue;
import com.eventify.husb3.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API REST para Venue — solo JSON, bajo /api/venues.
 *
 * Separación de rutas:
 *   /admin/venues/** → vistas Thymeleaf (@Controller)
 *   /api/venues/**   → API REST JSON  (@RestController) ← este archivo
 */
@RestController
@RequestMapping("/api/venues")
@Tag(name = "Venues API", description = "CRUD REST de Venues (JSON)")
public class VenueRestController {

    private final VenueService venueService;

    public VenueRestController(VenueService venueService) {
        this.venueService = venueService;
    }

    @Operation(summary = "Registrar un nuevo venue")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Venue creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Venue venue) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(venueService.registrar(venue));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @Operation(summary = "Listar venues paginados")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping
    public ResponseEntity<Page<Venue>> listar(
            @RequestParam(required = false) String nombre,
            @PageableDefault(size = 10, sort = "nombre", direction = Sort.Direction.ASC)
            Pageable pageable) {
        Page<Venue> pagina = (nombre != null && !nombre.isBlank())
                ? venueService.buscarPorNombre(nombre, pageable)
                : venueService.listarPaginado(pageable);
        return ResponseEntity.ok(pagina);
    }

    @Operation(summary = "Buscar venue por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado"),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Venue> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(venueService.buscarPorId(id));
    }

    @Operation(summary = "Actualizar un venue existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @RequestBody Venue datos) {
        try {
            return ResponseEntity.ok(venueService.actualizar(id, datos));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @Operation(summary = "Eliminar un venue")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminado"),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        venueService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
