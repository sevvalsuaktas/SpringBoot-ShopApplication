package com.example.shop.shop.service.impl;

import com.example.shop.shop.dto.CategoryDto;
import com.example.shop.shop.model.Category;
import com.example.shop.shop.repository.CategoryRepository;
import com.example.shop.shop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Transactional
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repo;

    private CategoryDto toDto(Category e) {
        return CategoryDto.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .build();
    }

    private Category toEntity(CategoryDto d) {
        return Category.builder()
                .name(d.getName())
                .description(d.getDescription())
                .build();
    }

    @Override
    public List<CategoryDto> getAll() {
        return repo.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getById(Long id) {
        return repo.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Kategori bulunamadı: " + id));
    }

    @Override
    public CategoryDto create(CategoryDto dto) {
        Category saved = repo.save(toEntity(dto));
        return toDto(saved);
    }

    @Override
    public CategoryDto update(Long id, CategoryDto dto) {
        Category existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Kategori bulunamadı: " + id));
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        return toDto(repo.save(existing));
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}

