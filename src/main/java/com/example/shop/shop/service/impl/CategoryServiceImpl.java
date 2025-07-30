package com.example.shop.shop.service.impl;

import com.example.shop.shop.dto.CategoryDto;
import com.example.shop.shop.logging.Loggable;
import com.example.shop.shop.model.Category;
import com.example.shop.shop.repository.CategoryRepository;
import com.example.shop.shop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repo;

    @Loggable
    private CategoryDto toDto(Category e) {
        return CategoryDto.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .build();
    }

    @Loggable
    private Category toEntity(CategoryDto d) {
        return Category.builder()
                .name(d.getName())
                .description(d.getDescription())
                .build();
    }

    @Loggable
    @Override
    @Cacheable(value = "categories")
    public List<CategoryDto> getAll() {
        return repo.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Loggable
    @Override
    public CategoryDto getById(Long id) {
        return repo.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Kategori bulunamadı: " + id));
    }

    @Loggable
    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryDto create(CategoryDto dto) { // kategori yoksa oluştur
        Category saved = repo.save(toEntity(dto));
        return toDto(saved);
    }

    @Loggable
    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryDto update(Long id, CategoryDto dto) {
        Category existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Kategori bulunamadı: " + id));
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        return toDto(repo.save(existing));
    }

    @Loggable
    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public void delete(Long id) {
        repo.deleteById(id);
    }
}

