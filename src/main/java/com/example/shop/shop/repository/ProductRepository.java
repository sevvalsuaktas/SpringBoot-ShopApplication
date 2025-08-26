package com.example.shop.shop.repository;

import com.example.shop.shop.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAll();

    // Kategoriye göre sayfalı listeleme
    List<Product> findByCategoryId(Long categoryId);

    // İsimde geçen ile arama
    List<Product> findByNameContainingIgnoreCase(String name);

    // Fiyata göre filtreleme
    //List<Product> findByPriceBetween(Double min, Double max);
}

