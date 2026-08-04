package gr.priovolos.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object representing the information required to
 * update an existing user account.
 *
 * <p>This DTO is submitted by clients when updating a user's
 * information. It allows modification of the user's username
 * and assigned role.</p>
 *
 * <p>All supplied values are validated before processing to ensure
 * that they satisfy the application's validation rules and business
 * constraints.</p>
 *
 * <p>Instances of this record are immutable and intended for
 * request payloads.</p>
 *
 * @param username the updated username of the user
 * @param roleId the identifier of the role to be assigned to
 *               the user
 *
 * @author Ioannis Priovolos
 */
public record UserUpdateDTO(
        @NotNull
        @Size(min = 2, max = 20)
        String username,

        Long roleId
) {
}
