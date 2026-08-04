package gr.priovolos.backend.dto;

import java.util.List;

/**
 * Read-only Data Transfer Object representing the aggregated result
 * of executing an SSH command on multiple network devices.
 *
 * <p>This DTO is returned after a batch SSH execution request has
 * completed. It provides execution statistics together with the
 * individual execution result for each processed network device.</p>
 *
 * <p>The response allows client applications to determine the
 * overall outcome of the operation while also inspecting the
 * result of each individual device.</p>
 *
 * <p>Instances of this record are immutable and intended exclusively
 * for API responses.</p>
 *
 * @param requestedDevices the total number of devices included in
 *                         the execution request
 * @param successfulDevices the number of devices on which the
 *                          command executed successfully
 * @param failedDevices the number of devices for which the command
 *                      execution failed
 * @param durationMs the total execution time of the batch operation,
 *                   expressed in milliseconds
 * @param results the individual execution results for each processed
 *                network device
 *
 * @author Ioannis Priovolos
 */

public record SshBatchExecutionResponseDTO(

        int requestedDevices,
        int successfulDevices,
        int failedDevices,
        long durationMs,
        List<SshCommandResultDTO> results
) {
}