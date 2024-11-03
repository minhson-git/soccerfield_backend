package com.ms.test_api.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ms.test_api.service.JwtService;
import com.ms.test_api.service.UserService;

import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class PreFilter extends OncePerRequestFilter{

    private final UserService userService;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {    
        
        log.info("----------- PreFilter -------------");

        final String authorizationHeader = request.getHeader("Authorization");

        if (StringUtils.isBlank(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }


        final String token = authorizationHeader.substring(7);

        try {
            final String userName = jwtService.extractUsername(token);

            if (StringUtils.isNotEmpty(userName) && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userService.userDetailsService().loadUserByUsername(userName);
                
                if (jwtService.isValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                    );
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
            
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Lỗi xác thực token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Invalid or expired token\", \"status\": 403}");
        }
    }
}
