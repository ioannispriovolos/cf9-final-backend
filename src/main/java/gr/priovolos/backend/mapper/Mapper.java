package gr.priovolos.backend.mapper;

import gr.priovolos.backend.dto.*;
import gr.priovolos.backend.dto.dashboard.RecentDeviceDTO;
import gr.priovolos.backend.model.Device;
import gr.priovolos.backend.model.Role;
import gr.priovolos.backend.model.User;
import org.springframework.stereotype.Component;

/**
 * Spring component responsible for converting between domain entities
 * and Data Transfer Objects (DTOs).
 *
 * <p>This mapper centralizes object transformation logic, ensuring
 * that the service and controller layers remain focused on business
 * operations rather than data conversion.</p>
 *
 * <p>Using a dedicated mapper promotes consistency, improves
 * maintainability, and prevents duplication of mapping logic
 * throughout the application.</p>
 *
 * @author Ioannis Priovolos
 */
@Component
public class Mapper {

    /**
     * Converts a user registration request into a {@link User} entity.
     *
     * <p>The returned entity contains the basic user information.
     * Additional processing, such as password encoding and role
     * assignment, is performed by the service layer.</p>
     *
     * @param userInsertDTO the user registration request
     * @return the corresponding {@link User} entity
     */
    public User mapToUserEntity(UserInsertDTO userInsertDTO) {
        return new User(userInsertDTO.username(), userInsertDTO.password());
    }

    /**
     * Converts a {@link User} entity into a read-only user DTO.
     *
     * <p>Only non-sensitive user information is exposed. The user's
     * password and other internal security details are intentionally
     * omitted.</p>
     *
     * @param user the user entity
     * @return the corresponding {@link UserReadOnlyDTO}
     */
    public UserReadOnlyDTO mapToUserReadOnlyDTO(User user) {
        return new UserReadOnlyDTO(user.getUuid(), user.getUsername(), user.getRole().getName());
    }

    public RoleReadOnlyDTO mapToRoleReadOnlyDTO(Role role) {
        return new RoleReadOnlyDTO(role.getId(), role.getName());
    }

    /**
     * Converts a device creation request into a {@link Device} entity.
     *
     * <p>String values are trimmed before assignment to remove leading
     * and trailing whitespace. If no SSH port is supplied, the default
     * port {@code 22} is assigned.</p>
     *
     * <p>The device password is intentionally not mapped here because
     * it is encrypted by the service layer before being persisted.</p>
     *
     * @param dto the device creation request
     * @return the corresponding {@link Device} entity
     */
    public Device toDeviceEntity(DeviceCreationDTO dto) {

        Device device = new Device();

        device.setTitle(dto.title().trim());
        device.setManufacturer(dto.manufacturer().trim());
        device.setModel(dto.model().trim());
        device.setIpAddress(dto.ipAddress().trim());
        device.setSshPort(dto.sshPort() == null ? 22 : dto.sshPort());
        device.setUsername(dto.username().trim());

        return device;
    }

    /**
     * Converts a {@link Device} entity into a read-only device DTO.
     *
     * <p>Only non-sensitive device information is included in the
     * response. SSH credentials are intentionally excluded.</p>
     *
     * @param device the device entity
     * @return the corresponding {@link DeviceResponseDTO}
     */
    public DeviceResponseDTO toDeviceResponseDTO(Device device) {

        return new DeviceResponseDTO(
                device.getId(),
                device.getTitle(),
                device.getManufacturer(),
                device.getModel(),
                device.getIpAddress(),
                device.getSshPort(),
                device.getUsername()
        );
    }

    /**
     * Converts a {@link Device} entity into a dashboard DTO
     * representing a recently created or recently updated device.
     *
     * <p>This DTO is used by the viewer dashboard to display the most
     * recent network devices managed by the application.</p>
     *
     * @param device the device entity
     * @return the corresponding {@link RecentDeviceDTO}
     */
    public RecentDeviceDTO toRecentDeviceDTO(Device device) {

        return new RecentDeviceDTO(
                device.getId(),
                device.getTitle(),
                device.getManufacturer(),
                device.getModel(),
                device.getIpAddress(),
                device.getSshPort(),
                device.getUpdatedAt()
        );
    }
}
