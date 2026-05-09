package WebMarket.Market.services;

import WebMarket.Market.DTO.DBCartDTO;
import WebMarket.Market.models.DBCartEntity;
import WebMarket.Market.models.ProductEntity;
import WebMarket.Market.repositories.CartRepository;
import WebMarket.Market.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void getCart_returnsMappedDTOList() {
        ProductEntity product = new ProductEntity(2, "Кофе Arabica", "кофе", 100, "вкусный");
        DBCartEntity cartItem = new DBCartEntity(1, 2, 3);
        cartItem.setProduct(product);

        when(cartRepository.findByUserId(1)).thenReturn(List.of(cartItem));

        List<DBCartDTO> result = cartService.getCart(1);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getUserId());
        assertEquals(2, result.get(0).getProductId());
        assertEquals(3, result.get(0).getProductCount());
        assertEquals("Кофе Arabica", result.get(0).getProductsName());
    }

    @Test
    void getCart_whenEmpty_returnsEmptyList() {
        when(cartRepository.findByUserId(1)).thenReturn(List.of());

        List<DBCartDTO> result = cartService.getCart(1);

        assertEquals(0, result.size());
    }

    @Test
    void save_newItem_savesWithCountOne() {
        ProductEntity product = new ProductEntity(5, "Чай", "чай", 10, "вкусный");
        when(productRepository.findById(5)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserIdAndProductId(1, 5)).thenReturn(Optional.empty());

        cartService.save(1, 5);

        verify(cartRepository).save(argThat(e ->
                e.getUserId() == 1 && e.getProductId() == 5 && e.getProductCount() == 1
        ));
    }

    @Test
    void save_existingItem_incrementsCount() {
        ProductEntity product = new ProductEntity(5, "Чай", "чай", 10, "вкусный");
        DBCartEntity existing = new DBCartEntity(1, 5, 3);
        when(productRepository.findById(5)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserIdAndProductId(1, 5)).thenReturn(Optional.of(existing));

        cartService.save(1, 5);

        verify(cartRepository).save(argThat(e -> e.getProductCount() == 4));
    }

    @Test
    void save_existingItem_doesNotExceedStock() {
        ProductEntity product = new ProductEntity(5, "Чай", "чай", 3, "вкусный");
        DBCartEntity existing = new DBCartEntity(1, 5, 3); // уже максимум
        when(productRepository.findById(5)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserIdAndProductId(1, 5)).thenReturn(Optional.of(existing));

        cartService.save(1, 5);

        // количество не должно превысить 3
        verify(cartRepository).save(argThat(e -> e.getProductCount() == 3));
    }

    @Test
    void save_productNotFound_doesNothing() {
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        cartService.save(1, 999);

        verify(cartRepository, never()).save(any());
    }

    @Test
    void changeCount_withPositiveCount_savesNewCount() {
        ProductEntity product = new ProductEntity(5, "Чай", "чай", 10, "вкусный");
        when(productRepository.findById(5)).thenReturn(Optional.of(product));

        cartService.changeCount(1, 5, 7);

        verify(cartRepository).save(argThat(e -> e.getProductCount() == 7));
    }

    @Test
    void changeCount_exceedsStock_capsAtStock() {
        ProductEntity product = new ProductEntity(5, "Чай", "чай", 10, "вкусный");
        when(productRepository.findById(5)).thenReturn(Optional.of(product));

        cartService.changeCount(1, 5, 999);

        verify(cartRepository).save(argThat(e -> e.getProductCount() == 10));
    }

    @Test
    void changeCount_withZeroOrNegative_deletesItem() {
        cartService.changeCount(1, 5, 0);

        verify(cartRepository).deleteAllByUserIdAndProductId(1, 5);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void clearCart_callsDeleteAllByUserId() {
        cartService.clearCart(1);

        verify(cartRepository).deleteAllByUserId(1);
    }

    @Test
    void deleteOne_callsDeleteAllByUserIdAndProductId() {
        cartService.deleteOne(1, 5);

        verify(cartRepository).deleteAllByUserIdAndProductId(1, 5);
    }
}
