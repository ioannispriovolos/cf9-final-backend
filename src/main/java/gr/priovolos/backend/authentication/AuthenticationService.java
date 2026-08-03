package gr.priovolos.backend.authentication;

import gr.priovolos.backend.dto.AuthenticationRequestDTO;
import gr.priovolos.backend.dto.AuthenticationResponseDTO;
import gr.priovolos.backend.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Service responsible for authenticating users and issuing JSON Web Tokens (JWT).
 *
 * <p>This service validates user credentials using Spring Security's
 * {@link AuthenticationManager}. Upon successful authentication, a signed
 * JWT containing the authenticated user's identity and role is generated
 * and returned to the client.</p>
 *
 * <p>The generated JWT is subsequently used to authenticate requests to
 * protected REST endpoints.</p>
 *
 * @author Ioannis Priovolos
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    /**
     * Service responsible for generating and validating JWT tokens.
     */
    private final JwtService jwtService;

    /**
     * Spring Security authentication manager used to verify user credentials.
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Authenticates a user using the supplied username and password.
     *
     * <p>If authentication succeeds, a JWT containing the authenticated
     * user's username and role is generated and returned.</p>
     *
     * <p>If authentication fails, Spring Security throws an
     * {@link org.springframework.security.core.AuthenticationException},
     * which is handled by the application's security exception handlers.</p>
     *
     * @param dto the authentication request containing the user's username
     *            and password
     * @return an {@link AuthenticationResponseDTO} containing the generated JWT
     */
    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO dto) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password()));

        User user = (User) authentication.getPrincipal();
        String token = jwtService.generateToken(authentication.getName(), user.getRole().getName());
        return new AuthenticationResponseDTO(token);
    }
}