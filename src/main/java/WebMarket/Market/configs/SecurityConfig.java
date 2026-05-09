package WebMarket.Market.configs;

import WebMarket.Market.services.UsersDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UsersDetailsService userDetailsService;

    public SecurityConfig(UsersDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(req -> req
                // SPA static files
                .requestMatchers("/", "/index.html", "/app.js", "/styles.css", "/favicon.ico").permitAll()
                // existing thymeleaf routes
                .requestMatchers("/hello", "/login/**", "/secure/registration", "/error",
                        "/stock/{good_id:[0-9]+}", "/stock", "/stock/cart").permitAll()
                // public API endpoints
                .requestMatchers("/api/me", "/api/login", "/api/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/{id}").permitAll()
                // admin only
                .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                .requestMatchers("/stock/addNew").hasRole("ADMIN")
                // everything else needs auth
                .anyRequest().authenticated()
        )
        .formLogin(log -> log.loginPage("/secure/login").permitAll()
                .loginProcessingUrl("/secure/processing_login").permitAll()
                .defaultSuccessUrl("/hello")
                .failureUrl("/secure/login?error=true").permitAll())
        .logout(logout -> logout.logoutUrl("/secure/logout")
                .logoutSuccessUrl("/secure/login"))
        .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    // for api requests return 401 json, for everything else redirect to login
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(401);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"error\":\"Необходима авторизация\"}");
                    } else {
                        response.sendRedirect("/secure/login");
                    }
                })
        )
        .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return this.userDetailsService;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(getPasswordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(authenticationProvider())
                .build();
    }

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
