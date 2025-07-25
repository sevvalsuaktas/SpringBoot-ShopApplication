package com.example.shop.shop.service.impl;

import com.example.shop.shop.client.InventoryClient;
import com.example.shop.shop.dto.InventoryDto;
import com.example.shop.shop.dto.ProductDto;
import com.example.shop.shop.model.Category;
import com.example.shop.shop.model.Product;
import com.example.shop.shop.repository.ProductRepository;
import com.example.shop.shop.repository.CategoryRepository;
import com.example.shop.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Transactional
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repo;
    private final CategoryRepository categoryRepo;
    private final InventoryClient inventoryClient;

    private ProductDto toDto(Product e) {
        return ProductDto.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .price(e.getPrice())
                .imageUrl(e.getImageUrl())
                .categoryId(e.getCategory().getId())
                .build();
    }

    private Product toEntity(ProductDto d) {
        Category cat = categoryRepo.findById(d.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Kategori bulunamadı: " + d.getCategoryId()));

        return Product.builder()
                .name(d.getName())
                .description(d.getDescription())
                .price(d.getPrice())
                .imageUrl(d.getImageUrl())
                .category(cat)
                .build();
    }

    @Override
    public ProductDto getById(Long id) {
        Product e = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı: " + id));
        ProductDto dto = toDto(e);

        // Feign ile inventory servisten stok al
        InventoryDto inv = inventoryClient.getStock(id);
        dto.setInStock(inv.getAvailable() > 0);

        return dto;
    }

    @Override
    public Page<ProductDto> getAll(Pageable page) {
        return repo.findAll(page).map(this::toDto);
    }

    @Override
    public ProductDto create(ProductDto dto) {
        Product saved = repo.save(toEntity(dto));
        return toDto(saved);
    }

    @Override
    public ProductDto update(Long id, ProductDto dto) {
        Product existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı: " + id));
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setImageUrl(dto.getImageUrl());
        if (!existing.getCategory().getId().equals(dto.getCategoryId())) {
            Category newCat = categoryRepo.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Kategori bulunamadı: " + dto.getCategoryId()));
            existing.setCategory(newCat);
        }
        return toDto(repo.save(existing));
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    public Page<ProductDto> searchByName(String name, Pageable page) {
        return repo.findByNameContainingIgnoreCase(name, page).map(this::toDto);
    }

    @Override
    public Page<ProductDto> filterByCategory(Long catId, Pageable page) {
        return repo.findByCategoryId(catId, page).map(this::toDto);
    }
}

