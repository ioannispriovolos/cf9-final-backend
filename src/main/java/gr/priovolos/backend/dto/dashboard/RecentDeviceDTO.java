package gr.priovolos.backend.dto.dashboard;

import java.time.Instant;

/**
 * Read-only Data Transfer Object representing a recently registered
 * network device.
 *
 * <p>This DTO is primarily used by the dashboard module to display
 * the latest network devices that have been added
 * within the system.</p>
 *
 * <p>Only non-sensitive device information is included. SSH
 * credentials are intentionally excluded to prevent exposure of
 * confidential information.</p>
 *
 * <p>Instances of this record are immutable and intended exclusively
 * for API responses.</p>
 *
 * @param id the unique identifier of the network device
 * @param title the descriptive name assigned to the device
 * @param manufacturer the device manufacturer
 *                     (for example, Cisco, MikroTik, Aruba)
 * @param model the device model
 * @param ipAddress the IPv4 address used to establish
 *                  SSH connectivity
 * @param sshPort the SSH port configured for the device
 * @param updatedAt the UTC timestamp indicating the most recent
 *                  update performed on the device
 *
 * @author Ioannis Priovolos
 */
public record RecentDeviceDTO(
        Long id,
        String title,
        String manufacturer,
        String model,
        String ipAddress,
        Integer sshPort,
        Instant updatedAt
) {
}