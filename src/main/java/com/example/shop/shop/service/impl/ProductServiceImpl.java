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
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor // Lombok, final olarak işaretlenmiş tüm alanları parametre olarak alan bir constructor oluşturur. Böylece repo, categoryRepo ve inventoryClient otomatik inject edilir.
@Transactional // Sınıf içindeki tüm public metodları bir transaction’a sarar; CRUD işlemlerinde rollback/fail-silence davranışını sağlar.
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repo; // repo → Product entity’leri için JPA repository
    private final CategoryRepository categoryRepo; // Category ilişkili sorgular için
    private final InventoryClient inventoryClient; // Harici (veya stub) envanter servisine Feign client aracılığıyla bağlanmak için

    @Loggable
    private ProductDto toDto(Product e) { // Product entity’sini API’ya dönecek ProductDto’ya çevirir.
        return ProductDto.builder() //builder() deseniyle sadece gerekli alanları alır. Henüz stok bilgisi (inStock) eklenmemiştir; bu DTO’ya getById içinde sonradan ekleyeceğiz.
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .price(e.getPrice())
                //.imageUrl(e.getImageUrl())
                .categoryId(e.getCategory().getId())
                .build();
    }

    @Loggable
    private Product toEntity(ProductDto d) { // API’dan gelen ProductDto’yu yeni bir Product entity’sine dönüştürür.
        Category cat = categoryRepo.findById(d.getCategoryId()) // id ye göre category arıyor bulamazsa hata fırlatıyor
                .orElseThrow(() -> new RuntimeException("Kategori bulunamadı: " + d.getCategoryId()));

        return Product.builder() // Builder deseniyle sadece DTO’dan gelen verileri ve yüklenen Category nesnesini kullanarak Product oluşturur.
                .name(d.getName())
                .description(d.getDescription())
                .price(d.getPrice())
                //.imageUrl(d.getImageUrl())
                .category(cat)
                .build();
    }

    @Loggable
    @Override
    @org.springframework.cache.annotation.Cacheable(value = "product", key = "#id")
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
    // Circuit Breaker fallback metodu
    // İmzası: orijinalin parametreleri + Throwable
    public ProductDto inventoryFallback(Long id, Throwable ex) {
        log.warn("Inventory servisi çağrısında hata: {}, id={} – stok 'false' olarak döndürülüyor",
                ex.toString(), id);
        // Hata durumunda bile ürünü döndür, stok=false
        Product product = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı: " + id));
        ProductDto dto = toDto(product);
        dto.setInStock(false);
        return dto;
    } // bu sayede uygulama hata vermedi sadece ürün stok bilgisi false döndü; uygulama kesintiye uğramamış oldu

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
    @CacheEvict(value = {"products", "product"}, allEntries = true) // yeni bir ürün oluşturulduğunda cache temizlenir
    public ProductDto create(ProductDto dto) {
        Product saved = repo.save(toEntity(dto)); // veritabanına kaydeder
        return toDto(saved);
    }

    @Loggable
    @Override
    @CacheEvict(value = {"products", "product"}, allEntries = true) // ürüne güncelleme yapıldığında cache temizlenir
    public ProductDto update(Long id, ProductDto dto) {
        Product existing = repo.findById(id) // var olan ürünü bulur yoksa hata fırlatır
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı: " + id));
        existing.setName(dto.getName()); // basit alanları setterlarla günceller
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        //existing.setImageUrl(dto.getImageUrl());
        if (!existing.getCategory().getId().equals(dto.getCategoryId())) {
            Category newCat = categoryRepo.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Kategori bulunamadı: " + dto.getCategoryId()));
            existing.setCategory(newCat); // kategori değişmişse yeni kategori yükler
        }
        return toDto(repo.save(existing));
    }

    @Loggable
    @Override
    @CacheEvict(value = {"products", "product"}, allEntries = true) // ürün veritabanından silindiğinde cache temzilenir
    public void delete(Long id) { // id ye göre ürün veritabanından silinir
        repo.deleteById(id);
    }

    @Loggable
    @Override
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "searchFallback")
    public List<ProductDto> searchByName(String name) {
        return repo.findByNameContainingIgnoreCase(name)
                .stream()                           // List<Product> → Stream<Product>
                .map(e -> {
                    ProductDto dto = toDto(e);
                    InventoryDto inv = inventoryClient.getStock(e.getId());
                    dto.setInStock(inv.getAvailable() > 0);
                    return dto;
                })
                .collect(Collectors.toList());     // Stream<ProductDto> → List<ProductDto>
    }

    public List<ProductDto> searchFallback(String name, Throwable ex) {
        log.warn("… hata: {}; fallback’te stok olmadan dönüyoruz", ex.toString());
        return repo.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::toDto)                  // stok bilgisi eklemiyorsak sadece DTO
                .collect(Collectors.toList());
    }

    @Loggable
    @Override
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "filterFallback")
    public List<ProductDto> filterByCategory(Long categoryId) {
        return repo.findByCategoryId(categoryId)
                .stream()
                .map(e -> {
                    ProductDto dto = toDto(e);
                    InventoryDto inv = inventoryClient.getStock(e.getId());
                    dto.setInStock(inv.getAvailable() > 0);
                    return dto;
                })
                .collect(Collectors.toList());  // Son olarak List<ProductDto> elde et
    }

    public List<ProductDto> filterFallback(Long categoryId, Throwable ex) {
        log.warn("Inventory servisi filtrelemede hata: {} – stok bilgisi olmadan dönüyoruz", ex.toString());
        return repo.findByCategoryId(categoryId)
                .stream()
                .map(e -> {
                    ProductDto dto = toDto(e);
                    dto.setInStock(false);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
