package gr.priovolos.backend.dto;

import jakarta.validation.constraints.*;

import java.util.List;

/**
 * Data Transfer Object representing a batch SSH command execution request.
 *
 * <p>This DTO is submitted by the client when requesting the execution
 * of an SSH command on one or more managed network devices.</p>
 *
 * <p>The request contains the identifiers of the target devices and
 * the user-supplied command that will be executed independently on
 * each selected device.</p>
 *
 * <p>Validation constraints ensure that the request contains at least
 * one valid device identifier and that the SSH command length remains
 * within the configured application limits.</p>
 *
 * <p>Instances of this record are immutable and intended for
 * request payloads.</p>
 *
 * @param deviceIds the identifiers of the network devices on which
 *                  the SSH command will be executed
 * @param command the SSH command to execute on every selected device
 *
 * @author Ioannis Priovolos
 */
public record ExecuteSshCommandRequestDTO(

        @NotEmpty(message = "At least one device must be selected.")
        @Size(
                max = 70,
                message = "A maximum of 70 devices may be selected."
        )
        List<
                        @Positive(message = "Device IDs must be positive.")
                                Long
                        > deviceIds,

        @NotBlank(message = "SSH command is required.")
        @Size(
                max = 2000,
                message = "The SSH command must not exceed 2000 characters."
        )
        String command
) {
}