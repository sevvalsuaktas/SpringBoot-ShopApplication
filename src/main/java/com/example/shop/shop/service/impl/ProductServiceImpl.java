package com.example.shop.shop.service.impl;

import com.example.shop.shop.client.InventoryClient;
import com.example.shop.shop.dto.InventoryDto;
import com.example.shop.shop.dto.ProductDto;
import com.example.shop.shop.logging.Loggable;
import com.example.shop.shop.model.Category;
import com.example.shop.shop.model.Product;
import com.example.shop.shop.repository.ProductRepository;
import com.example.shop.shop.repository.CategoryRepository;
import com.example.shop.shop.service.ProductService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repo;
    private final CategoryRepository categoryRepo;
    private final InventoryClient inventoryClient;

    private Double toDouble(BigDecimal v) {
        return v == null ? null : v.doubleValue();
    }

    private BigDecimal toBigDecimal(Double v) {
        return v == null ? BigDecimal.ZERO : BigDecimal.valueOf(v);
    }

    private ProductDto toDto(Product e) {
        return ProductDto.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .price(toDouble(e.getPrice()))
                .categoryId(e.getCategory().getId())
                .build();
    }

    @Loggable
    private Product toEntity(ProductDto d) {
        Category cat = categoryRepo.findById(d.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Kategori bulunamadı: " + d.getCategoryId()));

        return Product.builder()
                .name(d.getName())
                .description(d.getDescription())
                .price(toBigDecimal(d.getPrice()))
                .category(cat)
                .build();
    }

    @Loggable
    @Override
    @Cacheable(value = "product", key = "#id")
    public ProductDto getById(Long id) {
        return fetchByIdWithStock(id);
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "inventoryFallback")
    ProductDto fetchByIdWithStock(Long id) {
        Product e = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı: " + id));
        ProductDto dto = toDto(e);

        InventoryDto inv = inventoryClient.getStock(id);
        dto.setInStock(inv.getAvailable() > 0);
        return dto;
    }

    // inventory mikroservisine ulaşılmazsa inventoryFallback e düşecek
    public ProductDto inventoryFallback(Long id, Throwable ex) {
        log.warn("Inventory servisi çağrısında hata: {}, id={} – stok 'false' olarak döndürülüyor",
                ex.toString(), id);

        Product product = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı: " + id));
        ProductDto dto = toDto(product);
        dto.setInStock(false);
        return dto;
    }

    @Loggable
    @Override
    @Cacheable(value = "products")
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "listFallback")
    public List<ProductDto> getAll() {
        return repo.findAll().stream()
                .map(e -> {
                    ProductDto dto = toDto(e);
                    InventoryDto inv = inventoryClient.getStock(e.getId());
                    dto.setInStock(inv.getAvailable() > 0);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<ProductDto> listFallback(Throwable ex) {
        log.warn("Inventory servisi listeleme sırasında hata: {} – stok bilgisi olmadan dönüyoruz", ex.toString());
        return repo.findAll().stream()
                .map(e -> {
                    ProductDto dto = toDto(e);
                    dto.setInStock(false);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Loggable
    @Override
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public ProductDto create(ProductDto dto) {
        Product saved = repo.save(toEntity(dto));
        return toDto(saved);
    }

    @Loggable
    @Override
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public ProductDto update(Long id, ProductDto dto) {
        Product existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı: " + id));
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(toBigDecimal(dto.getPrice()));
        if (!existing.getCategory().getId().equals(dto.getCategoryId())) {
            Category newCat = categoryRepo.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Kategori bulunamadı: " + dto.getCategoryId()));
            existing.setCategory(newCat);
        }
        return toDto(repo.save(existing));
    }

    @Loggable
    @Override
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public void delete(Long id) {
        repo.deleteById(id);
    }
}