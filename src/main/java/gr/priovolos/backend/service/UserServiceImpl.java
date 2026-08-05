package gr.priovolos.backend.service;

import gr.priovolos.backend.core.exceptions.EntityAlreadyExistsException;
import gr.priovolos.backend.core.exceptions.EntityInvalidArgumentException;
import gr.priovolos.backend.core.exceptions.EntityNotFoundException;
import gr.priovolos.backend.dto.PageResponseDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service implementation responsible for managing application users.
 *
 * <p>This service implements the business operations defined by
 * {@link IUserService}, including user creation, retrieval, updating,
 * pagination, role assignment, password encoding, and soft deletion.</p>
 *
 * <p>User passwords are encoded with the configured
 * {@link PasswordEncoder} before persistence and are never returned
 * through API response DTOs.</p>
 *
 * <p>The service also applies method-level authorization to sensitive
 * operations through Spring Security's {@link PreAuthorize}
 * annotation.</p>
 *
 * @author Ioannis Priovolos
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    /**
     * Creates and persists a new application user.
     *
     * <p>The operation first verifies that the requested username is
     * not already registered. It then maps the request DTO to a user
     * entity, encodes the plaintext password, retrieves the requested
     * role, associates the role with the user, and persists the
     * resulting entity.</p>
     *
     * <p>The transaction is rolled back when a duplicate user or an
     * invalid role is detected.</p>
     *
     * @param userInsertDTO the information required to create the user
     * @return the newly created user's read-only representation
     * @throws EntityAlreadyExistsException if the username is already
     *                                      registered
     * @throws EntityInvalidArgumentException if the supplied role
     *                                        identifier is invalid
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(rollbackFor = { EntityAlreadyExistsException.class, EntityInvalidArgumentException.class })
    public UserReadOnlyDTO saveUser(UserInsertDTO userInsertDTO)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException {
        try {
            if (userRepository.findByUsername(userInsertDTO.username()).isPresent()) {
                throw new EntityAlreadyExistsException("User","User with username=" + userInsertDTO.username() + " already exists");
            }
            User user = mapper.mapToUserEntity(userInsertDTO);
            user.setPassword(passwordEncoder.encode(userInsertDTO.password()));

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

    /**
     * Retrieves an active user by their public UUID.
     *
     * <p>Soft-deleted users are excluded and are treated as unavailable.</p>
     *
     * @param uuid the public UUID of the requested user
     * @return the active user's read-only representation
     * @throws EntityNotFoundException if no active user exists with
     *                                 the supplied UUID
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
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

    /**
     * Soft deletes an active user.
     *
     * <p>The user remains stored in the database, but the deleted flag
     * is set to {@code true} and the deletion timestamp is recorded.
     * Because the {@code User} entity links its enabled state to its
     * deleted state, a soft-deleted user can no longer authenticate.</p>
     *
     * <p>Access requires the {@code DELETE_USER} capability.</p>
     *
     * @param uuid the public UUID of the user to soft delete
     * @return the soft-deleted user's read-only representation
     * @throws EntityNotFoundException if no active user exists with
     *                                 the supplied UUID
     */
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

    /**
     * Updates an active user's username and optionally their assigned role.
     *
     * <p>The username is updated when a non-blank value is supplied.
     * The role is changed only when the request contains a role
     * identifier; otherwise, the user's existing role remains
     * unchanged.</p>
     *
     * <p>Access requires the {@code EDIT_USER} capability.</p>
     *
     * @param uuid the public UUID of the user to update
     * @param dto the requested user changes
     * @return the updated user's read-only representation
     * @throws EntityNotFoundException if the user or requested role
     *                                 cannot be found
     */
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

    /**
     * Retrieves a paginated list of active users.
     *
     * <p>The repository excludes soft-deleted users and eagerly
     * retrieves each user's role. Entities are converted into
     * read-only DTOs before being returned.</p>
     *
     * <p>Access requires the {@code VIEW_USERS} capability.</p>
     *
     * @param pageable pagination and sorting information
     * @return a paginated response containing active users
     */
    @PreAuthorize("hasAuthority('VIEW_USERS')")
    @Transactional(readOnly = true)
    public PageResponseDTO<UserReadOnlyDTO> getAllUsersPaginated(
            Pageable pageable
    ) {

        Page<User> users =
                userRepository.findAllByDeletedFalse(pageable);


        return PageResponseDTO.from(
                users,
                user -> new UserReadOnlyDTO(
                        user.getUuid(),
                        user.getUsername(),
                        user.getRole().getName()
                )
        );
    }
}
