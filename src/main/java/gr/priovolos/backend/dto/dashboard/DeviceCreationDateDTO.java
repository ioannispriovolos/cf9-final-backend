package gr.priovolos.backend.dto.dashboard;

import java.time.Instant;

public record DeviceCreationDateDTO(
        Instant createdAt
) {
}
