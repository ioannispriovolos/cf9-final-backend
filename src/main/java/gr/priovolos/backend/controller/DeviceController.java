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

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Tag(name = "Device Controller", description = "Secure multi-vendor device insertion, retrieval, soft-deletion for Cisco, Palo Alto, Aruba, MikroTik, etc.")
public class DeviceController {

    private final IDeviceService deviceService;

//    @GetMapping
//    public ResponseEntity<List<DeviceResponseDTO>> getAllDevices() {
//        return ResponseEntity.ok(deviceService.getAllActiveDevices());
//    }

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
