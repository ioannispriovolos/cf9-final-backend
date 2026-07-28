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

public interface DeviceRepository extends JpaRepository<Device, Long> {

    //Page<Device> findAllByDeletedFalse(Pageable pageable);
    Optional<Device> findByIdAndDeletedFalse(Long id);
    List<Device> findAllByDeletedFalse();
    List<Device> findAllByIdInAndDeletedFalse(
            Collection<Long> ids
    );
    Page<Device> findAllByDeletedFalse(Pageable pageable);

    /*
     * Dashboard summary statistics
     */

    long countByDeletedFalse();

    long countByDeletedFalseAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            Instant start,
            Instant end
    );

    @Query("""
            SELECT COUNT(DISTINCT d.manufacturer)
            FROM Device d
            WHERE d.deleted = false
            """)
    long countDistinctActiveManufacturers();

    @Query("""
            SELECT COUNT(DISTINCT d.model)
            FROM Device d
            WHERE d.deleted = false
            """)
    long countDistinctActiveModels();

    /*
     * Dashboard chart: devices by manufacturer
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

    /*
     * Dashboard chart: top device models
     *
     * Pageable is used to limit the result to the first six models.
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

    /*
     * Dashboard chart: device creation dates.
     *
     * Only the createdAt field is retrieved.
     * Month grouping is performed in Java to avoid native PostgreSQL SQL.
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
