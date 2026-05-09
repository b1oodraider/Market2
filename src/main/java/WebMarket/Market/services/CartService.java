package WebMarket.Market.services;

import WebMarket.Market.DTO.DBCartDTO;
import WebMarket.Market.models.DBCartEntity;
import WebMarket.Market.repositories.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {
    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Transactional
    public List<DBCartDTO> getCart(int userId) {
        List<DBCartEntity> cartItems = cartRepository.findByUserId(userId);
        List<DBCartDTO> result = new ArrayList<>();
        for (DBCartEntity item : cartItems) {
            result.add(new DBCartDTO(
                    item.getUserId(),
                    item.getProductId(),
                    item.getProductCount(),
                    item.getProduct().getProductsName()
            ));
        }
        return result;
    }

    @Transactional
    public void save(int userId, int productId) {
        DBCartEntity cart = new DBCartEntity(userId, productId, 1);
        cartRepository.save(cart);
    }

    @Transactional
    public void changeCount(int userId, int productId, int newCount) {
        if (newCount <= 0) {
            cartRepository.deleteAllByUserIdAndProductId(userId, productId);
            return;
        }
        cartRepository.save(new DBCartEntity(userId, productId, newCount));
    }

    @Transactional
    public void clearCart(int userId) {
        cartRepository.deleteAllByUserId(userId);
    }

    @Transactional
    public void deleteOne(int userId, int productId) {
        cartRepository.deleteAllByUserIdAndProductId(userId, productId);
    }
}
