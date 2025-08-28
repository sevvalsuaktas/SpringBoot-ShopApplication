package com.example.shop.shop.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class InventoryStubControllerTest {
    private MockMvc mockMvc;
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new InventoryStubController())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/inventory/{productId} → 200 ve doğru JSON")
    void getStock_success() throws Exception {
        long productId = 77L;

        mockMvc.perform(get("/api/v1/inventory/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId").value((int) productId))
                .andExpect(jsonPath("$.available").value(100));
    }

    @Test
    @DisplayName("Eksik path ile istek → 404 Not Found")
    void getStock_missingId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/"))
                .andExpect(status().isNotFound());
    }
}