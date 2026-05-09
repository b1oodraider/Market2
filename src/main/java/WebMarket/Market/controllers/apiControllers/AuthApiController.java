package WebMarket.Market.controllers.apiControllers;

import WebMarket.Market.DTO.UserDTO;
import WebMarket.Market.security.SecurityUtils;
import WebMarket.Market.security.UsersDetails;
import WebMarket.Market.services.RegistrationService;
import WebMarket.Market.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthApiController {

    private final UserService userService;
    private final RegistrationService registrationService;
    private final AuthenticationManager authenticationManager;
    private final SecurityUtils securityUtils;

    public AuthApiController(UserService userService, RegistrationService registrationService,
                             AuthenticationManager authenticationManager, SecurityUtils securityUtils) {
        this.userService = userService;
        this.registrationService = registrationService;
        this.authenticationManager = authenticationManager;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Optional<UsersDetails> user = securityUtils.getCurrentUser();
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Не авторизован"));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.get().getUser().getId());
        result.put("username", user.get().getUsername());
        result.put("role", user.get().getUser().getRole());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String username = body.get("username");
        String password = body.get("password");
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            SecurityContext sc = SecurityContextHolder.createEmptyContext();
            sc.setAuthentication(auth);
            SecurityContextHolder.setContext(sc);
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);

            UsersDetails ud = (UsersDetails) auth.getPrincipal();
            Map<String, Object> result = new HashMap<>();
            result.put("id", ud.getUser().getId());
            result.put("username", ud.getUsername());
            result.put("role", ud.getUser().getRole());
            return ResponseEntity.ok(result);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Неправильное имя пользователя или пароль"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Map<String, String> errors = new HashMap<>();
        if (username == null || username.isEmpty()) {
            errors.put("username", "Имя пользователя не может быть пустым");
        }
        if (password == null || password.isEmpty()) {
            errors.put("password", "Пароль не может быть пустым");
        }
        if (password != null && (password.length() < 2 || password.length() > 30)) {
            errors.put("password", "Пароль должен быть от 2 до 30 символов");
        }
        if (username != null && userService.findByUsername(username).isPresent()) {
            errors.put("username", "Это имя пользователя уже занято");
        }

        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }

        registrationService.registerUser(new UserDTO(username, password));
        return ResponseEntity.ok(Map.of("message", "ok"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "ok"));
    }
}
