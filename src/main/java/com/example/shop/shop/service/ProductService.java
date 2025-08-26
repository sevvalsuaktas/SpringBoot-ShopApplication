package com.example.shop.shop.service;

import com.example.shop.shop.dto.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    List<ProductDto> getAll();
    ProductDto getById(Long id);
    ProductDto create(ProductDto dto);
    ProductDto update(Long id, ProductDto dto);
    void delete(Long id);
    List<ProductDto> searchByName(String name);
    List<ProductDto> filterByCategory(Long categoryId);
}

