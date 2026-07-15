package gr.priovolos.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SshCommandRequestDTO(
        @NotBlank(message = "Host IP address cannot be blank")
        @Pattern(
                regexp = "^(?:(?:25[0-5]|2[0-4]\\d|1?\\d{1,2})(?:\\.(?!$)|$)){4}$",
                message = "Must be a valid IPv4 address"
        )
        String host,

        @Min(value = 1) @Max(value = 65535)
        int port,

        @NotBlank(message = "Username cannot be blank")
        String username,

        @NotBlank(message = "Password cannot be blank")
        String password,

        @NotBlank(message = "Command cannot be blank")
        String command
) {}