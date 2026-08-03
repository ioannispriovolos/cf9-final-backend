package gr.priovolos.backend.controller;

import gr.priovolos.backend.core.exceptions.EntityAlreadyExistsException;
import gr.priovolos.backend.core.exceptions.EntityInvalidArgumentException;
import gr.priovolos.backend.core.exceptions.EntityNotFoundException;
import gr.priovolos.backend.core.exceptions.ValidationException;
import gr.priovolos.backend.dto.*;
import gr.priovolos.backend.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST controller responsible for managing application users.
 *
 * <p>This controller exposes endpoints for creating, retrieving,
 * updating, paginating and soft deleting user accounts.</p>
 *
 * <p>User management operations are intended for authenticated
 * administrators with the appropriate capabilities.
 * Authorization is enforced by Spring Security.</p>
 *
 * <p>The application uses a soft-delete mechanism, therefore deleted
 * users remain stored in the database but are excluded from normal
 * retrieval operations.</p>
 *
 * @author Ioannis Priovolos
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "Create, get paginated, update and soft-delete users for authorized administrators")
public class UserController {

    /**
     * Service responsible for user management business operations.
     */
    private final IUserService userService;

    /**
     * Registers a new user account.
     *
     * <p>The supplied request is validated before being processed.
     * The user's password is securely encoded before being stored
     * and the appropriate role is assigned.</p>
     *
     * <p>Upon successful creation, HTTP 201 (Created) is returned
     * together with the URI of the newly created user.</p>
     *
     * @param userInsertDTO the user registration request
     * @param bindingResult contains validation errors detected during
     *                      request binding
     * @return the newly created user
     * @throws ValidationException if request validation fails
     * @throws EntityInvalidArgumentException if invalid business data is supplied
     * @throws EntityAlreadyExistsException if the user already exists
     */
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account in the system."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserReadOnlyDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ValidationErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<UserReadOnlyDTO> registerUser(@Valid @RequestBody UserInsertDTO userInsertDTO, BindingResult bindingResult)
            throws ValidationException, EntityInvalidArgumentException, EntityAlreadyExistsException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("User", "User validation error", bindingResult);
        }

        UserReadOnlyDTO userReadOnlyDTO = userService.saveUser(userInsertDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(userReadOnlyDTO.uuid())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(userReadOnlyDTO);
    }

    /**
     * Retrieves an active user by UUID.
     *
     * <p>Only users that have not been soft deleted can be
     * retrieved through this endpoint.</p>
     *
     * @param uuid the unique identifier of the requested user
     * @return the requested user
     * @throws EntityNotFoundException if the user does not exist
     *                                 or has been soft deleted
     */
    @Operation(
            summary = "Get user by UUID",
            description = "Retrieves a non-deleted user by their UUID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserReadOnlyDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<UserReadOnlyDTO> getUserByUuid(@PathVariable UUID uuid) throws EntityNotFoundException {

        return ResponseEntity.ok(userService.getUserByUUIDDeletedFalse(uuid));
    }

    /**
     * Soft deletes an existing user.
     *
     * <p>The user is not permanently removed from the database.
     * Instead, the deleted flag is set and the deletion timestamp
     * is recorded.</p>
     *
     * <p>Soft-deleted users are automatically excluded from
     * authentication and normal retrieval operations.</p>
     *
     * @param uuid the UUID of the user to softly delete
     * @return the soft-deleted user's information
     * @throws EntityNotFoundException if the specified user
     *                                 does not exist
     */
    @Operation(
            summary = "Soft delete user",
            description = """
                Soft deletes a user by UUID.
                
                The user is not permanently removed from the database.
                Instead, the user's deleted flag is set to true and the deletion timestamp is recorded.
                
                Requires the DELETE_USER capability.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User was successfully soft deleted.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserReadOnlyDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid UUID supplied.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not have permission to delete users.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found.",
                    content = @Content
            )
    })
    @PatchMapping("/{uuid}")
    public ResponseEntity<UserReadOnlyDTO> deleteUser(@PathVariable UUID uuid) throws EntityNotFoundException {

        UserReadOnlyDTO readOnlyDTO = userService.deleteUserByUUID(uuid);
        return ResponseEntity.ok(readOnlyDTO);
    }

    /**
     * Retrieves a paginated list of active users.
     *
     * <p>Only non-soft-deleted users are included in the result.
     * Pagination and sorting are provided through Spring Data's
     * {@link Pageable} abstraction.</p>
     *
     * <p>Default pagination:
     * <ul>
     *     <li>Page: 0</li>
     *     <li>Size: 5</li>
     *     <li>Sort: username (ascending)</li>
     * </ul>
     *
     * @param pageable pagination and sorting information
     * @return a paginated list of active users
     */
    @Operation(
            summary = "Get all active users",
            description = """
                Retrieves a paginated list of all active (non-deleted) users.
                
                Results are returned using Spring Data pagination.
                
                Default pagination:
                - Page: 0
                - Size: 5
                - Sort: username (ascending)
                
                Example:
                GET /api/v1/users/allusers?page=0&size=5&sort=username,asc
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Paginated list of users retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not have permission to view users.",
                    content = @Content
            )
    })
    @GetMapping("/allusers")
    public ResponseEntity<PageResponseDTO<UserReadOnlyDTO>> getUsers(@ParameterObject @PageableDefault(
                    size = 5,
                    sort = "username",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                userService.getAllUsersPaginated(pageable)
        );
    }


    /**
     * Updates an existing user.
     *
     * <p>The supplied request may update one or more user
     * properties depending on the values contained in the
     * request body.</p>
     *
     * <p>The service layer validates the supplied information,
     * securely encodes a new password if one is provided,
     * persists the changes and returns the updated user.</p>
     *
     * @param uuid the UUID of the user to update
     * @param dto the updated user information
     * @return the updated user
     * @throws EntityNotFoundException if the specified user
     *                                 does not exist
     */
    @Operation(
            summary = "Update user",
            description = """
                Updates an existing user identified by its UUID.
                
                The request may include one or more user fields to be updated,
                such as the username, password, or role, depending on the
                values provided in the request body.
                
                Returns the updated user information upon successful completion.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserReadOnlyDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or validation failed.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not have permission to update users.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found.",
                    content = @Content
            )
    })
    @PutMapping("/{uuid}")
    public ResponseEntity<UserReadOnlyDTO> updateUserByUuid(@PathVariable UUID uuid, @Valid @RequestBody UserUpdateDTO dto) throws EntityNotFoundException {
        return ResponseEntity.ok(userService.updateUserByUuid(uuid, dto));
    }
}
