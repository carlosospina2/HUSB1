package com.eventify.husb4.controller;

import com.eventify.husb4.service.CategoryService;
import com.eventify.husb4.service.EventService;
import com.eventify.husb4.service.VenueService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Panel de inicio del administrador.
 * Muestra contadores de eventos activos, venues y categorías.
 */
@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final EventService    eventService;
    private final VenueService    venueService;
    private final CategoryService categoryService;

    public AdminDashboardController(EventService eventService,
                                    VenueService venueService,
                                    CategoryService categoryService) {
        this.eventService    = eventService;
        this.venueService    = venueService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalEvents",     eventService.countActive());
        model.addAttribute("totalVenues",     venueService.findAll().size());
        model.addAttribute("totalCategories", categoryService.findAll().size());
        return "admin/dashboard";
    }
}
