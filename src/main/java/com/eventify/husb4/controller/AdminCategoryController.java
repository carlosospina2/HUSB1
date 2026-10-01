package com.eventify.husb4.controller;

import com.eventify.husb4.model.Category;
import com.eventify.husb4.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de vistas para Categories.
 * Rutas: /admin/categories/**
 */
@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories/list";
    }

    @GetMapping("/nueva")
    public String formulario(Model model) {
        model.addAttribute("category", new Category());
        return "admin/categories/form";
    }

    @PostMapping
    public String guardar(@ModelAttribute Category category, Model model) {
        try {
            categoryService.save(category);
            return "redirect:/admin/categories";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("category", category);
            return "admin/categories/form";
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {
        categoryService.delete(id);
        return "redirect:/admin/categories";
    }
}
