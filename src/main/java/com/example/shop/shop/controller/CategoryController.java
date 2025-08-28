package com.example.shop.shop.controller;

import com.example.shop.shop.dto.CategoryDto;
import com.example.shop.shop.logging.Loggable;
import com.example.shop.shop.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @Loggable
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAll() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    @Loggable
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getById(@Valid @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @Loggable
    @PostMapping
    public ResponseEntity<CategoryDto> create(@Valid @RequestBody CategoryDto dto) {
        CategoryDto created = categoryService.create(dto);
        return ResponseEntity
                .status(201)
                .body(created);
    }

    @Loggable
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDto dto) {
        return ResponseEntity.ok(categoryService.update(id, dto));
    }

    @Loggable
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Valid @PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}