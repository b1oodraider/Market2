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
        if (user.getUsername() != null
                && !user.getUsername().isEmpty()
                && userService.findByUsername(user.getUsername()).isPresent()) {
            errors.rejectValue("username", "", "Это имя пользователя уже занято");
        }
        if (user.getPassword() != null
                && (user.getPassword().length() < 2 || user.getPassword().length() > 30)) {
            errors.rejectValue("password", "", "Пароль должен быть от 2 до 30 символов");
        }
    }
}
