package gr.priovolos.backend.service;

import gr.priovolos.backend.core.exceptions.EntityNotFoundException;
import gr.priovolos.backend.dto.DeviceCreationDTO;
import gr.priovolos.backend.dto.DeviceResponseDTO;
import gr.priovolos.backend.dto.PageResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service interface defining the business operations related to
 * managed network devices.
 *
 * <p>This service provides functionality for creating, retrieving,
 * paginating, and soft deleting network devices. It operates only
 * on active (non-deleted) devices unless explicitly stated
 * otherwise.</p>
 *
 * <p>Implementations of this interface encapsulate the application's
 * business rules, coordinate persistence operations, and perform
 * additional processing such as device password encryption before
 * storing SSH credentials.</p>
 *
 * @author Ioannis Priovolos
 */
public interface IDeviceService {

    /**
     * Retrieves all active (non-deleted) network devices.
     *
     * <p>The returned DTOs contain only non-sensitive device
     * information suitable for client applications. SSH passwords
     * are never included in the response.</p>
     *
     * @return a list of active network devices
     */
    List<DeviceResponseDTO> getAllActiveDevices();

    /**
     * Creates a new network device.
     *
     * <p>The supplied device information is validated before
     * processing. The device's SSH password is securely encrypted
     * before being persisted to the database.</p>
     *
     * @param request the device creation request
     * @return the newly created network device
     */
    DeviceResponseDTO createDevice(DeviceCreationDTO request);

    /**
     * Performs a logical (soft) deletion of a network device.
     *
     * <p>The device is not permanently removed from the database.
     * Instead, it is marked as deleted and excluded from normal
     * application operations.</p>
     *
     * @param id the identifier of the device to delete
     * @throws EntityNotFoundException if no active device with the
     *                                 specified identifier exists
     */
    @Transactional
    void softDeleteDevice(Long id) throws EntityNotFoundException;

    /**
     * Retrieves a paginated list of active (non-deleted)
     * network devices.
     *
     * <p>The returned page contains device information together
     * with pagination metadata such as the current page, total
     * number of pages, and total number of elements.</p>
     *
     * @param pageable pagination and sorting information
     * @return a paginated response containing active devices
     */
    PageResponseDTO<DeviceResponseDTO> getAllActiveDevicesPaginated(
            Pageable pageable
    );
}
