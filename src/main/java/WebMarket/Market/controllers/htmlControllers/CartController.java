package WebMarket.Market.controllers.htmlControllers;

import WebMarket.Market.DTO.DBCartDTO;
import WebMarket.Market.models.LocalCart;
import WebMarket.Market.models.ProductEntity;
import WebMarket.Market.security.UsersDetails;
import WebMarket.Market.services.CartService;
import WebMarket.Market.services.ProductService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/stock")
public class CartController {

    private final CartService cartService;
    private final LocalCart myLocalCart;
    private final ProductService productService;

    public CartController(CartService cartService, LocalCart myLocalCart, ProductService productService) {
        this.cartService = cartService;
        this.myLocalCart = myLocalCart;
        this.productService = productService;
    }

    @GetMapping("/cart")
    public String cart(Model model) {
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_USER"))) {
            UsersDetails usersDetails = (UsersDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<DBCartDTO> myDBCart = cartService.getCart(usersDetails.getUser().getId());
            model.addAttribute("myDBCart", myDBCart);
        } else {
            model.addAttribute("myLocalCart", myLocalCart.getCart().values().stream().toList());
        }
        return "user/userCart";
    }

    @PatchMapping("/cart")
    public String updateCart(@RequestParam(name = "user_id") int userId,
                             @RequestParam("count") int quantity,
                             @RequestParam("product_id") int productId) {
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_USER"))) {
            cartService.changeCount(userId, productId, quantity);
        } else {
            myLocalCart.changeCount(productId, quantity);
        }
        return "redirect:cart";
    }

    @DeleteMapping("/cart")
    public String deleteCart() {
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_USER"))) {
            UsersDetails usersDetails = (UsersDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            cartService.clearCart(usersDetails.getUser().getId());
        } else {
            myLocalCart.clearCart();
        }
        return "redirect:/stock";
    }

    @PostMapping("/{id}")
    public String addToCart(@ModelAttribute("product") ProductEntity product) {
        ProductEntity newGood = productService.getById(product.getId()).orElse(null);
        if (newGood == null) {
            return "redirect:/stock";
        }
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_USER"))) {
            UsersDetails usersDetails = (UsersDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            cartService.save(usersDetails.getUser().getId(), newGood.getId());
        } else {
            myLocalCart.add(newGood);
        }
        return "redirect:/stock";
    }

    @DeleteMapping("/{id}")
    public String removeFromCart(@ModelAttribute("product") ProductEntity product,
                                 @RequestHeader("Referer") String ref) {
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_USER"))) {
            UsersDetails usersDetails = (UsersDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            cartService.deleteOne(usersDetails.getUser().getId(), product.getId());
        } else {
            myLocalCart.removeOne(product.getId());
        }
        String[] refs = ref.split("/");
        return String.format("redirect:%s", refs[refs.length - 1]);
    }
}
