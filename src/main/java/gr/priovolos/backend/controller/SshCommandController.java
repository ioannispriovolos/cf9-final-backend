package gr.priovolos.backend.controller;

import gr.priovolos.backend.core.exceptions.EntityNotFoundException;
import gr.priovolos.backend.dto.ExecuteSshCommandRequestDTO;
import gr.priovolos.backend.dto.SshBatchExecutionResponseDTO;
import gr.priovolos.backend.service.ISshCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ssh")
@RequiredArgsConstructor
public class SshCommandController {

    private final ISshCommandService sshCommandService;

    @PostMapping("/execute")
    public ResponseEntity<SshBatchExecutionResponseDTO>
    executeCommand(
            @Valid @RequestBody
            ExecuteSshCommandRequestDTO request
    ) throws EntityNotFoundException {
        return ResponseEntity.ok(
                sshCommandService.executeOnSelectedDevices(
                        request
                )
        );
    }
}