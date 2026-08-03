package gr.priovolos.backend.controller;

import gr.priovolos.backend.dto.dashboard.ViewerDashboardResponseDTO;
import gr.priovolos.backend.service.IDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller responsible for providing dashboard statistics
 * and metrics related to the managed network infrastructure.
 *
 * <p>This controller exposes read-only endpoints that return
 * aggregated information about active network devices. The
 * dashboard is intended for authorized users who need an overview
 * of the current infrastructure without modifying any data.</p>
 *
 * <p>All statistics exclude soft-deleted devices.</p>
 *
 * @author Ioannis Priovolos
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard Controller", description = "Statistics and metrics of the stored devices for authorized view users")
public class DashboardController {

    /**
     * Service responsible for generating dashboard statistics.
     */
    private final IDashboardService dashboardService;

    /**
     * Retrieves the viewer dashboard.
     *
     * <p>The returned dashboard contains aggregated information
     * about the managed network devices, including:
     * <ul>
     *     <li>Total active devices.</li>
     *     <li>Total manufacturers.</li>
     *     <li>Total device models.</li>
     *     <li>Devices added during the current month.</li>
     *     <li>Device distribution by manufacturer.</li>
     *     <li>Device distribution by model.</li>
     *     <li>Monthly device additions.</li>
     *     <li>Recently added network devices.</li>
     * </ul>
     *
     * <p>Only active (non-soft-deleted) devices are included in
     * the calculated statistics.</p>
     *
     * @return a {@link ViewerDashboardResponseDTO} containing
     *         the dashboard statistics
     */
    @Operation(
            summary = "Get viewer dashboard",
            description = """
                Retrieves the dashboard information for users with viewer access.
                
                The dashboard provides an overview of the current network
                infrastructure, including device statistics, manufacturer and
                model distributions, monthly device additions, and the most
                recently added network devices.
                
                Only active (non-deleted) devices are included in all statistics.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Viewer dashboard retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ViewerDashboardResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not have permission to access the viewer dashboard.",
                    content = @Content
            )
    })
    @GetMapping("/viewer")
    public ResponseEntity<ViewerDashboardResponseDTO> getViewerDashboard() {

        ViewerDashboardResponseDTO response =
                dashboardService.getViewerDashboard();

        return ResponseEntity.ok(response);
    }
}
