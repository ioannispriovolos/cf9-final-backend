package gr.priovolos.backend.dto.dashboard;

import java.time.Instant;

/**
 * Read-only Data Transfer Object representing the creation timestamp
 * of a network device.
 *
 * <p>This DTO is primarily used by the dashboard module to transfer
 * device creation dates for statistical analysis and visualization,
 * such as monthly device registration trends.</p>
 *
 * <p>Instances of this record are immutable and intended exclusively
 * for API responses.</p>
 *
 * @param createdAt the UTC timestamp indicating when the network
 *                  device was registered in the system
 *
 * @author Ioannis Priovolos
 */
public record DeviceCreationDateDTO(
        Instant createdAt
) {
}
