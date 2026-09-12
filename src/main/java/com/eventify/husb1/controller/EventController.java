package com.eventify.husb1.controller;

import com.eventify.husb1.model.Event;
import com.eventify.husb1.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la entidad Event.
 *
 * Expone los endpoints de la API bajo el prefijo /api/events.
 * Delega toda la lógica de negocio a EventService — el Controller
 * solo traduce peticiones HTTP a llamadas de servicio y respuestas HTTP.
 *
 * Inyección por Constructor:
 *   EventService se inyecta por constructor para mantener
 *   coherencia con el patrón de inyección del proyecto.
 *
 * Estados HTTP:
 *   POST → 201 Created  (recurso creado exitosamente)
 *   GET  → 200 OK       (consulta exitosa, puede retornar lista vacía)
 *   Validación fallida → 400 Bad Request
 */
@RestController
@RequestMapping("/api/events")
@Tag(name = "Eventos", description = "Operaciones de registro y consulta de Eventos")
public class EventController {

    private final EventService eventService;

    /**
     * Constructor — Spring inyecta automáticamente EventService.
     */
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // -------------------------------------------------------------------------
    // POST /api/events — Registrar evento
    // -------------------------------------------------------------------------

    /**
     * Registra un nuevo evento en el catálogo.
     *
     * Escenario 1 (Camino Feliz):   datos válidos → 201 Created + objeto creado.
     * Escenario 2 (Camino de Error): nombre vacío → 400 Bad Request + mensaje.
     */
    @Operation(
            summary = "Registrar un nuevo evento",
            description = "Valida los datos, persiste el evento y retorna el objeto creado con su ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (nombre vacío o fecha nula)")
    })
    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Event event) {
        try {
            Event creado = eventService.registrar(event);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/events — Listar todos los eventos
    // -------------------------------------------------------------------------

    /**
     * Retorna el catálogo completo de eventos.
     *
     * Escenario 3 (Caso de Borde): sin datos → 200 OK + lista vacía [].
     */
    @Operation(
            summary = "Listar todos los eventos",
            description = "Retorna todos los eventos registrados. Si no hay ninguno, retorna una lista vacía."
    )
    @ApiResponse(responseCode = "200", description = "Consulta exitosa")
    @GetMapping
    public ResponseEntity<List<Event>> listarTodos() {
        return ResponseEntity.ok(eventService.listarTodos());
    }

    // -------------------------------------------------------------------------
    // GET /api/events/{id} — Buscar evento por ID
    // -------------------------------------------------------------------------

    /**
     * Busca y retorna un evento específico por su ID.
     */
    @Operation(
            summary = "Buscar evento por ID",
            description = "Retorna el evento correspondiente al ID proporcionado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento encontrado"),
            @ApiResponse(responseCode = "400", description = "No existe un evento con ese ID")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(eventService.buscarPorId(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
