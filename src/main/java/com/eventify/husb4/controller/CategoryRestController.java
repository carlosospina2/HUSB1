package com.eventify.husb4.controller;

import com.eventify.husb4.model.Category;
import com.eventify.husb4.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API REST para Category — JSON, bajo /api/categories.
 */
@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categorías API",
     description = "Gestión de categorías temáticas de eventos (Conciertos, Talleres, etc.)")
public class CategoryRestController {

    private final CategoryService categoryService;

    public CategoryRestController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Listar todas las categorías")
    @ApiResponse(responseCode = "200", description = "Lista de categorías")
    @GetMapping
    public ResponseEntity<List<Category>> list() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    @Operation(summary = "Registrar nueva categoría")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Categoría creada"),
        @ApiResponse(responseCode = "400", description = "Nombre vacío o duplicado")
    })
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Category category) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.save(category));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @Operation(summary = "Eliminar categoría por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Eliminada"),
        @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
