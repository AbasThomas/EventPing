package thomas.com.EventPing.security.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.User.repository.UserRepository;
import thomas.com.EventPing.security.service.JwtAuthenticationService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * JWT Authentication Filter for processing JWT tokens in requests
 * **Validates: Requirements 1.1, 1.2, 1.4**
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtAuthenticationService jwtAuthenticationService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = extractJwtFromRequest(request);
            
            if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                authenticateUser(jwt, request);
            }
        } catch (JwtException e) {
            log.warn("JWT authentication failed: {}", e.getMessage());
            // Continue with the filter chain - let Spring Security handle the unauthenticated request
        } catch (Exception e) {
            log.error("Unexpected error during JWT authentication", e);
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }

    private void authenticateUser(String jwt, HttpServletRequest request) {
        try {
            // Validate the JWT token
            Claims claims = jwtAuthenticationService.validateToken(jwt);
            
            // Extract user information from claims
            Long userId = claims.get("userId", Long.class);
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);
            
            if (userId == null || email == null || role == null) {
                log.warn("JWT token missing required claims");
                return;
            }

            // Load user from database to ensure they still exist and are active
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                log.warn("User not found for JWT token: {}", userId);
                return;
            }

            User user = userOpt.get();
            
            // Check if account is locked
            if (user.getAccountLocked()) {
                log.warn("Authentication attempt for locked account: {}", email);
                return;
            }

            // Verify email matches (additional security check)
            if (!user.getEmail().equals(email)) {
                log.warn("Email mismatch in JWT token for user: {}", userId);
                return;
            }

            // Create authentication token
            List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
            );

            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(user, null, authorities);
            
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            log.debug("Successfully authenticated user: {}", email);
            
        } catch (JwtException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            throw e; // Re-throw to be caught by outer try-catch
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Skip JWT authentication for public endpoints
        return path.startsWith("/api/auth/") ||
               path.startsWith("/actuator/health") ||
               (path.startsWith("/api/participants/events/") && path.endsWith("/join")) ||
               (path.startsWith("/api/events/") && "GET".equals(request.getMethod()));
    }
}