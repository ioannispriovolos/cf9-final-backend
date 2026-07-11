package gr.priovolos.backend.controller;

import gr.priovolos.backend.dto.SshCommandRequestDTO;
import gr.priovolos.backend.service.SshAutomationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/automation")
@RequiredArgsConstructor
@Slf4j
public class NetworkAutomationController {

    private final SshAutomationService sshAutomationService;

    @PostMapping("/execute")
    @PreAuthorize("hasAnyRole('ADMIN', 'NETWORK_ENGINEER')") // Enforce RBAC (blocks Viewers)
    public ResponseEntity<Map<String, String>> runCommand(@Valid @RequestBody SshCommandRequestDTO dto) {
        try {
            String result = sshAutomationService.executeUniversalCommand(
                    dto.host(),
                    dto.port(),
                    dto.username(),
                    dto.password(),
                    dto.command()
            );
            return ResponseEntity.ok(Map.of("output", result));
        } catch (IOException e) {
            log.error("Secure tracking failure targeting host node {}: ", dto.host(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Secure automation handshake failed over host port interface."
            ));
        }
    }
}