package gr.priovolos.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object representing the information required to
 * register a new application user.
 *
 * <p>This DTO is submitted by clients when creating a new user
 * account. It contains the user's credentials together with the
 * identifier of the role that will be assigned to the user.</p>
 *
 * <p>All fields are validated before processing to ensure that the
 * supplied information satisfies the application's validation and
 * security requirements.</p>
 *
 * <p>The supplied password is securely encoded before being stored
 * in the database. Plaintext passwords are never persisted.</p>
 *
 * <p>Instances of this record are immutable and intended for
 * request payloads.</p>
 *
 * @param username the unique username of the new user
 * @param password the user's plaintext password
 * @param roleId the identifier of the role assigned to the user
 *
 * @author Ioannis Priovolos
 */
public record UserInsertDTO(

        @NotNull
        @Size(min = 2, max = 20)
        String username,

        @NotNull
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])^.{8,}$")
        String password,

        @NotNull
        Long roleId
) {

}

