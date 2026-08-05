package gr.priovolos.backend.service;

import gr.priovolos.backend.core.exceptions.EntityAlreadyExistsException;
import gr.priovolos.backend.core.exceptions.EntityInvalidArgumentException;
import gr.priovolos.backend.core.exceptions.EntityNotFoundException;
import gr.priovolos.backend.dto.PageResponseDTO;
import gr.priovolos.backend.dto.UserInsertDTO;
import gr.priovolos.backend.dto.UserReadOnlyDTO;
import gr.priovolos.backend.dto.UserUpdateDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service interface defining business operations related to
 * application user management.
 *
 * <p>This service provides functionality for creating, retrieving,
 * updating, paginating, and soft deleting application users.
 * Implementations enforce the application's business rules,
 * validation requirements, and role assignment policies while
 * coordinating persistence operations.</p>
 *
 * <p>All methods return read-only Data Transfer Objects (DTOs),
 * ensuring that sensitive information such as user passwords is
 * never exposed outside the service layer.</p>
 *
 * @author Ioannis Priovolos
 */
public interface IUserService {

    /**
     * Creates a new application user.
     *
     * <p>The supplied user information is validated before
     * processing. The user's password is securely encoded before
     * being stored in the database.</p>
     *
     * @param userInsertDTO the user creation request
     * @return the newly created user
     * @throws EntityAlreadyExistsException if a user with the same
     *                                      username already exists
     * @throws EntityInvalidArgumentException if the supplied user
     *                                        information violates
     *                                        business rules
     */
    UserReadOnlyDTO saveUser(UserInsertDTO userInsertDTO)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException;


    /**
     * Retrieves a user by their public UUID.
     *
     * <p>This method may return both active and soft-deleted users,
     * depending on the implementation.</p>
     *
     * @param uuid the user's public UUID
     * @return the matching user
     * @throws EntityNotFoundException if the user cannot be found
     */
    UserReadOnlyDTO getUserByUUID(UUID uuid) throws EntityNotFoundException;

    /**
     * Retrieves an active (non-deleted) user by their public UUID.
     *
     * @param uuid the user's public UUID
     * @return the matching active user
     * @throws EntityNotFoundException if no active user exists with
     *                                 the specified UUID
     */
    UserReadOnlyDTO getUserByUUIDDeletedFalse(UUID uuid) throws EntityNotFoundException;

    /**
     * Performs a logical (soft) deletion of a user.
     *
     * <p>The user is not permanently removed from the database.
     * Instead, the user is marked as deleted and becomes unavailable
     * for authentication and normal application operations.</p>
     *
     * @param uuid the public UUID of the user to delete
     * @return the deleted user's information
     * @throws EntityNotFoundException if no active user exists with
     *                                 the specified UUID
     */
    UserReadOnlyDTO deleteUserByUUID(UUID uuid) throws EntityNotFoundException;

    /**
     * Updates an existing user's information.
     *
     * <p>The supplied data is validated before being applied to the
     * existing user account.</p>
     *
     * @param uuid the public UUID of the user to update
     * @param dto the updated user information
     * @return the updated user
     * @throws EntityNotFoundException if no user exists with the
     *                                 specified UUID
     */
    UserReadOnlyDTO updateUserByUuid(UUID uuid, UserUpdateDTO dto) throws EntityNotFoundException;

    /**
     * Retrieves a paginated list of active (non-deleted) users.
     *
     * <p>The returned response includes both the user data and
     * pagination metadata such as the current page, total pages,
     * and total number of users.</p>
     *
     * @param pageable pagination and sorting information
     * @return a paginated response containing active users
     */
    PageResponseDTO<UserReadOnlyDTO> getAllUsersPaginated(Pageable pageable);
}
