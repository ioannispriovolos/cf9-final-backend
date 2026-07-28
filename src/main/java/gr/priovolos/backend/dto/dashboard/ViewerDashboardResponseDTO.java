package gr.priovolos.backend.dto.dashboard;

import java.util.List;

public record ViewerDashboardResponseDTO(

        long activeDevices,
        long totalManufacturers,
        long totalModels,
        long devicesAddedThisMonth,

        List<DashboardCountDTO> devicesByManufacturer,
        List<DashboardCountDTO> devicesByModel,
        List<MonthlyDeviceCountDTO> devicesAddedByMonth,
        List<RecentDeviceDTO> recentlyUpdatedDevices
) {
}
