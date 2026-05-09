package WebMarket.Market.DTO;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    @NotEmpty(message = "Имя пользователя не может быть пустым")
    private String username;
    @NotEmpty(message = "Пароль не может быть пустым")
    private String password;
}
