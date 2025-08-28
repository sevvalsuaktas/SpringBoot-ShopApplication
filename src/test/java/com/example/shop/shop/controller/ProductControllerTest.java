package com.example.shop.shop.controller;

import com.example.shop.shop.dto.ProductDto;
import com.example.shop.shop.exception.GlobalExceptionHandler;
import com.example.shop.shop.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@ContextConfiguration(classes = { ProductController.class, GlobalExceptionHandler.class })
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ProductService productService;

    @Test
    @DisplayName("GET /api/v1/products → 200 OK, ürünlerin listesi döndü")
    void getAllProducts_returnsList() throws Exception {
        List<ProductDto> products = Arrays.asList(
                ProductDto.builder().id(1L).name("Prod A").description("Desc A").price(10.0).categoryId(5L).inStock(true).build(),
                ProductDto.builder().id(2L).name("Prod B").description("Desc B").price(20.0).categoryId(3L).inStock(false).build()
        );
        when(productService.getAll()).thenReturn(products);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Prod A"))
                .andExpect(jsonPath("$[1].inStock").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} → 200 OK, ürün döndü")
    void getProductById_whenFound() throws Exception {
        ProductDto dto = ProductDto.builder()
                .id(3L)
                .name("Prod C")
                .description("Desc C")
                .price(15.0)
                .categoryId(2L)
                .inStock(true)
                .build();
        when(productService.getById(3L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/products/{id}", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.price").value(15.0));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} → 400 when not found")
    void getProductById_whenNotFound() throws Exception {
        when(productService.getById(99L))
                .thenThrow(new RuntimeException("Ürün bulunamadı: 99"));

        mockMvc.perform(get("/api/v1/products/{id}", 99L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ürün bulunamadı: 99"));
    }

    @Test
    @DisplayName("POST /api/v1/products → 201 Created, yeni ürün oluştu")
    void createProduct_returnsCreated() throws Exception {
        String json = "{\"name\":\"New Prod\",\"description\":\"New Desc\",\"price\":30.0,\"categoryId\":4}";
        ProductDto created = ProductDto.builder()
                .id(10L)
                .name("New Prod")
                .description("New Desc")
                .price(30.0)
                .categoryId(4L)
                .inStock(true)
                .build();
        when(productService.create(any(ProductDto.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("New Prod"));
    }

    @Test
    @DisplayName("PUT /api/v1/products/{id} → 200 OK, returns updated product")
    void updateProduct_returnsOk() throws Exception {
        String json = "{\"name\":\"Upd Prod\",\"description\":\"Upd Desc\",\"price\":25.0,\"categoryId\":2}";
        ProductDto updated = ProductDto.builder()
                .id(5L)
                .name("Upd Prod")
                .description("Upd Desc")
                .price(25.0)
                .categoryId(2L)
                .inStock(false)
                .build();
        when(productService.update(eq(5L), any(ProductDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/products/{id}", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(2))
                .andExpect(jsonPath("$.inStock").value(false));
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} → 204 No Content")
    void deleteProduct_returnsNoContent() throws Exception {
        doNothing().when(productService).delete(7L);

        mockMvc.perform(delete("/api/v1/products/{id}", 7L))
                .andExpect(status().isNoContent());

        verify(productService).delete(7L);
    }
}