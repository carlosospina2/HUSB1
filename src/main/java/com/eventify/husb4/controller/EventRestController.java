package com.eventify.husb4.controller;

import com.eventify.husb4.dto.EventSummaryDTO;
import com.eventify.husb4.model.Event;
import com.eventify.husb4.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * API REST para Event — JSON, bajo /api/events.
 *
 * Escenario 4 (Swagger):
 *   - Endpoints documentados con Records (EventSummaryDTO).
 *   - Parámetros relacionales (city, categoryName) explicados.
 *   - Soft Delete documentado con @Operation describiendo active=false.
 *   - springdoc.paths-to-match=/api/** → solo rutas /api/ aparecen en Swagger UI.
 */
@RestController
@RequestMapping("/api/events")
@Tag(
    name = "Eventos API",
    description = """
        CRUD REST de Eventos.
        
        Reglas de negocio importantes:
        • Los listados retornan EventSummaryDTO (Record — proyección liviana via JPQL).
        • La paginación usa Slice (sin COUNT(*) para máxima eficiencia).
        • El borrado es lógico (Soft Delete): activa active=false.
          @SQLRestriction filtra los inactivos en TODAS las consultas JPA automáticamente.
        • Los filtros city y categoryName son case-insensitive y parciales (LIKE %param%).
        """
)
public class EventRestController {

    private final EventService eventService;

    public EventRestController(EventService eventService) {
        this.eventService = eventService;
    }

    // -------------------------------------------------------------------------
    // POST /api/events — Crear evento
    // -------------------------------------------------------------------------

    @Operation(
        summary = "Registrar un nuevo evento",
        description = """
            Crea un evento con relaciones Many-to-One (Venue) y Many-to-Many (Categories).
            El venueId es obligatorio. Los categoryIds son opcionales.
            Escenario 1: el log muestra el INSERT principal + vínculos con parámetros reales.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Evento creado con relaciones"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o venue inexistente"),
        @ApiResponse(responseCode = "404", description = "Venue o Category no encontrado")
    })
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            String    name        = (String)  body.get("name");
            LocalDate date        = LocalDate.parse((String) body.get("date"));
            String    description = (String)  body.get("description");
            Long      venueId     = Long.valueOf(body.get("venueId").toString());

            @SuppressWarnings("unchecked")
            List<Long> categoryIds = body.containsKey("categoryIds")
                    ? ((List<Integer>) body.get("categoryIds"))
                        .stream().map(Long::valueOf).toList()
                    : List.of();

            Event saved = eventService.save(name, date, description, venueId, categoryIds);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/events — Listado con Record EventSummaryDTO + filtros
    // -------------------------------------------------------------------------

    @Operation(
        summary = "Listar eventos (Slice de EventSummaryDTO)",
        description = """
            Retorna un Slice<EventSummaryDTO> con proyección liviana (id, eventName, date, venueName, city).
            
            Optimizaciones:
            • Record EventSummaryDTO: solo 5 campos viajan desde la BD (no entidades pesadas).
            • Slice: evita COUNT(*), más eficiente que Page para catálogos grandes.
            • Ordenado por fecha descendente (Escenario 2).
            
            Filtros disponibles (Escenario 6 — case-insensitive, parciales):
            • city: "bog" encuentra eventos en "Bogotá".
            • categoryName: "rock" encuentra eventos en "Conciertos de Rock".
            • Combinados: ambos filtros se aplican simultáneamente.
            """
    )
    @ApiResponse(responseCode = "200", description = "Slice de EventSummaryDTO ordenado por fecha DESC")
    @GetMapping
    public ResponseEntity<Slice<EventSummaryDTO>> list(
            @Parameter(description = "Filtro por ciudad (parcial, case-insensitive). Ej: 'bog' → Bogotá")
            @RequestParam(required = false) String city,

            @Parameter(description = "Filtro por categoría (parcial, case-insensitive). Ej: 'rock' → Conciertos")
            @RequestParam(required = false) String categoryName,

            @Parameter(description = "Número de página (0-based). Default: 0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Tamaño de página. Default: 10")
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(eventService.search(city, categoryName, page, size));
    }

    // -------------------------------------------------------------------------
    // GET /api/events/{id} — Detalle completo con venue y categories
    // -------------------------------------------------------------------------

    @Operation(
        summary = "Obtener evento por ID con detalles completos",
        description = """
            Carga el evento con venue y categories en una sola query SQL (@EntityGraph).
            Resuelve el problema N+1: no se generan SELECTs adicionales por las relaciones.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Evento encontrado con venue y categories"),
        @ApiResponse(responseCode = "404", description = "Evento no encontrado (o inactive por Soft Delete)")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Event> getById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.findWithDetailsById(id));
    }

    // -------------------------------------------------------------------------
    // PUT /api/events/{id} — Actualizar
    // -------------------------------------------------------------------------

    @Operation(
        summary = "Actualizar evento existente",
        description = "Modifica todos los campos del evento, incluyendo venue y categorías asignadas."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Evento actualizado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Evento, Venue o Category no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody Map<String, Object> body) {
        try {
            String    name        = (String) body.get("name");
            LocalDate date        = LocalDate.parse((String) body.get("date"));
            String    description = (String) body.get("description");
            Long      venueId     = Long.valueOf(body.get("venueId").toString());

            @SuppressWarnings("unchecked")
            List<Long> categoryIds = body.containsKey("categoryIds")
                    ? ((List<Integer>) body.get("categoryIds"))
                        .stream().map(Long::valueOf).toList()
                    : List.of();

            return ResponseEntity.ok(
                    eventService.update(id, name, date, description, venueId, categoryIds));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // -------------------------------------------------------------------------
    // DELETE /api/events/{id} — Soft Delete
    // -------------------------------------------------------------------------

    @Operation(
        summary = "Soft Delete de un evento",
        description = """
            Marca el evento como inactivo (active = false). NO ejecuta DELETE físico.
            
            Impacto del Soft Delete:
            • @SQLRestriction("active = true") filtra el evento automáticamente en
              TODAS las consultas JPA posteriores, sin necesidad de filtros manuales.
            • El registro permanece en la BD para auditoría histórica.
            • Escenario 4: este comportamiento está documentado en Swagger UI.
            
            Retorna 204 No Content si el evento existía y fue desactivado.
            Retorna 404 si el ID no existe (o ya fue desactivado previamente).
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Evento desactivado (Soft Delete aplicado)"),
        @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        eventService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // GET /api/events/search/date-range — Búsqueda por rango de fechas
    // -------------------------------------------------------------------------

    @Operation(
        summary = "Buscar eventos por rango de fechas",
        description = "Retorna eventos cuya fecha esté entre 'from' y 'to' (inclusive). Ordenados por fecha DESC."
    )
    @ApiResponse(responseCode = "200", description = "Slice de eventos en el rango especificado")
    @GetMapping("/search/date-range")
    public ResponseEntity<Slice<EventSummaryDTO>> byDateRange(
            @Parameter(description = "Fecha inicio (yyyy-MM-dd)") @RequestParam String from,
            @Parameter(description = "Fecha fin (yyyy-MM-dd)")    @RequestParam String to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                eventService.findByDateRange(
                        LocalDate.parse(from), LocalDate.parse(to), page, size));
    }
}
