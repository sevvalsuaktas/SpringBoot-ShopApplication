package com.example.shop.shop.service;

import com.example.shop.shop.dto.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductDto> getAll(Pageable pageable);
    ProductDto getById(Long id);
    ProductDto create(ProductDto dto);
    ProductDto update(Long id, ProductDto dto);
    void delete(Long id);
    Page<ProductDto> searchByName(String name, Pageable pageable);
    Page<ProductDto> filterByCategory(Long categoryId, Pageable pageable);
}

