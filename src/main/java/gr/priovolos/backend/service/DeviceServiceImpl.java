package gr.priovolos.backend.service;

import gr.priovolos.backend.core.exceptions.EntityNotFoundException;
import gr.priovolos.backend.dto.DeviceCreationDTO;
import gr.priovolos.backend.dto.DeviceResponseDTO;
import gr.priovolos.backend.mapper.Mapper;
import gr.priovolos.backend.model.Device;
import gr.priovolos.backend.repository.DeviceRepository;
import gr.priovolos.backend.security.DevicePasswordEncryption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceServiceImpl implements IDeviceService{

    private final DeviceRepository deviceRepository;
    private final Mapper mapper;
    private final DevicePasswordEncryption encryption;

    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponseDTO> getAllActiveDevices() {
        return deviceRepository.findAllByDeletedFalse()
                .stream()
                .map(mapper::toDeviceResponseDTO)
                .toList();
    }

    @PreAuthorize("hasAuthority('INSERT_DEVICE')")
    @Transactional
    public DeviceResponseDTO createDevice(DeviceCreationDTO dto) {

        Device device = mapper.toDeviceEntity(dto);

        device.setPassword(
                encryption.encrypt(dto.password())
        );

        return mapper.toDeviceResponseDTO(deviceRepository.save(device));
    }

    @Override
    @PreAuthorize("hasAuthority('DELETE_DEVICE')")
    @Transactional
    public void softDeleteDevice(Long id) throws EntityNotFoundException {

        Device device = deviceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("DEVICE_NOT_FOUND", "Device not found."));

        device.softDelete();

        deviceRepository.save(device);
    }

    private DeviceResponseDTO mapToResponse(Device device) {
        return new DeviceResponseDTO(
                device.getId(),
                device.getTitle(),
                device.getManufacturer(),
                device.getModel(),
                device.getIpAddress(),
                device.getSshPort(),
                device.getUsername()
        );
    }
}
