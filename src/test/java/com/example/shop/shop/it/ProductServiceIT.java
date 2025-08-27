package com.example.shop.shop.it;

import support.TestSecurityConfig;
import com.example.shop.shop.dto.ProductDto;
import com.example.shop.shop.model.Category;
import com.example.shop.shop.model.Product;
import com.example.shop.shop.repository.CategoryRepository;
import com.example.shop.shop.repository.ProductRepository;
import com.example.shop.shop.service.ProductService;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                // H2’yi inline ayarla ki ayrı application-test.yml gerekmesin
                "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                "spring.datasource.driverClassName=org.h2.Driver",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
        }
)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class) // tüm istekler permitAll
class ProductServiceIT {

    private static final WireMockServer wm = new WireMockServer(options().dynamicPort());

    @BeforeAll
    static void startWireMock() { wm.start(); }

    @AfterAll
    static void stopWireMock() { wm.stop(); }

    @DynamicPropertySource
    static void feignProps(DynamicPropertyRegistry reg) {
        // Feign’i WireMock’a yönlendir
        reg.add("inventory.service.url", () -> "http://localhost:" + wm.port());
    }

    @BeforeEach
    void stubInventory() {
        wm.resetAll();
        // /api/v1/inventory/{id} çağrısına 100 available döndür
        wm.stubFor(get(urlMatching("/api/v1/inventory/\\d+"))
                .willReturn(okJson("{\"productId\":1,\"available\":100}")));
    }

    @Autowired ProductService productService;
    @Autowired ProductRepository productRepository;
    @Autowired CategoryRepository categoryRepository;

    @Test
    @DisplayName("getById → Feign WireMock’a gider; available=100 → inStock=true")
    void getById_callsInventory_andSetsInStock() {
        // GIVEN
        Category cat = categoryRepository.save(Category.builder().name("Elektronik").build());
        Product p = productRepository.save(
                Product.builder()
                        .name("Telefon")
                        .description("Dokunmatik Telefon")
                        .price(BigDecimal.valueOf(999.99))
                        .category(cat)
                        .build()
        );

        // WHEN
        ProductDto dto = productService.getById(p.getId());

        // THEN
        assertThat(dto.getId()).isEqualTo(p.getId());
        assertThat(dto.getName()).isEqualTo("Telefon");
        assertThat(dto.getCategoryId()).isEqualTo(cat.getId());
        assertThat(dto.getInStock()).isTrue();
    }
}

