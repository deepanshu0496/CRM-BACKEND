package com.crm.backend.config;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.crm.backend.services.JwtService;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter implements Filter {

    @Autowired
    private JwtService jwtService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();

        // ✅ Allow public endpoints
        if (path.startsWith("/api/v1/auth/otp")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = req.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtService.validateToken(token)) {
                String userEmail = jwtService.getEmailFromToken(token);
                String userRole = jwtService.getRoleFromToken(token);     // you must implement extractRole()

                // ✅ Set authentication in SecurityContext
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userEmail, null, List.of(new SimpleGrantedAuthority(userRole))
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);

                chain.doFilter(request, response);
                return;
            }
        }

        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.getWriter().write("Unauthorized");
    }
}
