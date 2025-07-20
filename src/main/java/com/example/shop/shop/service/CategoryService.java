package com.example.shop.shop.service;

import com.example.shop.shop.dto.CategoryDto;
import java.util.List;

public interface CategoryService {
    List<CategoryDto> getAll();
    CategoryDto getById(Long id);
    CategoryDto create(CategoryDto dto);
    CategoryDto update(Long id, CategoryDto dto);
    void delete(Long id);
}
