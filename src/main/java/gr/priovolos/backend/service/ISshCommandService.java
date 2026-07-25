package gr.priovolos.backend.service;

import gr.priovolos.backend.core.exceptions.EntityNotFoundException;
import gr.priovolos.backend.dto.ExecuteSshCommandRequestDTO;
import gr.priovolos.backend.dto.SshBatchExecutionResponseDTO;

public interface ISshCommandService {

    SshBatchExecutionResponseDTO executeOnSelectedDevices(
            ExecuteSshCommandRequestDTO request
    ) throws EntityNotFoundException;
}