package gr.priovolos.backend.dto.dashboard;

import java.time.Instant;

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