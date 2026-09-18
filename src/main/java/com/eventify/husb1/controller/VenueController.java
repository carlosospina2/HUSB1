package com.eventify.husb1.controller;

import com.eventify.husb1.exception.ResourceNotFoundException;
import com.eventify.husb1.model.Venue;
import com.eventify.husb1.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
 * Controlador REST para Venue — CRUD completo con paginación.
 *
 * Endpoints:
 *   POST   /api/venues          → 201 Created
 *   GET    /api/venues          → 200 OK  (paginado: ?page=0&size=10&sort=nombre,asc)
 *   GET    /api/venues/{id}     → 200 OK  | 404 Not Found
 *   PUT    /api/venues/{id}     → 200 OK  | 404 Not Found | 400 Bad Request
 *   DELETE /api/venues/{id}     → 204 No Content | 404 Not Found
 */
@RestController
@RequestMapping("/api/venues")
@Tag(name = "Venues", description = "CRUD completo de Lugares con paginación y ordenamiento")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    // -------------------------------------------------------------------------
    // POST /api/venues — Crear
    // -------------------------------------------------------------------------

    @Operation(summary = "Registrar un nuevo venue",
               description = "Crea un venue y lo persiste en la BD. Retorna el objeto con ID asignado.")
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

    // -------------------------------------------------------------------------
    // GET /api/venues — Listar paginado
    // -------------------------------------------------------------------------

    @Operation(summary = "Listar venues (paginado)",
               description = "Soporta ?page=0&size=10&sort=nombre,asc. Retorna metadatos de paginación.")
    @ApiResponse(responseCode = "200", description = "Consulta exitosa")
    @GetMapping
    public ResponseEntity<Page<Venue>> listar(
            @Parameter(description = "Filtro opcional por nombre (parcial, case-insensitive)")
            @RequestParam(required = false) String nombre,
            @PageableDefault(size = 10, sort = "nombre", direction = Sort.Direction.ASC)
            Pageable pageable) {

        Page<Venue> pagina = (nombre != null && !nombre.isBlank())
                ? venueService.buscarPorNombre(nombre, pageable)
                : venueService.listarPaginado(pageable);

        return ResponseEntity.ok(pagina);
    }

    // -------------------------------------------------------------------------
    // GET /api/venues/{id} — Buscar por ID
    // -------------------------------------------------------------------------

    @Operation(summary = "Buscar venue por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venue encontrado"),
            @ApiResponse(responseCode = "404", description = "Venue no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Venue> buscarPorId(@PathVariable Long id) {
        // ResourceNotFoundException tiene @ResponseStatus(404) →
        // si no se captura aquí, Spring la convierte automáticamente en 404.
        return ResponseEntity.ok(venueService.buscarPorId(id));
    }

    // -------------------------------------------------------------------------
    // PUT /api/venues/{id} — Actualizar
    // -------------------------------------------------------------------------

    @Operation(summary = "Actualizar un venue existente",
               description = "Reemplaza todos los campos del venue. Retorna 404 si el ID no existe.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venue actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Venue no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @RequestBody Venue datos) {
        try {
            return ResponseEntity.ok(venueService.actualizar(id, datos));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
        // ResourceNotFoundException no se captura aquí → Spring la convierte en 404
    }

    // -------------------------------------------------------------------------
    // DELETE /api/venues/{id} — Eliminar
    // -------------------------------------------------------------------------

    @Operation(summary = "Eliminar un venue",
               description = "Borra el venue de la BD. Retorna 204 si fue exitoso, 404 si no existe.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Venue eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Venue no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        venueService.eliminar(id);
        // Escenario 4: eliminación exitosa → 204 No Content (sin cuerpo)
        return ResponseEntity.noContent().build();
    }
}
