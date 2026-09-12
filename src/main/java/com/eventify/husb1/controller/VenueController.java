package com.eventify.husb1.controller;

import com.eventify.husb1.model.Venue;
import com.eventify.husb1.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la entidad Venue.
 *
 * Expone los endpoints de la API bajo el prefijo /api/venues.
 * Delega toda la lógica de negocio a VenueService.
 *
 * Estados HTTP:
 *   POST → 201 Created  (venue creado exitosamente)
 *   GET  → 200 OK       (consulta exitosa)
 *   Validación fallida → 400 Bad Request
 */
@RestController
@RequestMapping("/api/venues")
@Tag(name = "Venues", description = "Operaciones de registro y consulta de Lugares (Venues)")
public class VenueController {

    private final VenueService venueService;

    /**
     * Constructor — Spring inyecta automáticamente VenueService.
     */
    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    // -------------------------------------------------------------------------
    // POST /api/venues — Registrar venue
    // -------------------------------------------------------------------------

    /**
     * Registra un nuevo venue en el catálogo.
     *
     * Camino Feliz:  datos válidos → 201 Created + objeto creado.
     * Camino Error:  nombre vacío o capacidad <= 0 → 400 Bad Request.
     */
    @Operation(
            summary = "Registrar un nuevo venue",
            description = "Valida los datos, persiste el venue y retorna el objeto creado con su ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Venue creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (nombre vacío o capacidad inválida)")
    })
    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Venue venue) {
        try {
            Venue creado = venueService.registrar(venue);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/venues — Listar todos los venues
    // -------------------------------------------------------------------------

    /**
     * Retorna el catálogo completo de venues.
     * Si no hay venues registrados retorna 200 OK con lista vacía [].
     */
    @Operation(
            summary = "Listar todos los venues",
            description = "Retorna todos los venues registrados. Si no hay ninguno, retorna una lista vacía."
    )
    @ApiResponse(responseCode = "200", description = "Consulta exitosa")
    @GetMapping
    public ResponseEntity<List<Venue>> listarTodos() {
        return ResponseEntity.ok(venueService.listarTodos());
    }

    // -------------------------------------------------------------------------
    // GET /api/venues/{id} — Buscar venue por ID
    // -------------------------------------------------------------------------

    /**
     * Busca y retorna un venue específico por su ID.
     */
    @Operation(
            summary = "Buscar venue por ID",
            description = "Retorna el venue correspondiente al ID proporcionado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venue encontrado"),
            @ApiResponse(responseCode = "400", description = "No existe un venue con ese ID")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(venueService.buscarPorId(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
