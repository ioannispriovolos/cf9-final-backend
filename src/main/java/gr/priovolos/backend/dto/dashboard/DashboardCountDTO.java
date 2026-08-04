package gr.priovolos.backend.dto.dashboard;

/**
 * Read-only Data Transfer Object representing a labeled numerical value
 * displayed on the dashboard.
 *
 * <p>This DTO is used to transfer aggregated statistical information,
 * such as the number of devices grouped by a specific category
 * (for example, manufacturer or model).</p>
 *
 * <p>Instances of this record are immutable and intended exclusively
 * for API responses.</p>
 *
 * @param label the descriptive label of the statistic
 *              (e.g. "Cisco", "MikroTik", "Router")
 * @param count the number of occurrences associated with the label
 *
 * @author Ioannis Priovolos
 */
public record DashboardCountDTO(
        String label,
        long count
) {
}