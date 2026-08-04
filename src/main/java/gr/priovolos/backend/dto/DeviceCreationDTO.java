package gr.priovolos.backend.dto;

import jakarta.validation.constraints.*;

/**
 * Data Transfer Object representing the information required to
 * register a new network device.
 *
 * <p>This DTO is submitted by clients when creating a new device.
 * It contains the device identification details, network connection
 * information, and the SSH credentials required for remote
 * administration.</p>
 *
 * <p>All fields are validated before processing to ensure that only
 * valid device information is accepted. The supplied SSH password is
 * encrypted before being stored in the database.</p>
 *
 * <p>Instances of this record are immutable and intended for
 * request payloads.</p>
 *
 * @param title the descriptive name assigned to the network device
 * @param manufacturer the device manufacturer
 *                     (for example, Cisco, MikroTik, Aruba)
 * @param model the device model
 * @param ipAddress the IPv4 address used to establish SSH
 *                  communication with the device
 * @param sshPort the TCP port used for SSH communication
 * @param username the SSH username used to authenticate to the device
 * @param password the SSH password used to authenticate to the device
 *
 * @author Ioannis Priovolos
 */
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
