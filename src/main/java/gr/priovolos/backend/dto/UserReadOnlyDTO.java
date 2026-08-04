package gr.priovolos.backend.dto;

import java.util.UUID;

/**
 * Read-only Data Transfer Object representing a user account.
 *
 * <p>This DTO is returned by user management endpoints and contains
 * non-sensitive information about an application user.</p>
 *
 * <p>For security reasons, confidential information such as the
 * user's password and internal authorization details are intentionally
 * excluded from this response.</p>
 *
 * <p>Instances of this record are immutable and intended exclusively
 * for API responses.</p>
 *
 * @param uuid the public unique identifier of the user
 * @param username the username of the user
 * @param role the name of the role assigned to the user
 *
 * @author Ioannis Priovolos
 */
public record UserReadOnlyDTO(UUID uuid, String username, String role) {
}

