package WebMarket.Market.services;

import WebMarket.Market.DTO.DBCartDTO;
import WebMarket.Market.models.DBCartEntity;
import WebMarket.Market.models.ProductEntity;
import WebMarket.Market.repositories.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void getCart_returnsMappedDTOList() {
        ProductEntity product = new ProductEntity(2, "Урбеч", "еда", 100, "вкусный");
        DBCartEntity cartItem = new DBCartEntity(1, 2, 3);
        cartItem.setProduct(product);

        when(cartRepository.findByUserId(1)).thenReturn(List.of(cartItem));

        List<DBCartDTO> result = cartService.getCart(1);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getUserId());
        assertEquals(2, result.get(0).getProductId());
        assertEquals(3, result.get(0).getProductCount());
        assertEquals("Урбеч", result.get(0).getProductsName());
    }

    @Test
    void getCart_whenEmpty_returnsEmptyList() {
        when(cartRepository.findByUserId(1)).thenReturn(List.of());

        List<DBCartDTO> result = cartService.getCart(1);

        assertEquals(0, result.size());
    }

    @Test
    void save_savesCartWithCountOne() {
        cartService.save(1, 5);

        verify(cartRepository).save(argThat(entity ->
                entity.getUserId() == 1
                && entity.getProductId() == 5
                && entity.getProductCount() == 1
        ));
    }

    @Test
    void changeCount_withPositiveCount_savesNewCount() {
        cartService.changeCount(1, 5, 10);

        verify(cartRepository).save(argThat(entity ->
                entity.getUserId() == 1
                && entity.getProductId() == 5
                && entity.getProductCount() == 10
        ));
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
