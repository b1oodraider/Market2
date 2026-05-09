package WebMarket.Market.services;

import WebMarket.Market.models.UserEntity;
import WebMarket.Market.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void findAll_returnsAllUsers() {
        UserEntity user = new UserEntity();
        user.setUsername("vasya");
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserEntity> result = userService.findAll();

        assertEquals(1, result.size());
        assertEquals("vasya", result.get(0).getUsername());
        verify(userRepository).findAll();
    }

    @Test
    void findById_whenUserExists_returnsOptionalWithUser() {
        UserEntity user = new UserEntity();
        user.setUsername("vasya");
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        Optional<UserEntity> result = userService.findById(1);

        assertTrue(result.isPresent());
        assertEquals("vasya", result.get().getUsername());
    }

    @Test
    void findById_whenUserNotFound_returnsEmptyOptional() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        Optional<UserEntity> result = userService.findById(99);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUsername_whenExists_returnsOptionalWithUser() {
        UserEntity user = new UserEntity();
        user.setUsername("vasya");
        when(userRepository.findByUsername("vasya")).thenReturn(Optional.of(user));

        Optional<UserEntity> result = userService.findByUsername("vasya");

        assertTrue(result.isPresent());
        assertEquals("vasya", result.get().getUsername());
    }

    @Test
    void findByUsername_whenNotExists_returnsEmptyOptional() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        Optional<UserEntity> result = userService.findByUsername("ghost");

        assertTrue(result.isEmpty());
    }

    @Test
    void save_callsRepositorySave() {
        UserEntity user = new UserEntity();

        userService.save(user);

        verify(userRepository).save(user);
    }

    @Test
    void delete_callsRepositoryDeleteById() {
        userService.delete(5);

        verify(userRepository).deleteById(5);
    }
}
