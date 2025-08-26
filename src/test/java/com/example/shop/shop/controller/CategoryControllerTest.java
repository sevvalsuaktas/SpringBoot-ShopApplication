package com.example.shop.shop.controller;

import com.example.shop.shop.dto.CategoryDto;
import com.example.shop.shop.exception.GlobalExceptionHandler;
import com.example.shop.shop.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(categoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/categories → 200 OK, kategori listesi döndü")
    void getAllCategories_returnsList() throws Exception {
        List<CategoryDto> categories = Arrays.asList(
                CategoryDto.builder().id(1L).name("Cat A").description("Desc A").build(),
                CategoryDto.builder().id(2L).name("Cat B").description("Desc B").build()
        );
        when(categoryService.getAll()).thenReturn(categories);

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].name").value("Cat B"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} → 200 OK, kategori döndü")
    void getCategoryById_whenFound() throws Exception {
        CategoryDto dto = CategoryDto.builder().id(3L).name("Cat C").description("Desc C").build();
        when(categoryService.getById(3L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/categories/{id}", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Cat C"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} → 400 when not found")
    void getCategoryById_whenNotFound() throws Exception {
        when(categoryService.getById(99L))
                .thenThrow(new RuntimeException("Kategori bulunamadı: 99"));

        mockMvc.perform(get("/api/v1/categories/{id}", 99L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Kategori bulunamadı: 99"));
    }

    @Test
    @DisplayName("POST /api/v1/categories → 201 Created, yeni kategori döndü")
    void createCategory_returnsCreated() throws Exception {
        String json = "{\"name\":\"New Cat\",\"description\":\"New Desc\"}";
        CategoryDto created = CategoryDto.builder()
                .id(10L)
                .name("New Cat")
                .description("New Desc")
                .build();
        when(categoryService.create(any(CategoryDto.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.description").value("New Desc"));
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} → 200 OK, güncellenmiş kategori döndü")
    void updateCategory_returnsOk() throws Exception {
        String json = "{\"name\":\"Upd Cat\",\"description\":\"Upd Desc\"}";
        CategoryDto updated = CategoryDto.builder()
                .id(5L)
                .name("Upd Cat")
                .description("Upd Desc")
                .build();
        when(categoryService.update(eq(5L), any(CategoryDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/categories/{id}", 5L)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Upd Cat"));
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} → 204 içerik silindi")
    void deleteCategory_returnsNoContent() throws Exception {
        doNothing().when(categoryService).delete(7L);

        mockMvc.perform(delete("/api/v1/categories/{id}", 7L))
                .andExpect(status().isNoContent());
    }
}
