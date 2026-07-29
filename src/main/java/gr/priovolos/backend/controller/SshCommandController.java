package gr.priovolos.backend.controller;

import gr.priovolos.backend.core.exceptions.EntityNotFoundException;
import gr.priovolos.backend.dto.ExecuteSshCommandRequestDTO;
import gr.priovolos.backend.dto.SshBatchExecutionResponseDTO;
import gr.priovolos.backend.service.ISshCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "SSH Command Controller", description = "Secure multi-vendor SSH command execution engine")
public class SshCommandController {

    private final ISshCommandService sshCommandService;

    @Operation(
            summary = "Execute SSH command on selected devices",
            description = """
                Executes a user-provided SSH command on one or more selected network devices.
                
                The request contains the command to execute and the list of target device IDs.
                The command is executed on each active (non-deleted) device, and the response
                contains the execution result for every device, including successful and failed
                executions, along with the total execution duration.
                
                Only authorized users with the required capability can execute SSH commands.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "SSH command executed successfully on the selected devices.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SshBatchExecutionResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or validation failed.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not have permission to execute SSH commands.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "One or more specified devices were not found.",
                    content = @Content
            )
    })
    @PostMapping("/execute")
    public ResponseEntity<SshBatchExecutionResponseDTO> executeCommand(@Valid @RequestBody ExecuteSshCommandRequestDTO request) throws EntityNotFoundException {
        return ResponseEntity.ok(
                sshCommandService.executeOnSelectedDevices(
                        request
                )
        );
    }
}