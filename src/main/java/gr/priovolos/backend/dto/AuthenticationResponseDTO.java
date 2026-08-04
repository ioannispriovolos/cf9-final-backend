package gr.priovolos.backend.dto;

/**
 * Data Transfer Object representing a successful authentication response.
 *
 * <p>This DTO is returned by the authentication endpoint after a user
 * has been successfully authenticated. It contains the JSON Web Token
 * (JWT) that must be included in subsequent requests to access
 * protected REST endpoints.</p>
 *
 * <p>The token should be sent in the HTTP
 * {@code Authorization} header using the Bearer authentication
 * scheme:</p>
 *
 * <pre>
 * Authorization: Bearer &lt;jwt-token&gt;
 * </pre>
 *
 * <p>Instances of this record are immutable and intended exclusively
 * for API responses.</p>
 *
 * @param token the signed JWT access token generated for the
 *              authenticated user
 *
 * @author Ioannis Priovolos
 */
public record AuthenticationResponseDTO(String token) {

}