package WebMarket.Market.controllers.htmlControllers;

import WebMarket.Market.DTO.UserDTO;
import WebMarket.Market.services.RegistrationService;
import WebMarket.Market.util.Validators.UserRegValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/secure")
public class SecurityController {
    private final UserRegValidator userRegValidator;
    private final RegistrationService registrationService;

    @Autowired
    public SecurityController(UserRegValidator userRegValidator, RegistrationService registrationService) {
        this.userRegValidator = userRegValidator;
        this.registrationService = registrationService;
    }

    @GetMapping("/login")
    public String login() {
        return "login/login";
    }

    @GetMapping("/registration")
    public String registration(@ModelAttribute("user") UserDTO userDTO) {
        return "registration";
    }

    @PostMapping("/registration")
    public String registration(@ModelAttribute("user") @Valid UserDTO userDTO,
                               BindingResult bindingResult) {
        userRegValidator.validate(userDTO, bindingResult);
        if(bindingResult.hasErrors()) {
            return "registration";
        }
        registrationService.registerUser(userDTO);
        return "redirect:/secure/login";
    }

}
