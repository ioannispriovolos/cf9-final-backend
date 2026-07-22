package gr.priovolos.backend.dto;

import jakarta.validation.constraints.*;

public record DeviceCreationDTO(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 100) String manufacturer,
        @NotBlank @Size(max = 100) String model,
        @NotBlank(message = "Host IP address cannot be blank")
        @Pattern(
                regexp = "^(?:(?:25[0-5]|2[0-4]\\d|1?\\d{1,2})(?:\\.(?!$)|$)){4}$",
                message = "Must be a valid IPv4 address"
        )
        String ipAddress,
        @NotNull @Min(value = 1) @Max(value = 65535)
        Integer sshPort,
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Size(max = 255) String password
) { }
