package WebMarket.Market.services;

import WebMarket.Market.models.ProductEntity;
import WebMarket.Market.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getAll_returnsAllProducts() {
        ProductEntity product = new ProductEntity(1, "Кофе Arabica", "кофе", 10, "описание");
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductEntity> result = productService.getAll();

        assertEquals(1, result.size());
        assertEquals("Кофе Arabica", result.get(0).getProductsName());
        verify(productRepository).findAll();
    }

    @Test
    void getById_whenExists_returnsOptionalWithProduct() {
        ProductEntity product = new ProductEntity(1, "Кофе Arabica", "кофе", 10, "описание");
        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        Optional<ProductEntity> result = productService.getById(1);

        assertTrue(result.isPresent());
        assertEquals("Кофе Arabica", result.get().getProductsName());
    }

    @Test
    void getById_whenNotExists_returnsEmptyOptional() {
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        Optional<ProductEntity> result = productService.getById(999);

        assertTrue(result.isEmpty());
    }

    @Test
    void save_callsRepositorySave() {
        ProductEntity product = new ProductEntity();
        product.setProductsName("Чай Sencha");

        productService.save(product);

        verify(productRepository).save(product);
    }
}
