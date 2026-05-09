package WebMarket.Market.controllers.apiControllers;

import WebMarket.Market.DTO.UserDTO;
import WebMarket.Market.security.SecurityUtils;
import WebMarket.Market.security.UsersDetails;
import WebMarket.Market.services.RegistrationService;
import WebMarket.Market.util.Validators.UserRegValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthApiController {

    private final RegistrationService registrationService;
    private final AuthenticationManager authenticationManager;
    private final SecurityUtils securityUtils;
    private final UserRegValidator userRegValidator;

    public AuthApiController(RegistrationService registrationService,
                             AuthenticationManager authenticationManager, SecurityUtils securityUtils,
                             UserRegValidator userRegValidator) {
        this.registrationService = registrationService;
        this.authenticationManager = authenticationManager;
        this.securityUtils = securityUtils;
        this.userRegValidator = userRegValidator;
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
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO, BindingResult bindingResult) {
        // @Valid отрабатывает jakarta.validation (@NotEmpty), затем доменные правила
        // (уникальность username, длина пароля) подключаются тем же UserRegValidator,
        // что и Thymeleaf-форма регистрации — единый источник истины.
        // Валидатор содержит null-check, поэтому безопасен даже если @Valid уже нашёл ошибки.
        userRegValidator.validate(userDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError fe : bindingResult.getFieldErrors()) {
                errors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        }

        registrationService.registerUser(userDTO);
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
