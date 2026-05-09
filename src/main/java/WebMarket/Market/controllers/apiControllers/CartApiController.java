package WebMarket.Market.controllers.apiControllers;

import WebMarket.Market.DTO.DBCartDTO;
import WebMarket.Market.security.SecurityUtils;
import WebMarket.Market.security.UsersDetails;
import WebMarket.Market.services.CartService;
import WebMarket.Market.services.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    private final CartService cartService;
    private final ProductService productService;
    private final SecurityUtils securityUtils;

    public CartApiController(CartService cartService, ProductService productService, SecurityUtils securityUtils) {
        this.cartService = cartService;
        this.productService = productService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public ResponseEntity<?> getCart() {
        Optional<UsersDetails> user = securityUtils.getCurrentUser();
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Необходима авторизация"));
        }
        List<DBCartDTO> cart = cartService.getCart(user.get().getUser().getId());
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<?> addToCart(@PathVariable int productId) {
        Optional<UsersDetails> user = securityUtils.getCurrentUser();
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Необходима авторизация"));
        }
        if (productService.getById(productId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Товар не найден"));
        }
        cartService.save(user.get().getUser().getId(), productId);
        return ResponseEntity.ok(Map.of("message", "added"));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<?> updateCount(@PathVariable int productId, @RequestBody Map<String, Integer> body) {
        Optional<UsersDetails> user = securityUtils.getCurrentUser();
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Необходима авторизация"));
        }
        Integer count = body.get("count");
        if (count == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Поле count обязательно"));
        }
        cartService.changeCount(user.get().getUser().getId(), productId, count);
        return ResponseEntity.ok(Map.of("message", "updated"));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeItem(@PathVariable int productId) {
        Optional<UsersDetails> user = securityUtils.getCurrentUser();
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Необходима авторизация"));
        }
        cartService.deleteOne(user.get().getUser().getId(), productId);
        return ResponseEntity.ok(Map.of("message", "removed"));
    }

    @DeleteMapping
    public ResponseEntity<?> clearCart() {
        Optional<UsersDetails> user = securityUtils.getCurrentUser();
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Необходима авторизация"));
        }
        cartService.clearCart(user.get().getUser().getId());
        return ResponseEntity.ok(Map.of("message", "cleared"));
    }
}
