package com.creditbuilder.api.security;

import com.creditbuilder.api.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Get the Authorization
        final String authHeader = request.getHeader("Authorization");

        // 2. If no token, skip this filter - public endpoint
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract the token (remove "Bearer" prefix)

        try {
            final String jwt = authHeader.substring(7);
            final String email = jwtService.extractEmail(jwt);

            // 4. If we have an email and user isn't aLready authentication
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // 5. validate the token
                if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 6. Tell Spring Security this user is authorised
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("JWT: " + jwt);
                    System.out.println("EMAIL FROM TOKEN: " + email);
                    System.out.println("USER FROM DB: " + userDetails.getUsername());

                    boolean isValid = jwtService.isTokenValid(jwt, userDetails.getUsername());
                    System.out.println("IS TOKEN VALID: " + isValid);
                }
            }
        } catch (Exception e){
            System.out.println("JWT ERROR: " + e.getMessage());
        }
        filterChain.doFilter(request, response);
    }
}
