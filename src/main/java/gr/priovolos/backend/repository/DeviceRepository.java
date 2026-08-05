package gr.priovolos.backend.repository;

import gr.priovolos.backend.dto.dashboard.DashboardCountDTO;
import gr.priovolos.backend.dto.dashboard.DeviceCreationDateDTO;
import gr.priovolos.backend.model.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Device} entities.
 *
 * <p>This repository provides persistence operations for managed
 * network devices. In addition to the standard CRUD functionality
 * inherited from {@link JpaRepository}, it exposes custom query
 * methods used by the application's device management and viewer
 * dashboard modules.</p>
 *
 * <p>The repository follows the application's soft-delete policy by
 * retrieving only active (non-deleted) devices for normal business
 * operations.</p>
 *
 * <p>Dashboard-specific methods provide aggregated statistical data
 * required by the viewer dashboard without exposing entity objects
 * directly.</p>
 *
 * @author Ioannis Priovolos
 */
public interface DeviceRepository extends JpaRepository<Device, Long> {

    /**
     * Retrieves an active network device by its identifier.
     *
     * @param id the device identifier
     * @return an {@link Optional} containing the device if found and
     *         not soft deleted; otherwise an empty {@link Optional}
     */
    Optional<Device> findByIdAndDeletedFalse(Long id);

    /**
     * Retrieves all active (non-deleted) network devices.
     *
     * @return a list containing all active devices
     */
    List<Device> findAllByDeletedFalse();

    /**
     * Retrieves all active devices whose identifiers are contained
     * in the supplied collection.
     *
     * <p>This method is primarily used for batch SSH command
     * execution.</p>
     *
     * @param ids the identifiers of the requested devices
     * @return a list of matching active devices
     */
    List<Device> findAllByIdInAndDeletedFalse(
            Collection<Long> ids
    );

    /**
     * Retrieves a paginated list of active network devices.
     *
     * @param pageable pagination and sorting information
     * @return a page containing active devices
     */
    Page<Device> findAllByDeletedFalse(Pageable pageable);

    /**
     * Returns the total number of active network devices.
     *
     * @return the number of non-deleted devices
     */
    long countByDeletedFalse();

    /**
     * Returns the number of active devices created within
     * the specified time interval.
     *
     * <p>The lower bound is inclusive while the upper bound
     * is exclusive.</p>
     *
     * @param start the beginning of the time interval
     * @param end the end of the time interval
     * @return the number of matching devices
     */
    long countByDeletedFalseAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            Instant start,
            Instant end
    );

    /**
     * Counts the number of distinct manufacturers represented by
     * active network devices.
     *
     * @return the number of distinct manufacturers
     */
    @Query("""
            SELECT COUNT(DISTINCT d.manufacturer)
            FROM Device d
            WHERE d.deleted = false
            """)
    long countDistinctActiveManufacturers();

    /**
     * Counts the number of distinct device models represented by
     * active network devices.
     *
     * @return the number of distinct device models
     */
    @Query("""
            SELECT COUNT(DISTINCT d.model)
            FROM Device d
            WHERE d.deleted = false
            """)
    long countDistinctActiveModels();

    /**
     * Returns the number of active devices grouped by manufacturer.
     *
     * <p>The results are ordered in descending order based on the
     * number of devices for each manufacturer.</p>
     *
     * @return dashboard statistics grouped by manufacturer
     */
    @Query("""
            SELECT new gr.priovolos.backend.dto.dashboard.DashboardCountDTO(
                d.manufacturer,
                COUNT(d)
            )
            FROM Device d
            WHERE d.deleted = false
            GROUP BY d.manufacturer
            ORDER BY COUNT(d) DESC
            """)
    List<DashboardCountDTO> countActiveDevicesByManufacturer();

    /**
     * Returns the number of active devices grouped by model.
     *
     * <p>The supplied {@link Pageable} limits the number of returned
     * results, allowing only the most common models to be retrieved
     * for dashboard visualization.</p>
     *
     * @param pageable pagination information used to limit the result
     * @return dashboard statistics grouped by device model
     */
    @Query("""
            SELECT new gr.priovolos.backend.dto.dashboard.DashboardCountDTO(
                d.model,
                COUNT(d)
            )
            FROM Device d
            WHERE d.deleted = false
            GROUP BY d.model
            ORDER BY COUNT(d) DESC
            """)
    List<DashboardCountDTO> countActiveDevicesByModel(
            Pageable pageable
    );

    /**
     * Retrieves the creation timestamps of active devices created
     * after the specified date.
     *
     * <p>Only the {@code createdAt} field is selected in order to
     * minimize the amount of transferred data. Monthly grouping is
     * intentionally performed within the service layer using Java,
     * avoiding database-specific SQL.</p>
     *
     * @param fromDate the earliest creation timestamp to include
     * @return the creation dates of matching devices
     */
    @Query("""
            SELECT new gr.priovolos.backend.dto.dashboard.DeviceCreationDateDTO(
                d.createdAt
            )
            FROM Device d
            WHERE d.deleted = false
              AND d.createdAt >= :fromDate
            ORDER BY d.createdAt ASC
            """)
    List<DeviceCreationDateDTO> findActiveDeviceCreationDatesSince(
            @Param("fromDate") Instant fromDate
    );
}
