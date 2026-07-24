package gr.priovolos.backend.controller;

import gr.priovolos.backend.core.exceptions.EntityNotFoundException;
import gr.priovolos.backend.dto.DeviceCreationDTO;
import gr.priovolos.backend.dto.DeviceResponseDTO;
import gr.priovolos.backend.service.IDeviceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Tag(name = "Network Automation", description = "Secure multi-vendor SSH orchestration engine for Cisco, Palo Alto, Aruba, MikroTik, etc.")
public class DeviceController {

    private final IDeviceService deviceService;

    @GetMapping
    public ResponseEntity<List<DeviceResponseDTO>> getAllDevices() {
        return ResponseEntity.ok(deviceService.getAllActiveDevices());
    }

    @PostMapping
    public ResponseEntity<DeviceResponseDTO> createDevice(@Valid @RequestBody DeviceCreationDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.createDevice(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> softDeleteDevice(@PathVariable Long id) throws EntityNotFoundException {

        deviceService.softDeleteDevice(id);

        return ResponseEntity.noContent().build();
    }
}
