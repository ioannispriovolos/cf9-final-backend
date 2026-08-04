package gr.priovolos.backend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object representing an authentication request.
 *
 * <p>This DTO is used to transfer the credentials required to
 * authenticate a user. It is submitted to the authentication
 * endpoint, where the supplied username and password are validated
 * before a JSON Web Token (JWT) is issued.</p>
 *
 * <p>Both fields are mandatory and must be provided by the client.</p>
 *
 * <p>Instances of this record are immutable and intended for
 * request payloads.</p>
 *
 * @param username the username of the user attempting to authenticate
 * @param password the user's plaintext password
 *
 * @author Ioannis Priovolos
 */
public record AuthenticationRequestDTO(@NotNull String username, @NotNull String password) {

}