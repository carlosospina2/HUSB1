package com.eventify.husb3.controller;

import com.eventify.husb3.exception.ResourceNotFoundException;
import com.eventify.husb3.model.Event;
import com.eventify.husb3.service.EventService;
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
 * API REST para Event — solo JSON, bajo /api/events.
 *
 * Separación de rutas:
 *   /admin/events/** → vistas Thymeleaf (@Controller)
 *   /api/events/**   → API REST JSON  (@RestController) ← este archivo
 *
 * Swagger documenta SOLO este controller (y VenueRestController)
 * gracias a springdoc.paths-to-match=/api/** en application.properties.
 */
@RestController
@RequestMapping("/api/events")
@Tag(name = "Eventos API", description = "CRUD REST de Eventos (JSON)")
public class EventRestController {

    private final EventService eventService;

    public EventRestController(EventService eventService) {
        this.eventService = eventService;
    }

    @Operation(summary = "Registrar un nuevo evento")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Event event) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(eventService.registrar(event));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @Operation(summary = "Listar eventos paginados")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping
    public ResponseEntity<Page<Event>> listar(
            @RequestParam(required = false) String nombre,
            @PageableDefault(size = 10, sort = "nombre", direction = Sort.Direction.ASC)
            Pageable pageable) {
        Page<Event> pagina = (nombre != null && !nombre.isBlank())
                ? eventService.buscarPorNombre(nombre, pageable)
                : eventService.listarPaginado(pageable);
        return ResponseEntity.ok(pagina);
    }

    @Operation(summary = "Buscar evento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado"),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Event> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.buscarPorId(id));
    }

    @Operation(summary = "Actualizar un evento existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "No encontrado")
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

    @Operation(summary = "Eliminar un evento")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminado"),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        eventService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
