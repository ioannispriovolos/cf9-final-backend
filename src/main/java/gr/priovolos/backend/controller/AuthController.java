package gr.priovolos.backend.controller;

import gr.priovolos.backend.dto.AuthenticationRequestDTO;
import gr.priovolos.backend.dto.AuthenticationResponseDTO;
import gr.priovolos.backend.authentication.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller responsible for user authentication.
 *
 * <p>This controller exposes the authentication endpoint used by clients
 * to obtain a JSON Web Token (JWT). Users authenticate by providing
 * valid credentials, and upon successful authentication a signed JWT
 * is returned.</p>
 *
 * <p>The generated JWT must be included in the
 * {@code Authorization} request header using the Bearer authentication
 * scheme when accessing protected REST endpoints.</p>
 *
 * @author Ioannis Priovolos
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Controller", description = "Enter credentials to authenticate and to get a token for authorization")
public class AuthController {

    /**
     * Service responsible for authenticating users and
     * generating JWT tokens.
     */
    private final AuthenticationService authenticationService;


    /**
     * Authenticates a user using the supplied credentials.
     *
     * <p>If the supplied username and password are valid,
     * a signed JWT is generated and returned to the client.
     * The returned token should subsequently be included in
     * the {@code Authorization} header using the Bearer
     * authentication scheme.</p>
     *
     * @param authenticationRequestDTO the authentication request
     *                                 containing the user's username
     *                                 and password
     * @return a JWT wrapped inside an {@link AuthenticationResponseDTO}
     */
    @Operation(
            summary = "Authenticate user",
            description = "Returns JWT token for valid credentials")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponseDTO.class))),
            @ApiResponse(
                    responseCode = "401", description = "Unauthorized - Invalid credentials",
                    content = @Content),
            @ApiResponse(
                    responseCode = "400",  description = "Bad request - Missing/invalid parameters",
                    content = @Content)
    })
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponseDTO> authenticate(@Valid @RequestBody AuthenticationRequestDTO authenticationRequestDTO) {
        AuthenticationResponseDTO authenticationResponseDTO = authenticationService.authenticate(authenticationRequestDTO);
        return new ResponseEntity<>(authenticationResponseDTO, HttpStatus.OK);
    }
}