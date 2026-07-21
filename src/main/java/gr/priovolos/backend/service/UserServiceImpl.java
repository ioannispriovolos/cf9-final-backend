package gr.priovolos.backend.service;

import gr.priovolos.backend.core.exceptions.EntityAlreadyExistsException;
import gr.priovolos.backend.core.exceptions.EntityInvalidArgumentException;
import gr.priovolos.backend.core.exceptions.EntityNotFoundException;
import gr.priovolos.backend.dto.UserInsertDTO;
import gr.priovolos.backend.dto.UserReadOnlyDTO;
import gr.priovolos.backend.dto.UserUpdateDTO;
import gr.priovolos.backend.mapper.Mapper;
import gr.priovolos.backend.model.Role;
import gr.priovolos.backend.model.User;
import gr.priovolos.backend.repository.RoleRepository;
import gr.priovolos.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    @Transactional(rollbackFor = { EntityAlreadyExistsException.class, EntityInvalidArgumentException.class })
    public UserReadOnlyDTO saveUser(UserInsertDTO userInsertDTO)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException {
        try {
            if (userRepository.findByUsername(userInsertDTO.username()).isPresent()) {
                throw new EntityAlreadyExistsException("User","User with username=" + userInsertDTO.username() + " already exists");
            }
            User user = mapper.mapToUserEntity(userInsertDTO);
            user.setPassword(passwordEncoder.encode(userInsertDTO.password()));

//            if (userInsertDTO.roleId() == 3)
//                throw new EntityInvalidArgumentException("Role","Role id=" + userInsertDTO.roleId() + " invalid. User can not have teacher role");

            Role role = roleRepository.findById(userInsertDTO.roleId())
                    .orElseThrow(() -> new EntityInvalidArgumentException("Role","Role id=" + userInsertDTO.roleId() + " invalid"));
            role.addUser(user);
            userRepository.save(user);
            log.info("Save succeeded for user with username={}.", userInsertDTO.username());
            return mapper.mapToUserReadOnlyDTO(user);
        } catch (EntityAlreadyExistsException e) {
            log.error("Save failed. User with username={} already exists", userInsertDTO.username());
            throw e;
        } catch (EntityInvalidArgumentException e) {
            log.error("Save failed. Invalid arguments for user with username={}", userInsertDTO.username());
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional(readOnly = true)
    public UserReadOnlyDTO getUserByUUID(UUID uuid) throws EntityNotFoundException {
        try {
            User user = userRepository.findByUuid(uuid)
                    .orElseThrow(() -> new EntityNotFoundException("User","User with uuid=" + uuid + " not found"));
            log.debug("Get user by uuid={} returned successfully", uuid);
            return mapper.mapToUserReadOnlyDTO(user);
        } catch (EntityNotFoundException e) {
            log.error("Get user by uuid={} failed", uuid, e);
            throw e;
        }
    }

    @Override
//    @PreAuthorize("hasAuthority('EDIT_TEACHER')")
//    @PreAuthorize("hasAuthority('VIEW_TEACHER') or (hasAuthority('VIEW_ONLY_TEACHER') and #uuid == authentication.principal.uuid)")
    @Transactional(readOnly = true)
    public UserReadOnlyDTO getUserByUUIDDeletedFalse(UUID uuid) throws EntityNotFoundException {

        try {
            User user = userRepository.findByUuidAndDeletedFalse(uuid)
                    .orElseThrow(() -> new EntityNotFoundException("User","User with uuid=" + uuid + " not found"));
            log.debug("Get non-deleted user by uuid={} returned successfully", uuid);
            return mapper.mapToUserReadOnlyDTO(user);
        } catch (EntityNotFoundException e) {
            log.error("Get user by uuid={} failed", uuid, e);
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasAuthority('DELETE_USER')")
    @Transactional(rollbackFor = EntityNotFoundException.class)
    public UserReadOnlyDTO deleteUserByUUID(UUID uuid) throws EntityNotFoundException {
        try {
            User user = userRepository.findByUuidAndDeletedFalse(uuid)
                    .orElseThrow(() -> new EntityNotFoundException("Teacher","Teacher with uuid=" + uuid + " not found"));

            user.softDelete();
            log.info("User with uuid={} deleted successfully", uuid);
            return mapper.mapToUserReadOnlyDTO(user);
        } catch (EntityNotFoundException e) {
            log.error("Update failed for teacher with uuid={}. Teacher not found", uuid, e);
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('VIEW_USERS')")
    @Transactional(readOnly = true)
    public List<UserReadOnlyDTO> getAllUsersReadOnly() {
        return userRepository.findAllByDeletedFalse().stream()
                .map(user -> new UserReadOnlyDTO(
                        user.getUuid(),
                        user.getUsername(),
                        user.getRole().getName()
                ))
                .toList();
    }

    @PreAuthorize("hasAuthority('EDIT_USER')")
    @Transactional
    @Override
    public UserReadOnlyDTO updateUserByUuid(UUID uuid, UserUpdateDTO dto) throws EntityNotFoundException {
        // 1. Fetch active user by UUID
        User user = userRepository.findByUuidAndDeletedFalse(uuid)
                .orElseThrow(() -> new EntityNotFoundException("User not found with UUID: ", uuid.toString()));

        // 2. Update username if provided and changed
        if (dto.username() != null && !dto.username().isBlank()) {
            user.setUsername(dto.username());
        }

        // 3. Update role ONLY if a roleId is explicitly passed in
        if (dto.roleId() != null) {
            Role role = roleRepository.findById(dto.roleId())
                    .orElseThrow(() -> new EntityNotFoundException("Role not found with ID: ", dto.roleId().toString()));
            user.setRole(role);
        }
        // If dto.roleId() is null, user.getRole() stays untouched!

        // 4. Save and return updated DTO
        User updatedUser = userRepository.save(user);

        return new UserReadOnlyDTO(
                updatedUser.getUuid(),
                updatedUser.getUsername(),
                updatedUser.getRole() != null ? updatedUser.getRole().getName() : "NO_ROLE"
        );
    }
}
