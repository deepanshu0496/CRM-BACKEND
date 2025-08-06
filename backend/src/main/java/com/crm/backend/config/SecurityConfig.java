package com.crm.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
       @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // ✅ Allow unauthenticated access to OTP endpoints
                .requestMatchers("/api/v1/auth/otp/**").permitAll()

                // ✅ Allow access to any other public endpoints (optional)
                // .requestMatchers("/api/v1/public/**").permitAll()

                // 🔐 All other endpoints require authentication
                .anyRequest().authenticated()
            );

        return http.build();
    }

@Autowired
private JwtAuthFilter jwtAuthFilter;


    @Bean
public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("*");
        }
    };
}
}
