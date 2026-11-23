
package com.fincore.identity.config;

import com.fincore.identity.entity.User;
import com.fincore.identity.repository.UserRepository;
import com.fincore.identity.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {

        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            if (!jwtService.isTokenValid(token)) {
                response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid or expired token"
                );
                return;
            }

            String email = jwtService.extractEmail(token);

            User user = userRepository.findByEmail(email)
                .filter(User::isActive)
                .orElse(null);

            if (user == null) {
                response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "User is unavailable"
                );
                return;
            }

            var authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole())
            );

            var authentication =
                new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    null,
                    authorities
                );

            SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        } catch (io.jsonwebtoken.JwtException |
                 IllegalArgumentException ex) {

            SecurityContextHolder.clearContext();

            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Invalid or expired token"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}
