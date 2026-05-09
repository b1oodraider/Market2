package WebMarket.Market.controllers.apiControllers;

import WebMarket.Market.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/user")
@Controller
public class UsersPagesController {
    private final UserService userService;

    public UsersPagesController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public String user(@PathVariable int id, Model model) {
        model.addAttribute("user", userService.findById(id));
        return null;
    }

}
