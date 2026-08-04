package gr.priovolos.backend.dto.dashboard;

/**
 * Read-only Data Transfer Object representing the number of network
 * devices registered during a specific month.
 *
 * <p>This DTO is used by the dashboard module to provide data for
 * time-based statistics and charts, such as monthly device
 * registration trends.</p>
 *
 * <p>Instances of this record are immutable and intended exclusively
 * for API responses.</p>
 *
 * @param month the month associated with the statistic
 *              (for example, "January", "Feb", or "2026-07",
 *              depending on the application's formatting)
 * @param count the number of devices registered during the
 *              specified month
 *
 * @author Ioannis Priovolos
 */
public record MonthlyDeviceCountDTO(
        String month,
        long count
) {
}