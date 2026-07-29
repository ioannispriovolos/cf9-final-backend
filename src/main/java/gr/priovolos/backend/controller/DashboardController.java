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

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard Controller", description = "Statistics and metrics of the stored devices for authorized view users")
public class DashboardController {

    private final IDashboardService dashboardService;

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
