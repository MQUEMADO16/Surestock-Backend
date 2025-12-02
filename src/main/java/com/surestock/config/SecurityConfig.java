package com.surestock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF to allow Postman/React to send POST requests easily
                .csrf(AbstractHttpConfigurer::disable)
                // Configure URL access
                .authorizeHttpRequests(auth -> auth
                        // Allow unrestricted access to all /api/ endpoints for dev
                        .requestMatchers("/api/**").permitAll()
                        // Require authentication for anything else
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}