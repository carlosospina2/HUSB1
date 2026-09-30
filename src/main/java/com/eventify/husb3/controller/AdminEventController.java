package com.eventify.husb3.controller;

import com.eventify.husb3.model.Event;
import com.eventify.husb3.service.EventService;
import com.eventify.husb3.service.VenueService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de VISTAS para la gestión de Eventos.
 *
 * @Controller → retorna nombres de plantillas Thymeleaf.
 *
 * Rutas de interfaz web: /admin/events/**
 *
 * Se inyecta también VenueService para poblar el selector
 * de venues en el formulario de creación/edición.
 *
 * Patrón PRG aplicado en todos los POST.
 */
@Controller
@RequestMapping("/admin/events")
public class AdminEventController {

    private final EventService eventService;
    private final VenueService venueService;

    public AdminEventController(EventService eventService,
                                VenueService venueService) {
        this.eventService = eventService;
        this.venueService = venueService;
    }

    // -------------------------------------------------------------------------
    // GET /admin/events — Listado
    // -------------------------------------------------------------------------

    /**
     * Muestra el listado de todos los eventos.
     *
     * Model:
     *   "events"  → List<Event> para th:each en la tabla.
     *   "venues"  → List<Venue> para mostrar el nombre del venue en cada fila.
     *
     * Escenario 1: tabla con datos.
     * Escenario 2: lista vacía → Thymeleaf renderiza mensaje amigable.
     *
     * @return templates/admin/events/list.html
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("events", eventService.listarTodos());
        model.addAttribute("venues", venueService.listarTodos());
        return "admin/events/list";
    }

    // -------------------------------------------------------------------------
    // GET /admin/events/nuevo — Formulario de creación
    // -------------------------------------------------------------------------

    /**
     * Muestra el formulario vacío para crear un evento.
     *
     * Model:
     *   "event"  → Event vacío enlazado a th:object del formulario.
     *   "venues" → lista para el <select> de venue.
     *
     * @return templates/admin/events/form.html
     */
    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("event", new Event());
        model.addAttribute("venues", venueService.listarTodos());
        return "admin/events/form";
    }

    // -------------------------------------------------------------------------
    // POST /admin/events — Guardar evento (PRG)
    // -------------------------------------------------------------------------

    /**
     * @ModelAttribute enlaza los campos del formulario HTML al objeto Event.
     *
     * Escenario 3: guarda → redirect al listado (PRG).
     * Si la validación falla: vuelve al formulario con mensaje de error.
     */
    @PostMapping
    public String guardar(@ModelAttribute Event event, Model model) {
        try {
            eventService.registrar(event);
            // Patrón PRG: redirect evita reenvío del formulario al refrescar
            return "redirect:/admin/events";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("event", event);
            model.addAttribute("venues", venueService.listarTodos());
            return "admin/events/form";
        }
    }

    // -------------------------------------------------------------------------
    // GET /admin/events/{id}/editar — Formulario de edición
    // -------------------------------------------------------------------------

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.buscarPorId(id));
        model.addAttribute("venues", venueService.listarTodos());
        return "admin/events/form";
    }

    // -------------------------------------------------------------------------
    // POST /admin/events/{id}/editar — Guardar edición (PRG)
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/editar")
    public String guardarEdicion(@PathVariable Long id,
                                 @ModelAttribute Event event,
                                 Model model) {
        try {
            eventService.actualizar(id, event);
            return "redirect:/admin/events";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("event", event);
            model.addAttribute("venues", venueService.listarTodos());
            return "admin/events/form";
        }
    }

    // -------------------------------------------------------------------------
    // POST /admin/events/{id}/eliminar — Eliminar (PRG)
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {
        eventService.eliminar(id);
        return "redirect:/admin/events";
    }
}
