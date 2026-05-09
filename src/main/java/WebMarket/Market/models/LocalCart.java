package WebMarket.Market.models;

import WebMarket.Market.repositories.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@SessionScope
@Component
public class LocalCart {
    private Map<String, CartNode> cart = new HashMap<>();

    private final ProductRepository productRepository;

    public LocalCart(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void add(ProductEntity product) {
        cart.put(product.getProductsName(), new CartNode(product, 1));
    }

    public void changeCount(int id, int count) {
        ProductEntity product = productRepository.findById(id).get();
        cart.put(product.getProductsName(), new CartNode(product, count));
    }

    public void removeOne(int id) {
        cart.remove(productRepository.findById(id).get().getProductsName());
    }

    public void clearCart() {
        cart.clear();
    }
}

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
class CartNode {
    private ProductEntity product;
    private int count;
}
