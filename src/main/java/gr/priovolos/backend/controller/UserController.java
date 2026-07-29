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

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "Create, get paginated, update and soft-delete users for authorized administrators")
public class UserController {

    private final IUserService userService;

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
                    responseCode = "409",
                    description = "User already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
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
        // user validator business rules TODO

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
//        return ResponseEntity.ok(teacherService.getTeacherByUUID(uuid));
        return ResponseEntity.ok(userService.getUserByUUIDDeletedFalse(uuid));
    }

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

//    @GetMapping("/allusers")
//    public ResponseEntity<List<UserReadOnlyDTO>> getAllUsers() {
//        List<UserReadOnlyDTO> users = userService.getAllUsersReadOnly();
//        return ResponseEntity.ok(users);
//    }

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
