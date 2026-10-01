package com.eventify.husb4.controller;

import com.eventify.husb4.dto.EventSummaryDTO;
import com.eventify.husb4.model.Event;
import com.eventify.husb4.service.CategoryService;
import com.eventify.husb4.service.EventService;
import com.eventify.husb4.service.VenueService;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador de vistas para gestión de Eventos.
 *
 * Rutas: /admin/events/**
 *
 * Características HU-SB4:
 * - Listado usa Slice<EventSummaryDTO> (Record, proyección liviana).
 * - Filtros city y categoryName persisten en la URL (Escenario 3).
 * - Formulario incluye select de Venue y checkboxes de Category.
 * - Borrado invoca softDelete (no DELETE físico).
 * - Navegación Anterior/Siguiente basada en slice.hasNext() / page > 0.
 */
@Controller
@RequestMapping("/admin/events")
public class AdminEventController {

    private static final int PAGE_SIZE = 10;

    private final EventService    eventService;
    private final VenueService    venueService;
    private final CategoryService categoryService;

    public AdminEventController(EventService eventService,
                                VenueService venueService,
                                CategoryService categoryService) {
        this.eventService    = eventService;
        this.venueService    = venueService;
        this.categoryService = categoryService;
    }

    // -------------------------------------------------------------------------
    // GET /admin/events — Listado con filtros y navegación Slice
    // -------------------------------------------------------------------------

    /**
     * Listado paginado con filtros persistentes en la URL.
     *
     * Escenario 2: una sola query SQL (EventSummaryDTO via JPQL).
     * Escenario 3: city y categoryName se mantienen en los links de navegación.
     * Escenario 6: filtros case-insensitive y parciales.
     *
     * @param city         Filtro por ciudad (parcial, opcional).
     * @param categoryName Filtro por categoría (parcial, opcional).
     * @param page         Página actual (0-based).
     */
    @GetMapping
    public String listar(
            @RequestParam(required = false, defaultValue = "") String city,
            @RequestParam(required = false, defaultValue = "") String categoryName,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Slice<EventSummaryDTO> slice = eventService.search(
                city.isBlank() ? null : city,
                categoryName.isBlank() ? null : categoryName,
                page, PAGE_SIZE);

        model.addAttribute("events",       slice.getContent());
        model.addAttribute("slice",        slice);
        model.addAttribute("currentPage",  page);
        model.addAttribute("hasNext",      slice.hasNext());
        model.addAttribute("hasPrev",      page > 0);
        model.addAttribute("nextPage",     page + 1);
        model.addAttribute("prevPage",     Math.max(0, page - 1));
        // Filtros activos — persisten en la URL de navegación (Escenario 3)
        model.addAttribute("city",         city);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("categories",   categoryService.findAll());

        return "admin/events/list";
    }

    // -------------------------------------------------------------------------
    // GET /admin/events/nuevo — Formulario de creación
    // -------------------------------------------------------------------------

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("event",      new Event());
        model.addAttribute("venues",     venueService.findAll());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("selectedCategoryIds", List.of());
        return "admin/events/form";
    }

    // -------------------------------------------------------------------------
    // POST /admin/events — Guardar nuevo evento (PRG)
    // -------------------------------------------------------------------------

    /**
     * Recibe datos del formulario, incluyendo checkboxes de categorías.
     * categoryIds es una lista de IDs seleccionados por el usuario.
     * Escenario 1: INSERT principal + vínculos relacionales visibles en log.
     */
    @PostMapping
    public String guardar(
            @RequestParam String name,
            @RequestParam String date,
            @RequestParam(required = false) String description,
            @RequestParam Long venueId,
            @RequestParam(required = false) List<Long> categoryIds,
            Model model) {
        try {
            eventService.save(name, LocalDate.parse(date), description, venueId, categoryIds);
            return "redirect:/admin/events";
        } catch (Exception ex) {
            model.addAttribute("error",      ex.getMessage());
            model.addAttribute("venues",     venueService.findAll());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("selectedCategoryIds", categoryIds != null ? categoryIds : List.of());
            model.addAttribute("event", new Event());
            return "admin/events/form";
        }
    }

    // -------------------------------------------------------------------------
    // GET /admin/events/{id}/editar
    // -------------------------------------------------------------------------

    @GetMapping("/{id}/editar")
    public String mostrarEdicion(@PathVariable Long id, Model model) {
        Event event = eventService.findWithDetailsById(id);
        List<Long> selectedIds = event.getCategories().stream()
                .map(c -> c.getId()).toList();

        model.addAttribute("event",      event);
        model.addAttribute("venues",     venueService.findAll());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("selectedCategoryIds", selectedIds);
        return "admin/events/form";
    }

    // -------------------------------------------------------------------------
    // POST /admin/events/{id}/editar — Actualizar (PRG)
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/editar")
    public String guardarEdicion(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String date,
            @RequestParam(required = false) String description,
            @RequestParam Long venueId,
            @RequestParam(required = false) List<Long> categoryIds,
            Model model) {
        try {
            eventService.update(id, name, LocalDate.parse(date), description, venueId, categoryIds);
            return "redirect:/admin/events";
        } catch (Exception ex) {
            model.addAttribute("error",      ex.getMessage());
            model.addAttribute("venues",     venueService.findAll());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("selectedCategoryIds", categoryIds != null ? categoryIds : List.of());
            model.addAttribute("event", eventService.findWithDetailsById(id));
            return "admin/events/form";
        }
    }

    // -------------------------------------------------------------------------
    // POST /admin/events/{id}/eliminar — Soft Delete (PRG)
    // -------------------------------------------------------------------------

    /**
     * Borrado lógico: marca active=false.
     * @SQLRestriction filtra el evento automáticamente tras el UPDATE.
     * NUNCA ejecuta DELETE físico.
     */
    @PostMapping("/{id}/eliminar")
    public String softDelete(@PathVariable Long id) {
        eventService.softDelete(id);
        return "redirect:/admin/events";
    }
}
