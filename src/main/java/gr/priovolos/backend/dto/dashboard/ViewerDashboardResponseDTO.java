package gr.priovolos.backend.dto.dashboard;

import java.util.List;

/**
 * Read-only Data Transfer Object representing the complete dashboard
 * returned to users with viewer access.
 *
 * <p>This DTO aggregates the statistical information required by the
 * dashboard, providing an overview of the managed network
 * infrastructure.</p>
 *
 * <p>The dashboard includes summary metrics, manufacturer and model
 * distributions, monthly device registration statistics, and the
 * most recently registered network devices.</p>
 *
 * <p>Only active (non-soft-deleted) devices are included in all
 * statistics and collections.</p>
 *
 * <p>Instances of this record are immutable and intended exclusively
 * for API responses.</p>
 *
 * @param activeDevices the total number of active network devices
 * @param totalManufacturers the number of distinct device manufacturers
 *                           represented in the system
 * @param totalModels the number of distinct device models represented
 *                    in the system
 * @param devicesAddedThisMonth the number of devices registered during
 *                              the current calendar month
 * @param devicesByManufacturer the distribution of devices grouped by
 *                              manufacturer
 * @param devicesByModel the distribution of devices grouped by model
 * @param devicesAddedByMonth the number of devices registered for each
 *                            month
 * @param recentlyCreatedDevices the most recently registered network
 *                               devices
 *
 * @author Ioannis Priovolos
 */
public record ViewerDashboardResponseDTO(

        long activeDevices,
        long totalManufacturers,
        long totalModels,
        long devicesAddedThisMonth,

        List<DashboardCountDTO> devicesByManufacturer,
        List<DashboardCountDTO> devicesByModel,
        List<MonthlyDeviceCountDTO> devicesAddedByMonth,
        List<RecentDeviceDTO> recentlyCreatedDevices
) {
}
