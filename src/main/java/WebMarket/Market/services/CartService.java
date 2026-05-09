package WebMarket.Market.services;

import WebMarket.Market.DTO.DBCartDTO;
import WebMarket.Market.models.DBCartEntity;
import WebMarket.Market.models.ProductEntity;
import WebMarket.Market.repositories.CartRepository;
import WebMarket.Market.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
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
        ProductEntity product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return;
        }

        Optional<DBCartEntity> existing = cartRepository.findByUserIdAndProductId(userId, productId);

        if (existing.isPresent()) {
            int newCount = Math.min(existing.get().getProductCount() + 1, product.getProductsInStock());
            cartRepository.save(new DBCartEntity(userId, productId, newCount));
        } else {
            cartRepository.save(new DBCartEntity(userId, productId, 1));
        }
    }

    @Transactional
    public void changeCount(int userId, int productId, int newCount) {
        if (newCount <= 0) {
            cartRepository.deleteAllByUserIdAndProductId(userId, productId);
            return;
        }
        ProductEntity product = productRepository.findById(productId).orElse(null);
        int cappedCount = (product != null)
                ? Math.min(newCount, product.getProductsInStock())
                : newCount;
        cartRepository.save(new DBCartEntity(userId, productId, cappedCount));
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
