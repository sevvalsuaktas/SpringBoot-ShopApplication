package com.example.shop.shop.service;

import com.example.shop.shop.client.InventoryClient;
import com.example.shop.shop.dto.InventoryDto;
import com.example.shop.shop.dto.ProductDto;
import com.example.shop.shop.model.Category;
import com.example.shop.shop.model.Product;
import com.example.shop.shop.repository.CategoryRepository;
import com.example.shop.shop.repository.ProductRepository;
import com.example.shop.shop.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    ProductRepository repo;

    @Mock
    CategoryRepository categoryRepo;

    @Mock
    InventoryClient inventoryClient;

    @InjectMocks ProductServiceImpl service;

    private static Category cat(long id, String name) {
        return Category.builder().id(id).name(name).build();
    }

    private static Product prod(long id, String name, double price, Category c) {
        return Product.builder()
                .id(id).name(name).description(name + " desc")
                .price(price).category(c)
                .build();
    }

    private static InventoryDto stock(long pid, int available) {
        return InventoryDto.builder().productId(pid).available(available).build();
    }

    @Test
    @DisplayName("getById: mevcut id için DTO + stok bilgisi döner")
    void getById_ok() {
        Category c = cat(42L, "Elektronik");
        Product p = prod(5L, "ProdA", 15.5, c);

        when(repo.findById(5L)).thenReturn(Optional.of(p));
        when(inventoryClient.getStock(5L)).thenReturn(stock(5L, 3));

        ProductDto dto = service.getById(5L);

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getName()).isEqualTo("ProdA");
        assertThat(dto.getPrice()).isEqualTo(15.5);
        assertThat(dto.getCategoryId()).isEqualTo(42L);
        assertThat(dto.getInStock()).isTrue();
    }

    @Test
    @DisplayName("getById: olmayan id için RuntimeException fırlatır")
    void getById_notFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Ürün bulunamadı: 99");
        verify(inventoryClient, never()).getStock(anyLong());
    }

    @Test
    @DisplayName("getAll: tüm ürünleri DTO'ya map eder ve stok değerini doldurur")
    void getAll_ok() {
        Category c = cat(42L, "Elektronik");
        Product p1 = prod(1L, "U1", 10.0, c);
        Product p2 = prod(2L, "U2", 20.0, c);

        when(repo.findAll()).thenReturn(Arrays.asList(p1, p2));
        when(inventoryClient.getStock(1L)).thenReturn(stock(1L, 0)); // false
        when(inventoryClient.getStock(2L)).thenReturn(stock(2L, 7)); // true

        List<ProductDto> list = service.getAll();

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getId()).isEqualTo(1L);
        assertThat(list.get(0).getInStock()).isFalse();
        assertThat(list.get(1).getId()).isEqualTo(2L);
        assertThat(list.get(1).getInStock()).isTrue();
    }

    @Test
    @DisplayName("create: DTO -> Entity kaydeder ve DTO döndürür")
    void create_ok() {
        Category c = cat(42L, "Elektronik");
        when(categoryRepo.findById(42L)).thenReturn(Optional.of(c));

        ProductDto input = ProductDto.builder()
                .name("Yeni").description("YeniDesc").price(30.0).categoryId(42L)
                .build();

        when(repo.save(any(Product.class))).thenAnswer(inv -> {
            Product arg = inv.getArgument(0);
            // id DB'den gelmiş gibi
            return Product.builder()
                    .id(10L)
                    .name(arg.getName())
                    .description(arg.getDescription())
                    .price(arg.getPrice())
                    .category(arg.getCategory())
                    .build();
        });

        ProductDto out = service.create(input);

        assertThat(out.getId()).isEqualTo(10L);
        assertThat(out.getName()).isEqualTo("Yeni");
        assertThat(out.getCategoryId()).isEqualTo(42L);

        verify(repo).save(argThat((ArgumentMatcher<Product>) p ->
                "Yeni".equals(p.getName()) &&
                        p.getCategory() != null &&
                        p.getCategory().getId().equals(42L)
        ));
    }

    @Test
    @DisplayName("update: mevcut ürün güncellenir; kategori değiştiyse yeni kategori set edilir")
    void update_changeCategory() {
        Category oldCat = cat(42L, "Elektronik");
        Category newCat = cat(77L, "Kitap");
        Product existing = prod(7L, "Eski", 5.0, oldCat);

        when(repo.findById(7L)).thenReturn(Optional.of(existing));
        when(categoryRepo.findById(77L)).thenReturn(Optional.of(newCat));
        when(repo.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductDto update = ProductDto.builder()
                .name("Guncel")
                .description("GuncelDesc")
                .price(7.5)
                .categoryId(77L)
                .build();

        ProductDto out = service.update(7L, update);

        assertThat(out.getName()).isEqualTo("Guncel");
        assertThat(out.getPrice()).isEqualTo(7.5);
        assertThat(out.getCategoryId()).isEqualTo(77L);

        verify(categoryRepo).findById(77L);
        verify(repo).save(argThat(p ->
                p.getId().equals(7L) &&
                        p.getCategory().getId().equals(77L)
        ));
    }

    @Test
    @DisplayName("update: kategori aynı ise Category repo çağrılmaz")
    void update_sameCategory() {
        Category cat = cat(42L, "Elektronik");
        Product existing = prod(7L, "Eski", 5.0, cat);

        when(repo.findById(7L)).thenReturn(Optional.of(existing));
        when(repo.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductDto update = ProductDto.builder()
                .name("Guncel")
                .description("GuncelDesc")
                .price(7.5)
                .categoryId(42L) // aynı kategori
                .build();

        ProductDto out = service.update(7L, update);

        assertThat(out.getCategoryId()).isEqualTo(42L);
        verify(categoryRepo, never()).findById(anyLong());
    }

    @Test
    @DisplayName("update: olmayan id için RuntimeException fırlatır")
    void update_notFound() {
        when(repo.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(100L, ProductDto.builder().build()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Ürün bulunamadı: 100");
    }

    @Test
    @DisplayName("delete: repo.deleteById çağrılır")
    void delete_ok() {
        service.delete(8L);
        verify(repo).deleteById(8L);
    }

    @Test
    @DisplayName("searchByName: isimle arar ve stok bilgisini set eder")
    void searchByName_ok() {
        Category c = cat(1L, "Genel");
        Product p1 = prod(11L, "pro-abc", 9.9, c);
        Product p2 = prod(12L, "PRO-xyz", 19.9, c);

        when(repo.findByNameContainingIgnoreCase("pro")).thenReturn(Arrays.asList(p1, p2));
        when(inventoryClient.getStock(11L)).thenReturn(stock(11L, 0));
        when(inventoryClient.getStock(12L)).thenReturn(stock(12L, 2));

        List<ProductDto> out = service.searchByName("pro");

        assertThat(out).hasSize(2);
        assertThat(out.get(0).getInStock()).isFalse();
        assertThat(out.get(1).getInStock()).isTrue();
    }

    @Test
    @DisplayName("filterByCategory: kategoriye göre listeler ve stok bilgisini set eder")
    void filterByCategory_ok() {
        Category c = cat(5L, "Ktg");
        Product p1 = prod(21L, "A", 1.0, c);
        Product p2 = prod(22L, "B", 2.0, c);

        when(repo.findByCategoryId(5L)).thenReturn(Arrays.asList(p1, p2));
        when(inventoryClient.getStock(21L)).thenReturn(stock(21L, 1));
        when(inventoryClient.getStock(22L)).thenReturn(stock(22L, 0));

        List<ProductDto> out = service.filterByCategory(5L);

        assertThat(out).hasSize(2);
        assertThat(out.get(0).getCategoryId()).isEqualTo(5L);
        assertThat(out.get(0).getInStock()).isTrue();
        assertThat(out.get(1).getInStock()).isFalse();
    }

    @Test
    @DisplayName("inventoryFallback: ürün bulunursa stok=false ile döner")
    void inventoryFallback_ok() {
        Category c = cat(42L, "Elektronik");
        Product p = prod(5L, "ProdA", 15.5, c);
        when(repo.findById(5L)).thenReturn(Optional.of(p));

        ProductDto out = service.inventoryFallback(5L, new RuntimeException("boom"));

        assertThat(out.getId()).isEqualTo(5L);
        assertThat(out.getInStock()).isFalse();
    }

    @Test
    @DisplayName("listFallback: stok bilgisi olmadan (false) döner")
    void listFallback_ok() {
        Category c = cat(42L, "Elektronik");
        when(repo.findAll()).thenReturn(Arrays.asList(
                prod(1L, "U1", 1.0, c),
                prod(2L, "U2", 2.0, c)
        ));

        List<ProductDto> out = service.listFallback(new RuntimeException("x"));

        assertThat(out).hasSize(2);
        assertThat(out.get(0).getInStock()).isFalse();
        assertThat(out.get(1).getInStock()).isFalse();
    }
}
