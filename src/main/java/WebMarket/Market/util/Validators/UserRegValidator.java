package WebMarket.Market.util.Validators;

import WebMarket.Market.DTO.UserDTO;
import WebMarket.Market.services.UserService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserRegValidator implements Validator {
    private final UserService userService;

    public UserRegValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return UserDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserDTO user = (UserDTO) target;
        if(userService.findByUsername(user.getUsername()).isPresent()) {
            errors.rejectValue("username", "", "Username already exists");
        }
        if(user.getPassword().length() < 2 || user.getPassword().length() > 30) {
            errors.rejectValue("password", "", "Password must be between 2 and 30 characters");
        }

    }
}
