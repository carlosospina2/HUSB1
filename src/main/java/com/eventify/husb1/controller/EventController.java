package com.eventify.husb1.controller;

import com.eventify.husb1.model.Event;
import com.eventify.husb1.service.EventService;
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
 * Controlador REST para Event — CRUD completo con paginación.
 *
 * Endpoints:
 *   POST   /api/events          → 201 Created
 *   GET    /api/events          → 200 OK  (paginado: ?page=0&size=10&sort=nombre,asc)
 *   GET    /api/events/{id}     → 200 OK  | 404 Not Found
 *   PUT    /api/events/{id}     → 200 OK  | 404 Not Found | 400 Bad Request
 *   DELETE /api/events/{id}     → 204 No Content | 404 Not Found
 */
@RestController
@RequestMapping("/api/events")
@Tag(name = "Eventos", description = "CRUD completo de Eventos con paginación y ordenamiento")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // -------------------------------------------------------------------------
    // POST /api/events — Crear
    // -------------------------------------------------------------------------

    @Operation(summary = "Registrar un nuevo evento",
               description = "Crea un evento y lo persiste en la BD. Retorna el objeto con ID asignado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Venue referenciado no existe")
    })
    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Event event) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(eventService.registrar(event));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
        // ResourceNotFoundException (venue inexistente) → Spring convierte en 404
    }

    // -------------------------------------------------------------------------
    // GET /api/events — Listar paginado
    // -------------------------------------------------------------------------

    @Operation(summary = "Listar eventos (paginado)",
               description = "Soporta ?page=0&size=10&sort=nombre,asc y filtro ?nombre=texto.")
    @ApiResponse(responseCode = "200", description = "Consulta exitosa")
    @GetMapping
    public ResponseEntity<Page<Event>> listar(
            @Parameter(description = "Filtro opcional por nombre (parcial, case-insensitive)")
            @RequestParam(required = false) String nombre,
            @PageableDefault(size = 10, sort = "nombre", direction = Sort.Direction.ASC)
            Pageable pageable) {

        Page<Event> pagina = (nombre != null && !nombre.isBlank())
                ? eventService.buscarPorNombre(nombre, pageable)
                : eventService.listarPaginado(pageable);

        return ResponseEntity.ok(pagina);
    }

    // -------------------------------------------------------------------------
    // GET /api/events/{id} — Buscar por ID
    // -------------------------------------------------------------------------

    @Operation(summary = "Buscar evento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento encontrado"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Event> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.buscarPorId(id));
    }

    // -------------------------------------------------------------------------
    // PUT /api/events/{id} — Actualizar
    // -------------------------------------------------------------------------

    @Operation(summary = "Actualizar un evento existente",
               description = "Reemplaza todos los campos del evento. Retorna 404 si el ID no existe.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @RequestBody Event datos) {
        try {
            return ResponseEntity.ok(eventService.actualizar(id, datos));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // DELETE /api/events/{id} — Eliminar
    // -------------------------------------------------------------------------

    @Operation(summary = "Eliminar un evento",
               description = "Borra el evento de la BD. Retorna 204 si fue exitoso, 404 si no existe.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Evento eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        eventService.eliminar(id);
        // Escenario 4: eliminación exitosa → 204 No Content (sin cuerpo en la respuesta)
        return ResponseEntity.noContent().build();
    }
}
