package gr.priovolos.backend.dto;

/**
 * Read-only Data Transfer Object representing a managed network device.
 *
 * <p>This DTO is returned by the device management endpoints and
 * contains the non-sensitive information of a registered network
 * device.</p>
 *
 * <p>For security reasons, the device's SSH password is intentionally
 * omitted from this response. Although the password is stored in the
 * database in encrypted form, it is never exposed through the REST API.</p>
 *
 * <p>Instances of this record are immutable and intended exclusively
 * for API responses.</p>
 *
 * @param id the unique identifier of the network device
 * @param title the descriptive name assigned to the device
 * @param manufacturer the device manufacturer
 *                     (for example, Cisco, MikroTik, Aruba)
 * @param model the device model
 * @param ipAddress the IPv4 address used to establish SSH
 *                  communication with the device
 * @param sshPort the TCP port used for SSH communication
 * @param username the SSH username configured for the device
 *
 * @author Ioannis Priovolos
 */
public record DeviceResponseDTO(
        Long id,
        String title,
        String manufacturer,
        String model,
        String ipAddress,
        Integer sshPort,
        String username
) {
}
