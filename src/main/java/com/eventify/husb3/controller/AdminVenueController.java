package com.eventify.husb3.controller;

import com.eventify.husb3.model.Venue;
import com.eventify.husb3.service.VenueService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de VISTAS para la gestión de Venues.
 *
 * @Controller (no @RestController) → retorna nombres de plantillas
 * Thymeleaf, no JSON. Spring MVC resuelve el nombre a un archivo
 * en src/main/resources/templates/.
 *
 * Rutas de interfaz web: /admin/venues/**
 * (separadas de la API REST en /api/venues/**)
 *
 * Patrón Post-Redirect-Get (PRG):
 *   El POST guarda el dato y hace redirect al listado.
 *   Así, si el usuario refresca el navegador no reenvía el formulario.
 */
@Controller
@RequestMapping("/admin/venues")
public class AdminVenueController {

    private final VenueService venueService;

    public AdminVenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    // -------------------------------------------------------------------------
    // GET /admin/venues — Listado
    // -------------------------------------------------------------------------

    /**
     * Muestra la lista de venues.
     *
     * Model: agrega "venues" (List<Venue>) para que Thymeleaf
     * pueda iterarla con th:each.
     *
     * Escenario 1: si hay venues → renderiza tabla.
     * Escenario 2: si no hay → Thymeleaf muestra mensaje "sin registros"
     *              usando th:if / th:unless sobre la lista vacía.
     *
     * @return nombre de la plantilla: templates/admin/venues/list.html
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("venues", venueService.listarTodos());
        return "admin/venues/list";
    }

    // -------------------------------------------------------------------------
    // GET /admin/venues/nuevo — Formulario de creación
    // -------------------------------------------------------------------------

    /**
     * Muestra el formulario vacío para crear un nuevo venue.
     *
     * Model: agrega un Venue vacío como "venue" para que Thymeleaf
     * lo enlace al formulario con th:object.
     *
     * @return nombre de la plantilla: templates/admin/venues/form.html
     */
    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("venue", new Venue());
        return "admin/venues/form";
    }

    // -------------------------------------------------------------------------
    // POST /admin/venues — Guardar venue (PRG)
    // -------------------------------------------------------------------------

    /**
     * Recibe los datos del formulario HTML.
     *
     * @ModelAttribute enlaza automáticamente los campos del formulario
     * a los atributos del objeto Venue (igual que @RequestBody para JSON,
     * pero para datos de formulario application/x-www-form-urlencoded).
     *
     * Escenario 3 (Registro y Redirección):
     *   1. Guarda el venue en la BD.
     *   2. Redirige al listado → el navegador hace un GET nuevo.
     *   3. El usuario ve el listado actualizado sin riesgo de re-envío.
     *
     * @return redirect al listado (Patrón PRG)
     */
    @PostMapping
    public String guardar(@ModelAttribute Venue venue, Model model) {
        try {
            venueService.registrar(venue);
            return "redirect:/admin/venues";
        } catch (IllegalArgumentException ex) {
            // Si la validación falla, volvemos al formulario con el error
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("venue", venue);
            return "admin/venues/form";
        }
    }

    // -------------------------------------------------------------------------
    // GET /admin/venues/{id}/editar — Formulario de edición
    // -------------------------------------------------------------------------

    /**
     * Muestra el formulario pre-rellenado para editar un venue existente.
     * Si el ID no existe, ResourceNotFoundException → 404.
     */
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        model.addAttribute("venue", venueService.buscarPorId(id));
        return "admin/venues/form";
    }

    // -------------------------------------------------------------------------
    // POST /admin/venues/{id}/editar — Guardar edición (PRG)
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/editar")
    public String guardarEdicion(@PathVariable Long id,
                                 @ModelAttribute Venue venue,
                                 Model model) {
        try {
            venueService.actualizar(id, venue);
            return "redirect:/admin/venues";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("venue", venue);
            return "admin/venues/form";
        }
    }

    // -------------------------------------------------------------------------
    // POST /admin/venues/{id}/eliminar — Eliminar (PRG)
    // -------------------------------------------------------------------------

    /**
     * Elimina el venue y redirige al listado.
     * Se usa POST (no DELETE) porque los formularios HTML solo soportan GET/POST.
     */
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {
        venueService.eliminar(id);
        return "redirect:/admin/venues";
    }
}
