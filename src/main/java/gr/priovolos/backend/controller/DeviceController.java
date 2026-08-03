package gr.priovolos.backend.controller;

import gr.priovolos.backend.core.exceptions.EntityNotFoundException;
import gr.priovolos.backend.dto.DeviceCreationDTO;
import gr.priovolos.backend.dto.DeviceResponseDTO;
import gr.priovolos.backend.dto.PageResponseDTO;
import gr.priovolos.backend.service.IDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller responsible for managing network devices.
 *
 * <p>This controller provides endpoints for registering, retrieving,
 * and soft deleting network devices that are managed by the Network
 * Infrastructure Management Platform.</p>
 *
 * <p>The controller supports multivendor environments and can be used
 * to manage devices from vendors such as Cisco, MikroTik, Palo Alto,
 * Aruba, and other SSH-enabled network devices.</p>
 *
 * <p>Device credentials are securely encrypted before being stored in
 * the database and are only decrypted when an authorized SSH operation
 * is performed.</p>
 *
 * <p>All retrieval operations return only active (non-soft-deleted)
 * devices.</p>
 *
 * @author Ioannis Priovolos
 */
@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Tag(name = "Device Controller", description =
                """
                Provides secure management of network devices,
                including registration, retrieval and soft deletion
                for SSH-enabled devices from multiple vendors.
                """)
public class DeviceController {

    /**
     * Service responsible for managing network devices.
     */
    private final IDeviceService deviceService;

    /**
     * Retrieves a paginated list of active network devices.
     *
     * <p>Only devices that have not been soft deleted are returned.
     * Results are ordered according to the supplied pagination
     * parameters or the configured default values.</p>
     *
     * <p>Default pagination:
     * <ul>
     *     <li>Page: 0</li>
     *     <li>Size: 6</li>
     *     <li>Sort: title (ascending)</li>
     * </ul>
     *
     * @param pageable pagination and sorting information
     * @return a paginated collection of active network devices
     */
    @Operation(
            summary = "Get all active devices",
            description = """
                Retrieves a paginated list of all active (non-deleted) network devices.
                
                Results are returned using Spring Data pagination.
                
                Default pagination:
                - Page: 0
                - Size: 6
                - Sort: title (ascending)
                
                Example:
                GET /api/v1/devices?page=0&size=6&sort=title,asc
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Paginated list of active devices retrieved successfully.",
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
                    description = "User does not have permission to view devices.",
                    content = @Content
            )
    })
    @GetMapping
    public ResponseEntity<PageResponseDTO<DeviceResponseDTO>> getAllActiveDevicesPaginated(
            @PageableDefault(
                    page = 0,
                    size = 6,
                    sort = "title",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(deviceService.getAllActiveDevicesPaginated(pageable));
    }

    /**
     * Registers a new network device.
     *
     * <p>The supplied device information is validated before being
     * persisted. SSH credentials are encrypted prior to storage,
     * ensuring that plaintext passwords are never written to the
     * database.</p>
     *
     * @param request the device creation request
     * @return the newly created network device
     */
    @Operation(
            summary = "Create a new network device",
            description = """
                Creates a new network device and stores it in the system.
                
                The request must include the device information, including
                its network address and SSH credentials. Device credentials
                are securely encrypted before being stored in the database.
                
                Returns the newly created device upon successful creation.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Network device created successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DeviceResponseDTO.class)
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
                    description = "User does not have permission to create devices.",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<DeviceResponseDTO> createDevice(@Valid @RequestBody DeviceCreationDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.createDevice(request));
    }

    /**
     * Soft deletes a network device.
     *
     * <p>The device is not permanently removed from the database.
     * Instead, it is marked as deleted and assigned a deletion
     * timestamp.</p>
     *
     * <p>Soft-deleted devices are automatically excluded from:
     * <ul>
     *     <li>Device listings.</li>
     *     <li>Dashboard statistics.</li>
     *     <li>SSH command execution.</li>
     * </ul>
     *
     * @param id the identifier of the device to be soft deleted
     * @return HTTP 204 (No Content) when the operation completes
     * successfully
     * @throws EntityNotFoundException if the specified device does
     * not exist or has already been deleted
     */
    @Operation(
            summary = "Soft delete a network device",
            description = """
                Soft deletes a network device identified by its ID.
                
                The device is not permanently removed from the database.
                Instead, it is marked as deleted and the deletion timestamp
                is recorded. Soft-deleted devices are excluded from normal
                application operations and dashboard statistics.
                
                Returns no content upon successful completion.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Network device soft deleted successfully."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid device ID supplied.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not have permission to delete devices.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Network device not found.",
                    content = @Content
            )
    })
    @PatchMapping("/{id}")
    public ResponseEntity<Void> softDeleteDevice(@PathVariable Long id) throws EntityNotFoundException {

        deviceService.softDeleteDevice(id);

        return ResponseEntity.noContent().build();
    }
}
