package project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF с Cookie-хранилищем и SLURL-режимом для HTMX
        http.csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()) // SLURL для HTMX
        );

        http
                .authorizeHttpRequests(auth -> auth
                        // Доступно всем
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers("/neuronets/**").permitAll()
                        .requestMatchers("/users/register", "/users/login").permitAll()
                        // Только для авторизованных
                        .requestMatchers("/reviews/**").authenticated()
                        // Всё остальное
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/users/login")
                        .loginProcessingUrl("/users/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/users/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/users/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }
}