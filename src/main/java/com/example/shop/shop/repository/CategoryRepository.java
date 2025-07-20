package com.example.shop.shop.repository;

import com.example.shop.shop.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Gerekirse özel sorgular ekleyebilirsin, örneğin:
    // Optional<Category> findByName(String name);
}