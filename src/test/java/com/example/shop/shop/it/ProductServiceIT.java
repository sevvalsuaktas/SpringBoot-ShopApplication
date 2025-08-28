package com.example.shop.shop.it;

import com.example.shop.shop.dto.ProductDto;
import com.example.shop.shop.model.Category;
import com.example.shop.shop.model.Product;
import com.example.shop.shop.repository.CategoryRepository;
import com.example.shop.shop.repository.ProductRepository;
import com.example.shop.shop.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                // H2 bellek içi DB
                "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                "spring.datasource.driverClassName=org.h2.Driver",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                // Testte cache/redis uğraşmasın
                "spring.cache.type=none"
        }
)
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0) // WireMock'u Spring başlatır ve 'wiremock.server.port' property’sini yazar
class ProductServiceIT {

    // Feign'in base URL'ini WireMock’un dinlediği porta yönlendir
    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("inventory.service.url",
                () -> "http://localhost:" + System.getProperty("wiremock.server.port"));
    }

    @Autowired ProductService productService;
    @Autowired ProductRepository productRepository;
    @Autowired CategoryRepository categoryRepository;

    @BeforeEach
    void stubInventory() {
        reset(); // tüm önceki stub/verify'ları sıfırla
        stubFor(get(urlPathMatching("/api/v1/inventory/\\d+"))
                .willReturn(okJson("{\"productId\":1,\"available\":100}")));
    }

    @Test
    @DisplayName("getById → WireMock envanter yanıtına göre inStock=true")
    void getById_callsInventory_andSetsInStock() {
        // GIVEN
        Category cat = categoryRepository.save(Category.builder().name("Elektronik").build());
        Product p = productRepository.save(Product.builder()
                .name("Telefon")
                .description("Dokunmatik Telefon")
                .price(new BigDecimal("999.99"))
                .category(cat)
                .build());

        // WHEN
        ProductDto dto = productService.getById(p.getId());

        // THEN
        assertThat(dto.getId()).isEqualTo(p.getId());
        assertThat(dto.getName()).isEqualTo("Telefon");
        assertThat(dto.getCategoryId()).isEqualTo(cat.getId());
        assertThat(dto.getInStock()).isTrue();

        // Çağrı gerçekten WireMock'a gitti mi? (tam path'i doğrulayalım)
        verify(getRequestedFor(urlPathEqualTo("/api/v1/inventory/" + p.getId())));
    }
}
