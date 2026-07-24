package gr.priovolos.backend.service;

import gr.priovolos.backend.core.exceptions.EntityNotFoundException;
import gr.priovolos.backend.dto.DeviceCreationDTO;
import gr.priovolos.backend.dto.DeviceResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IDeviceService {

    List<DeviceResponseDTO> getAllActiveDevices();
    DeviceResponseDTO createDevice(DeviceCreationDTO request);

    @Transactional
    void softDeleteDevice(Long id) throws EntityNotFoundException;
}
