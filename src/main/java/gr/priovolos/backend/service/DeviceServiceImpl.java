package gr.priovolos.backend.service;

import gr.priovolos.backend.core.exceptions.EntityInvalidArgumentException;
import gr.priovolos.backend.core.exceptions.EntityNotFoundException;
import gr.priovolos.backend.dto.DeviceCreationDTO;
import gr.priovolos.backend.dto.DeviceResponseDTO;
import gr.priovolos.backend.dto.DeviceUpdateDTO;
import gr.priovolos.backend.dto.PageResponseDTO;
import gr.priovolos.backend.mapper.Mapper;
import gr.priovolos.backend.model.Device;
import gr.priovolos.backend.repository.DeviceRepository;
import gr.priovolos.backend.security.DevicePasswordEncryption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Service implementation responsible for managing network devices.
 *
 * <p>This service encapsulates the business logic for creating,
 * retrieving, paginating, and soft deleting managed network devices.
 * It coordinates validation, persistence operations, object mapping,
 * and secure handling of device SSH credentials.</p>
 *
 * <p>Before a device is persisted, its SSH password is encrypted
 * using the application's encryption service. Passwords are never
 * stored in plaintext.</p>
 *
 * <p>All retrieval operations return only active (non-deleted)
 * devices, ensuring that logically deleted devices are excluded
 * from normal application use.</p>
 *
 * @author Ioannis Priovolos
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceServiceImpl implements IDeviceService{

    private final DeviceRepository deviceRepository;
    private final Mapper mapper;
    private final DevicePasswordEncryption encryption;

    /**
     * Retrieves all active (non-deleted) network devices.
     *
     * <p>The returned devices are converted into read-only DTOs,
     * ensuring that sensitive information such as encrypted SSH
     * passwords is never exposed to clients.</p>
     *
     * @return a list of active network devices
     */
    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponseDTO> getAllActiveDevices() {
        return deviceRepository.findAllByDeletedFalse()
                .stream()
                .map(mapper::toDeviceResponseDTO)
                .toList();
    }

    /**
     * Retrieves a paginated list of active network devices.
     *
     * <p>The requested pagination and sorting information is applied
     * through Spring Data JPA. The returned page includes both device
     * information and pagination metadata.</p>
     *
     * <p>Access requires the {@code INSERT_DEVICE} capability.</p>
     *
     * @param pageable pagination and sorting information
     * @return a paginated response containing active devices
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('INSERT_DEVICE')")
    public PageResponseDTO<DeviceResponseDTO> getAllActiveDevicesPaginated(Pageable pageable) {
        Page<Device> devicePage =
                deviceRepository.findAllByDeletedFalse(pageable);

        return PageResponseDTO.from(devicePage, mapper::toDeviceResponseDTO);
    }

    /**
     * Creates a new managed network device.
     *
     * <p>The supplied device information is converted into a domain
     * entity and the SSH password is encrypted before persistence.
     * The saved entity is then returned as a read-only DTO.</p>
     *
     * <p>Access requires the {@code INSERT_DEVICE} capability.</p>
     *
     * @param dto the device creation request
     * @return the newly created network device
     */
    @PreAuthorize("hasAuthority('INSERT_DEVICE')")
    @Transactional
    public DeviceResponseDTO createDevice(DeviceCreationDTO dto) {

        Device device = mapper.toDeviceEntity(dto);
        device.setPassword(encryption.encrypt(dto.password()));

        return mapper.toDeviceResponseDTO(deviceRepository.save(device));
    }

    /**
     * Performs a logical (soft) deletion of a network device.
     *
     * <p>The device is not permanently removed from the database.
     * Instead, it is marked as deleted and excluded from normal
     * application operations and dashboard statistics.</p>
     *
     * <p>Access requires the {@code DELETE_DEVICE} capability.</p>
     *
     * @param id the identifier of the device to delete
     * @throws EntityNotFoundException if no active device exists
     *                                 with the specified identifier
     */
    @Override
    @PreAuthorize("hasAuthority('DELETE_DEVICE')")
    @Transactional
    public void softDeleteDevice(Long id) throws EntityNotFoundException {

        Device device = deviceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("DEVICE_NOT_FOUND", "Device not found."));

        device.softDelete();

        deviceRepository.save(device);
    }

    /**
     * Converts a {@link Device} entity into a
     * {@link DeviceResponseDTO}.
     *
     * <p>This helper method returns only non-sensitive device
     * information and intentionally excludes the encrypted SSH
     * password.</p>
     *
     * <p><strong>Note:</strong> This method is currently unused
     * because mapping is delegated to the application's
     * {@link Mapper} component.</p>
     *
     * @param device the device entity
     * @return the corresponding read-only device DTO
     */
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

    /**
     * Updates one or more editable properties of an active network device.
     *
     * <p>The method supports partial updates. Only values supplied in the
     * {@link DeviceUpdateDTO} are applied, while omitted values remain
     * unchanged.</p>
     *
     * <p>The device password is not modified by this operation. The update
     * is limited to the device title, manufacturer, model, IP address,
     * SSH port, and username.</p>
     *
     * <p>The operation requires the {@code EDIT_DEVICE} capability and
     * can only be performed on active, non-soft-deleted devices.</p>
     *
     * <p>The {@code updatedAt} timestamp is refreshed for every successful
     * update request, even when the supplied values are identical to the
     * currently stored values.</p>
     *
     * @param id the identifier of the active device to update
     * @param request the optional device properties to update
     * @return the updated device as a read-only response DTO
     * @throws EntityNotFoundException if no active device exists with
     *                                 the supplied identifier
     * @throws EntityInvalidArgumentException if the request does not
     *                                        contain any properties to update
     */
    @Override
    @PreAuthorize("hasAuthority('EDIT_DEVICE')")
    @Transactional
    public DeviceResponseDTO updateDeviceById(
            Long id,
            DeviceUpdateDTO request
    ) throws EntityNotFoundException, EntityInvalidArgumentException {

        if (!request.hasChanges()) {
            throw new EntityInvalidArgumentException(
                    "Device",
                    "At least one device property must be provided."
            );
        }

        Device device = deviceRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Device",
                                "Active device with id="
                                        + id
                                        + " was not found."
                        )
                );

        if (request.title() != null) {
            device.setTitle(
                    request.title().trim()
            );
        }

        if (request.manufacturer() != null) {
            device.setManufacturer(
                    request.manufacturer().trim()
            );
        }

        if (request.model() != null) {
            device.setModel(
                    request.model().trim()
            );
        }

        if (request.ipAddress() != null) {
            device.setIpAddress(
                    request.ipAddress().trim()
            );
        }

        if (request.sshPort() != null) {
            device.setSshPort(
                    request.sshPort()
            );
        }

        if (request.username() != null) {
            device.setUsername(
                    request.username().trim()
            );
        }

        /*
         * Always update the modification timestamp for every
         * successful update request, even when the supplied values
         * are identical to the currently stored values.
         */
        device.setUpdatedAt(Instant.now());

        Device updatedDevice =
                deviceRepository.save(device);

        return mapper.toDeviceResponseDTO(
                updatedDevice
        );
    }
}
