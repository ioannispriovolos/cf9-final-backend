package gr.priovolos.backend.controller;

import gr.priovolos.backend.dto.dashboard.ViewerDashboardResponseDTO;
import gr.priovolos.backend.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IDashboardService dashboardService;

    @GetMapping("/viewer")
    public ResponseEntity<ViewerDashboardResponseDTO> getViewerDashboard() {

        ViewerDashboardResponseDTO response =
                dashboardService.getViewerDashboard();

        return ResponseEntity.ok(response);
    }
}
