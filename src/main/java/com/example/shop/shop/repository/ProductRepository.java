package com.example.shop.shop.repository;

import com.example.shop.shop.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Kategoriye göre sayfalı listeleme
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    // İsimde geçen ile arama
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Fiyata göre filtreleme
    Page<Product> findByPriceBetween(Double min, Double max, Pageable pageable);
}

