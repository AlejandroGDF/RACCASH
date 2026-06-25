package com.raccooncash.api.categoria;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoriaControlador {

    private final CategoriaServicio categoryService;

    public CategoriaControlador(CategoriaServicio categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoriaRespuesta>> getCategories(@RequestParam(required = false) TipoCategoria type) {
        return ResponseEntity.ok(categoryService.getCategories(type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaRespuesta> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PostMapping
    public ResponseEntity<CategoriaRespuesta> createCategory(@Valid @RequestBody CategoriaSolicitud request) {
        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaRespuesta> updateCategory(@PathVariable Long id,
                                                           @Valid @RequestBody CategoriaSolicitud request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
