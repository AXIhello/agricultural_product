package com.example.agricultural_product.config;

import com.example.agricultural_product.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No JWT Token found in Authorization header. Passing to next filter.");
            filterChain.doFilter(request, response);
            System.out.println("====== JWT FILTER END ======");
            return;
        }

        final String token = authHeader.substring(7);

        try {
            Claims claims = JwtUtil.parseToken(token);
            String username = claims.get("userName", String.class);
            String role = claims.get("role", String.class);


            if (username != null && role != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                System.out.println("User is not authenticated yet. Proceeding to set authentication.");

                String authorityString = "ROLE_" + role.toUpperCase();
                
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority(authorityString)
                );

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
                
            } else {
                System.out.println("User is already authenticated or token info is incomplete.");
            }
        } catch (JwtException e) {
            System.out.println("!!! JWT Token processing failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
        System.out.println("====== JWT FILTER END ======");
    }
}