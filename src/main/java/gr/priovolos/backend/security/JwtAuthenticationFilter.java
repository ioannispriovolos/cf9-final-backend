package gr.priovolos.backend.security;

import gr.priovolos.backend.authentication.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        String username = "";

        // If the request has no JWT token, continue to the next filter.
        // The next filter will decide if the request is for permit-all or not.
        // If permit-all, then the request will be allowed.
        // If not, then the request will be denied because the security context will be empty.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7).trim();
        try {
            username = jwtService.extractSubject(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (!jwtService.isTokenValid(jwt, userDetails)) {
                    throw new BadCredentialsException("Invalid Token");
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
                Authentication a = SecurityContextHolder.getContext().getAuthentication();
                System.out.println("principal class = " + a.getPrincipal().getClass());
                System.out.println("authorities = " + a.getAuthorities());
            }
        } catch (ExpiredJwtException e) {
            // triggers το AuthenticationEntryPoint 401
            throw new AuthenticationCredentialsNotFoundException("Expired token", e);
        } catch (JwtException | IllegalArgumentException e) {
            // triggers το AuthenticationEntryPoint 401
            throw new BadCredentialsException("Invalid token");
        } catch (BadCredentialsException e) {
            // Just leave it to move to the next filter
            // Otherwise will be caught by generic exception handler
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during token validation", e);
            throw new AuthenticationCredentialsNotFoundException("Token validation failed", e);
        }

        /*
         / UsernameNotFoundException hits catch (Exception e) →
         / AuthenticationCredentialsNotFoundException → 401.
         / Clean and no duplication
        */

        filterChain.doFilter(request, response);
    }
}
