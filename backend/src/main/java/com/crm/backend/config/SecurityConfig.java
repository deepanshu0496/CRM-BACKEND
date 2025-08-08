package com.crm.backend.config;

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
            // ✅ Public OTP endpoints
            .requestMatchers("/api/v1/auth/otp/**").permitAll()

            // ✅ Example: Only `administrator` can access user management
            .requestMatchers("/api/v1/users/**").hasAuthority("administrator")

            // ✅ Example: Only Sales Manager or Representative can access leads
            .requestMatchers("/api/v1/sales/**").hasAnyAuthority("Sales Manager", "Sales Representative")

            // ✅ Example: Only Marketing Professionals
            .requestMatchers("/api/v1/marketing/**").hasAuthority("Marketing Professional")

            // ✅ Example: Only finance officers
            .requestMatchers("/api/v1/finance/**").hasAuthority("finance_officer")

            // ✅ All other endpoints require login
            .anyRequest().authenticated()
        );

        return http.build();
    }



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
