package gr.priovolos.backend.mapper;

import gr.priovolos.backend.dto.RoleReadOnlyDTO;
import gr.priovolos.backend.dto.UserInsertDTO;
import gr.priovolos.backend.dto.UserReadOnlyDTO;
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
}
