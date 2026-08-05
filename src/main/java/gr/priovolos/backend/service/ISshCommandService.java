package gr.priovolos.backend.service;

import gr.priovolos.backend.core.exceptions.EntityNotFoundException;
import gr.priovolos.backend.dto.ExecuteSshCommandRequestDTO;
import gr.priovolos.backend.dto.SshBatchExecutionResponseDTO;

/**
 * Service interface defining operations for executing SSH commands
 * on managed network devices.
 *
 * <p>This service coordinates secure SSH communication with one or
 * more selected network devices. Implementations are responsible for
 * validating the execution request, retrieving the target devices,
 * establishing SSH connections, executing the requested command, and
 * aggregating the execution results.</p>
 *
 * <p>The returned response contains both overall execution statistics
 * and the individual result for each processed device, allowing client
 * applications to display detailed execution information.</p>
 *
 * @author Ioannis Priovolos
 */
public interface ISshCommandService {

    /**
     * Executes an SSH command on the selected network devices.
     *
     * <p>The supplied request contains the identifiers of the target
     * devices together with the command to execute. Only active
     * (non-deleted) devices are processed.</p>
     *
     * <p>The returned response includes execution statistics such as
     * the number of successful and failed executions, the total
     * execution duration, and the detailed result for every device.</p>
     *
     * @param request the SSH command execution request containing
     *                the target device identifiers and command
     * @return the aggregated execution results for all processed
     *         devices
     * @throws EntityNotFoundException if one or more specified
     *                                 devices cannot be found
     */
    SshBatchExecutionResponseDTO executeOnSelectedDevices(
            ExecuteSshCommandRequestDTO request
    ) throws EntityNotFoundException;
}