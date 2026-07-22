package gr.priovolos.backend.mapper;

import gr.priovolos.backend.dto.*;
import gr.priovolos.backend.model.Device;
import gr.priovolos.backend.model.Role;
import gr.priovolos.backend.model.User;
import org.springframework.stereotype.Component;

@Component
public class Mapper {
    public User mapToUserEntity(UserInsertDTO userInsertDTO) {
        return new User(userInsertDTO.username(), userInsertDTO.password());
    }

    public UserReadOnlyDTO mapToUserReadOnlyDTO(User user) {
        return new UserReadOnlyDTO(user.getUuid(), user.getUsername(), user.getRole().getName());
    }

    public RoleReadOnlyDTO mapToRoleReadOnlyDTO(Role role) {
        return new RoleReadOnlyDTO(role.getId(), role.getName());
    }

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
}
