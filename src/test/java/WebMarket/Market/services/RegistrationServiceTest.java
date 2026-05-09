package WebMarket.Market.services;

import WebMarket.Market.DTO.UserDTO;
import WebMarket.Market.models.UserEntity;
import WebMarket.Market.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void registerUser_encodesPasswordAndAssignsRole() {
        UserDTO dto = new UserDTO("vasya", "secret");

        UserEntity mappedUser = new UserEntity();
        mappedUser.setUsername("vasya");
        mappedUser.setPassword("secret");

        when(modelMapper.map(dto, UserEntity.class)).thenReturn(mappedUser);
        when(passwordEncoder.encode("secret")).thenReturn("encoded_secret");

        registrationService.registerUser(dto);

        verify(passwordEncoder).encode("secret");
        // проверяем что в БД сохраняется пользователь с зашифрованным паролем и ролью ROLE_USER
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("encoded_secret")
                && user.getRole().equals("ROLE_USER")
        ));
    }

    @Test
    void registerUser_callsRepositorySave() {
        UserDTO dto = new UserDTO("petya", "pass");
        UserEntity mappedUser = new UserEntity();
        mappedUser.setPassword("pass");

        when(modelMapper.map(dto, UserEntity.class)).thenReturn(mappedUser);
        when(passwordEncoder.encode("pass")).thenReturn("hashed");

        registrationService.registerUser(dto);

        verify(userRepository, times(1)).save(any(UserEntity.class));
    }
}
