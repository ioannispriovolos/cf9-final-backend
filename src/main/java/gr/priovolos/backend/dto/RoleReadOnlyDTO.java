package gr.priovolos.backend.dto;

/**
 * Read-only Data Transfer Object representing an application role.
 *
 * <p>This DTO is used to transfer non-sensitive role information
 * between the backend and client applications. It contains only the
 * role identifier and its name, making it suitable for read
 * operations such as user management and role selection.</p>
 *
 * <p>Instances of this record are immutable and intended exclusively
 * for API responses.</p>
 *
 * @param id the unique identifier of the role
 * @param name the name of the role
 *             (for example, {@code ADMIN},
 *             {@code NETWORK_ENGINEER}, or {@code VIEWER})
 *
 * @author Ioannis Priovolos
 */
public record RoleReadOnlyDTO(Long id, String name) {
}