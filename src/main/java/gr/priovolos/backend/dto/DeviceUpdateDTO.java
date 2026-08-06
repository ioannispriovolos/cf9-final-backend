package gr.priovolos.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object containing optional device fields that may
 * be updated.
 *
 * <p>Fields omitted from the request remain unchanged. Fields that
 * are supplied must satisfy their corresponding validation rules.</p>
 *
 * @param title the updated device title
 * @param manufacturer the updated device manufacturer
 * @param model the updated device model
 * @param ipAddress the updated IPv4 address
 * @param sshPort the updated SSH port
 * @param username the updated SSH username
 *
 * @author Ioannis Priovolos
 */
public record DeviceUpdateDTO(

        @Size(max = 150)
        @Pattern(
                regexp = ".*\\S.*",
                message = "Title must not be blank."
        )
        String title,

        @Size(max = 100)
        @Pattern(
                regexp = ".*\\S.*",
                message = "Manufacturer must not be blank."
        )
        String manufacturer,

        @Size(max = 100)
        @Pattern(
                regexp = ".*\\S.*",
                message = "Model must not be blank."
        )
        String model,

        @Pattern(
                regexp = "^(?:(?:25[0-5]|2[0-4]\\d|1?\\d{1,2})(?:\\.(?!$)|$)){4}$",
                message = "Must be a valid IPv4 address."
        )
        String ipAddress,

        @Min(
                value = 1,
                message = "SSH port must be at least 1."
        )
        @Max(
                value = 65535,
                message = "SSH port must not exceed 65535."
        )
        Integer sshPort,

        @Size(max = 100)
        @Pattern(
                regexp = ".*\\S.*",
                message = "Username must not be blank."
        )
        String username

) {
    public boolean hasChanges() {
        return title != null
                || manufacturer != null
                || model != null
                || ipAddress != null
                || sshPort != null
                || username != null;
    }
}
