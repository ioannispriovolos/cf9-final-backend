package gr.priovolos.backend.service;

import gr.priovolos.backend.core.exceptions.EntityNotFoundException;
import gr.priovolos.backend.dto.DeviceCreationDTO;
import gr.priovolos.backend.dto.DeviceResponseDTO;
import gr.priovolos.backend.dto.PageResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IDeviceService {

    List<DeviceResponseDTO> getAllActiveDevices();
    DeviceResponseDTO createDevice(DeviceCreationDTO request);

    @Transactional
    void softDeleteDevice(Long id) throws EntityNotFoundException;

    PageResponseDTO<DeviceResponseDTO> getAllActiveDevicesPaginated(
            Pageable pageable
    );
}
