package com.eventify.husb3.controller;

import com.eventify.husb3.service.EventService;
import com.eventify.husb3.service.VenueService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador del panel de inicio del administrador.
 *
 * Ruta: /admin
 * Muestra un resumen con contadores de eventos y venues.
 * Sirve como punto de entrada al panel administrativo.
 */
@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final EventService eventService;
    private final VenueService venueService;

    public AdminDashboardController(EventService eventService,
                                    VenueService venueService) {
        this.eventService = eventService;
        this.venueService = venueService;
    }

    /**
     * Panel principal del administrador.
     *
     * Model:
     *   "totalEvents" → conteo total de eventos (para tarjeta de resumen).
     *   "totalVenues" → conteo total de venues.
     *
     * @return templates/admin/dashboard.html
     */
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalEvents", eventService.listarTodos().size());
        model.addAttribute("totalVenues", venueService.listarTodos().size());
        return "admin/dashboard";
    }
}
