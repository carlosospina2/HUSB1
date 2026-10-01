package com.eventify.husb4.controller;

import com.eventify.husb4.model.Venue;
import com.eventify.husb4.service.VenueService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de vistas para Venues.
 * Rutas: /admin/venues/**
 * Patrón PRG en todos los POST.
 */
@Controller
@RequestMapping("/admin/venues")
public class AdminVenueController {

    private final VenueService venueService;

    public AdminVenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("venues", venueService.findAll());
        return "admin/venues/list";
    }

    @GetMapping("/nuevo")
    public String formulario(Model model) {
        model.addAttribute("venue", new Venue());
        return "admin/venues/form";
    }

    @PostMapping
    public String guardar(@ModelAttribute Venue venue, Model model) {
        try {
            venueService.save(venue);
            return "redirect:/admin/venues";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("venue", venue);
            return "admin/venues/form";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("venue", venueService.findById(id));
        return "admin/venues/form";
    }

    @PostMapping("/{id}/editar")
    public String guardarEdicion(@PathVariable Long id,
                                 @ModelAttribute Venue venue,
                                 Model model) {
        try {
            venueService.update(id, venue);
            return "redirect:/admin/venues";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("venue", venue);
            return "admin/venues/form";
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {
        venueService.delete(id);
        return "redirect:/admin/venues";
    }
}
