package gr.priovolos.backend.dto;

import jakarta.validation.constraints.*;

import java.util.List;

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