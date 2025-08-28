package com.example.shop.shop.service;

import com.example.shop.shop.dto.CategoryDto;
import com.example.shop.shop.model.Category;
import com.example.shop.shop.repository.CategoryRepository;
import com.example.shop.shop.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    @DisplayName("getAll: repository'deki tüm kategorileri DTO olarak döndürmeli")
    void getAll_returnsAllCategoryDtos() {
        // Arrange
        List<Category> categories = Arrays.asList(
                Category.builder().id(1L).name("Cat1").description("Desc1").build(),
                Category.builder().id(2L).name("Cat2").description("Desc2").build()
        );
        when(categoryRepository.findAll()).thenReturn(categories);

        // Act
        List<CategoryDto> dtos = categoryService.getAll();

        // Assert
        assertThat(dtos).hasSize(2)
                .extracting(CategoryDto::getId, CategoryDto::getName)
                .containsExactly(
                        tuple(1L, "Cat1"),
                        tuple(2L, "Cat2")
                );
    }

    @Test
    @DisplayName("getById: var olan id için doğru DTO döndürmeli")
    void getById_existingId_returnsDto() {
        // Arrange
        Category category = Category.builder().id(5L).name("CatA").description("DescA").build();
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));

        // Act
        CategoryDto dto = categoryService.getById(5L);

        // Assert
        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getName()).isEqualTo("CatA");
    }

    @Test
    @DisplayName("getById: olmayan id için RuntimeException fırlatmalı")
    void getById_missingId_throwsException() {
        // Arrange
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.getById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Kategori bulunamadı: 99");
    }

    @Test
    @DisplayName("create: verilen DTO ile yeni kategori oluşturup kayıt etmeli ve DTO döndürmeli")
    void create_savesCategoryAndReturnsDto() {
        // Arrange
        CategoryDto input = CategoryDto.builder().name("Yeni").description("YeniDesc").build();
        Category savedEntity = Category.builder().id(10L).name("Yeni").description("YeniDesc").build();
        when(categoryRepository.save(any(Category.class))).thenReturn(savedEntity);

        // Act
        CategoryDto result = categoryService.create(input);

        // Assert
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Yeni");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("update: var olan id için entity güncelleyip DTO döndürmeli")
    void update_existingId_updatesAndReturnsDto() {
        // Arrange
        Category existing = Category.builder().id(7L).name("Eski").description("EskiDesc").build();
        CategoryDto updateDto = CategoryDto.builder().name("Guncel").description("GuncelDesc").build();
        Category updatedEntity = Category.builder().id(7L).name("Guncel").description("GuncelDesc").build();
        when(categoryRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedEntity);

        // Act
        CategoryDto result = categoryService.update(7L, updateDto);

        // Assert
        assertThat(result.getName()).isEqualTo("Guncel");
        verify(categoryRepository).save(argThat(c -> c.getId().equals(7L) && c.getName().equals("Guncel")));
    }

    @Test
    @DisplayName("update: olmayan id için RuntimeException fırlatmalı")
    void update_missingId_throwsException() {
        // Arrange
        when(categoryRepository.findById(100L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.update(100L, CategoryDto.builder().build()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Kategori bulunamadı: 100");
    }

    @Test
    @DisplayName("delete: repository.deleteById çağrılmalı")
    void delete_invokesRepository() {
        // Act
        categoryService.delete(8L);

        // Assert
        verify(categoryRepository).deleteById(8L);
    }
}