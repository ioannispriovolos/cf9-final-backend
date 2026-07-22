package gr.priovolos.backend.service;

import gr.priovolos.backend.dto.DeviceCreationDTO;
import gr.priovolos.backend.dto.DeviceResponseDTO;

import java.util.List;

public interface IDeviceService {

    List<DeviceResponseDTO> getAllActiveDevices();
    DeviceResponseDTO createDevice(DeviceCreationDTO request);
}
