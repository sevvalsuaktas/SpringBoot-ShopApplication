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

@Service
@RequiredArgsConstructor // Lombok, final olarak işaretlenmiş tüm alanları parametre olarak alan bir constructor oluşturur. Böylece repo, categoryRepo ve inventoryClient otomatik inject edilir.
@Transactional // Sınıf içindeki tüm public metodları bir transaction’a sarar; CRUD işlemlerinde rollback/fail-silence davranışını sağlar.
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repo; // repo → Product entity’leri için JPA repository
    private final CategoryRepository categoryRepo; // Category ilişkili sorgular için
    private final InventoryClient inventoryClient; // Harici (veya stub) envanter servisine Feign client aracılığıyla bağlanmak için

    private ProductDto toDto(Product e) { // Product entity’sini API’ya dönecek ProductDto’ya çevirir.
        return ProductDto.builder() //builder() deseniyle sadece gerekli alanları alır. Henüz stok bilgisi (inStock) eklenmemiştir; bu DTO’ya getById içinde sonradan ekleyeceğiz.
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .price(e.getPrice())
                .imageUrl(e.getImageUrl())
                .categoryId(e.getCategory().getId())
                .build();
    }

    private Product toEntity(ProductDto d) { // API’dan gelen ProductDto’yu yeni bir Product entity’sine dönüştürür.
        Category cat = categoryRepo.findById(d.getCategoryId()) // id ye göre category arıyor bulamazsa hata fırlatıyor
                .orElseThrow(() -> new RuntimeException("Kategori bulunamadı: " + d.getCategoryId()));

        return Product.builder() // Builder deseniyle sadece DTO’dan gelen verileri ve yüklenen Category nesnesini kullanarak Product oluşturur.
                .name(d.getName())
                .description(d.getDescription())
                .price(d.getPrice())
                .imageUrl(d.getImageUrl())
                .category(cat)
                .build();
    }

    @Override
    public ProductDto getById(Long id) {
        Product e = repo.findById(id) // findById ile veritabanından ürünü alır, yoksa RuntimeException fırlatır.
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı: " + id));
        ProductDto dto = toDto(e);

        // Feign ile inventory servisten stok al
        InventoryDto inv = inventoryClient.getStock(id); // stok miktarını çeker
        dto.setInStock(inv.getAvailable() > 0); // dto ya boolean stok durumu ekler

        return dto;
    }

    @Override
    public Page<ProductDto> getAll(Pageable page) { // Sayfalı (Pageable) ürün listesini DTO listesine dönüştürür.
        return repo.findAll(page).map(this::toDto); // JPA sayfalı sonuç döner, map(this::toDto) her bir entity’yi DTO’ya çevirir.
    }

    @Override
    public ProductDto create(ProductDto dto) {
        Product saved = repo.save(toEntity(dto)); // veritabanına kaydeder
        return toDto(saved);
    }

    @Override
    public ProductDto update(Long id, ProductDto dto) {
        Product existing = repo.findById(id) // var olan ürünü bulur yoksa hata fırlatır
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı: " + id));
        existing.setName(dto.getName()); // basit alanları setterlarla günceller
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setImageUrl(dto.getImageUrl());
        if (!existing.getCategory().getId().equals(dto.getCategoryId())) {
            Category newCat = categoryRepo.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Kategori bulunamadı: " + dto.getCategoryId()));
            existing.setCategory(newCat); // kategori değişmişse yeni kategori yükler
        }
        return toDto(repo.save(existing));
    }

    @Override
    public void delete(Long id) { // id ye göre üürn veritabanından silinir
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

