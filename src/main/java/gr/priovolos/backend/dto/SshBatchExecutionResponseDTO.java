package gr.priovolos.backend.dto;

import java.util.List;

public record SshBatchExecutionResponseDTO(

        int requestedDevices,
        int successfulDevices,
        int failedDevices,
        long durationMs,
        List<SshCommandResultDTO> results
) {
}