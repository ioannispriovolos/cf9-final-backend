package gr.priovolos.backend.dto;

public record DeviceResponseDTO(
        Long id,
        String title,
        String manufacturer,
        String model,
        String ipAddress,
        Integer sshPort,
        String username
) {
}
