package com.eventify.husb4.service;

import com.eventify.husb4.exception.ResourceNotFoundException;
import com.eventify.husb4.model.Category;
import com.eventify.husb4.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Capa de negocio para Category.
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category save(Category category) {
        if (category.getName() == null || category.getName().isBlank())
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio.");
        return categoryRepository.save(category);
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public void delete(Long id) {
        if (!categoryRepository.existsById(id))
            throw new ResourceNotFoundException("Category", id);
        categoryRepository.deleteById(id);
    }
}
