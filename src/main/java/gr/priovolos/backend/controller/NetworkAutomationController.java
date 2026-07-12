package gr.priovolos.backend.controller;

import gr.priovolos.backend.dto.SshCommandRequestDTO;
import gr.priovolos.backend.service.SshAutomationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Network Automation", description = "Secure multi-vendor SSH orchestration engine for Cisco, Palo Alto, Aruba, and MikroTik appliances.")
public class NetworkAutomationController {

    private final SshAutomationService sshAutomationService;

    @PostMapping("/execute")
    @PreAuthorize("hasAnyRole('ADMIN', 'NETWORK_ENGINEER')")
    @Operation(
            summary = "Execute remote SSH command",
            description = "Establishes a secure interactive terminal session with the specified network device to run a command. Requires ADMIN or NETWORK_ENGINEER roles.",
            security = @SecurityRequirement(name = "bearerAuth") // Links this to your JWT configuration setup in Swagger
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Command executed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"output\": \"Cisco IOS Software...\\nDevice# show version\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User lacks sufficient network execution roles",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Device communication or handshake failure",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Secure automation handshake failed over host port interface.\"}")
                    )
            )
    })
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