package com.mindvault.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public JwtAuthenticationFilter(final JwtUtils jwtUtils, final UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                String emailFromToken = jwtUtils.extractUsername(token);
                logger.info("JWT Token receive for email : {}", emailFromToken);

                if (emailFromToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(emailFromToken);
                    String userNameFromToken = jwtUtils.extractUsername(token);
                    logger.info("JWT Token receive for username : {}", userNameFromToken);

                    boolean valid = jwtUtils.validateToken(token, userNameFromToken);
                    logger.info("JWT Token is valid ? {}", valid);

                    if (valid) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.info("Authentication successful for : {}", emailFromToken);
                    } else {
                        logger.warn("Authentication failed for user : {}", emailFromToken);
                    }
                }
            } catch (Exception e) {
                logger.error("Error during the JWT token validation process : {}", e.getMessage());
            }
        } else {
            logger.debug("Header empty or compromise");
        }

        filterChain.doFilter(request, response);
    }
}
