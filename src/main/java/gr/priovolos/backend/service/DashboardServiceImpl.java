package gr.priovolos.backend.service;

import gr.priovolos.backend.dto.dashboard.*;
import gr.priovolos.backend.mapper.Mapper;
import gr.priovolos.backend.model.Device;
import gr.priovolos.backend.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service implementation responsible for generating the viewer dashboard.
 *
 * <p>This service collects, aggregates, and transforms information
 * related to managed network devices into a dashboard suitable for
 * read-only users. All statistics are calculated using active
 * (non-deleted) devices only.</p>
 *
 * <p>The dashboard includes:</p>
 * <ul>
 *     <li>Summary statistics.</li>
 *     <li>Device distribution by manufacturer.</li>
 *     <li>Most common device models.</li>
 *     <li>Monthly device creation statistics.</li>
 *     <li>Most recently created network devices.</li>
 * </ul>
 *
 * <p>The service delegates data retrieval to the repository layer,
 * performs aggregation and time-based calculations where appropriate,
 * and returns immutable DTOs suitable for presentation by the frontend.</p>
 *
 * @author Ioannis Priovolos
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private static final ZoneId DASHBOARD_ZONE =
            ZoneId.of("Europe/Athens");

    private static final int MONTH_HISTORY = 12;

    private static final int TOP_MODEL_LIMIT = 6;

    private static final int RECENT_DEVICE_LIMIT = 6;

    private final DeviceRepository deviceRepository;
    private final Mapper mapper;

    /**
     * Generates the complete viewer dashboard.
     *
     * <p>The dashboard consists exclusively of information derived
     * from active (non-deleted) devices and includes:</p>
     * <ul>
     *     <li>Total active devices.</li>
     *     <li>Total manufacturers.</li>
     *     <li>Total device models.</li>
     *     <li>Devices added during the current month.</li>
     *     <li>Device distribution by manufacturer.</li>
     *     <li>Most common device models.</li>
     *     <li>Monthly device creation history.</li>
     *     <li>Recently created devices.</li>
     * </ul>
     *
     * <p>The method is read-only and requires the
     * {@code VIEW_ONLY_USER} capability.</p>
     *
     * @return the complete viewer dashboard
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('VIEW_ONLY_USER')")
    public ViewerDashboardResponseDTO getViewerDashboard() {

        /*
         * Summary-card values
         */

        long activeDevices = deviceRepository.countByDeletedFalse();

        long totalManufacturers = deviceRepository.countDistinctActiveManufacturers();

        long totalModels = deviceRepository.countDistinctActiveModels();

        Instant startOfCurrentMonth = getStartOfCurrentMonth();

        Instant startOfNextMonth = getStartOfNextMonth();

        long devicesAddedThisMonth =
                deviceRepository
                        .countByDeletedFalseAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(startOfCurrentMonth, startOfNextMonth);

        /*
         * Manufacturer distribution
         */

        List<DashboardCountDTO> devicesByManufacturer = deviceRepository.countActiveDevicesByManufacturer();

        /*
         * Six most common models
         */

        PageRequest topModelsPageRequest =
                PageRequest.of(0, TOP_MODEL_LIMIT);

        List<DashboardCountDTO> devicesByModel =
                deviceRepository.countActiveDevicesByModel(topModelsPageRequest);

        /*
         * Devices added during the last 12 calendar months
         */

        Instant monthlyHistoryStart = getMonthlyHistoryStart();

        List<DeviceCreationDateDTO> creationDates = deviceRepository.findActiveDeviceCreationDatesSince(monthlyHistoryStart);

        List<MonthlyDeviceCountDTO> devicesAddedByMonth = createCompleteMonthlySeries(creationDates);

        /*
         * Six most recently created active devices
         */

        List<RecentDeviceDTO> recentlyUpdatedDevices =
                getRecentlyCreatedDevices();

        log.debug(
                "Viewer dashboard generated: activeDevices={}, manufacturers={}, models={}",
                activeDevices,
                totalManufacturers,
                totalModels
        );

        return new ViewerDashboardResponseDTO(
                activeDevices,
                totalManufacturers,
                totalModels,
                devicesAddedThisMonth,
                devicesByManufacturer,
                devicesByModel,
                devicesAddedByMonth,
                recentlyUpdatedDevices
        );
    }

    /**
     * Retrieves the most recently created active network devices.
     *
     * <p>The returned devices are ordered by creation timestamp
     * (descending). If two devices share the same creation time,
     * they are secondarily ordered alphabetically by title.</p>
     *
     * @return a list containing the most recently created devices
     */
    private List<RecentDeviceDTO> getRecentlyCreatedDevices() {

        PageRequest pageRequest =
                PageRequest.of(
                        0,
                        RECENT_DEVICE_LIMIT,
                        Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("title")));

        Page<Device> devicePage = deviceRepository.findAllByDeletedFalse(pageRequest);

        return devicePage.getContent().stream().map(mapper::toRecentDeviceDTO).toList();
    }

    /**
     * Creates a complete twelve-month time series for dashboard
     * visualization.
     *
     * <p>The supplied creation timestamps are grouped by calendar
     * month. Months without any device insertions are included with
     * a count of zero, ensuring that the frontend always receives
     * a continuous twelve-month dataset.</p>
     *
     * @param creationDates the creation timestamps retrieved from
     *                      the repository
     * @return a complete monthly device count series
     */
    private List<MonthlyDeviceCountDTO> createCompleteMonthlySeries(
            List<DeviceCreationDateDTO> creationDates
    ) {

        Map<YearMonth, Long> countsByMonth =
                creationDates.stream()
                        .map(DeviceCreationDateDTO::createdAt)
                        .map(createdAt ->
                                YearMonth.from(createdAt.atZone(DASHBOARD_ZONE)))
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        YearMonth firstMonth = YearMonth.now(DASHBOARD_ZONE).minusMonths(MONTH_HISTORY - 1L);

        List<MonthlyDeviceCountDTO> monthlySeries = new ArrayList<>(MONTH_HISTORY);

        for (int index = 0; index < MONTH_HISTORY; index++) {

            YearMonth month = firstMonth.plusMonths(index);

            long count = countsByMonth.getOrDefault(month, 0L);

            monthlySeries.add(new MonthlyDeviceCountDTO(month.toString(), count));
        }

        return List.copyOf(monthlySeries);
    }

    /**
     * Calculates the beginning of the current calendar month.
     *
     * @return the first instant of the current month in the
     * configured dashboard time zone
     */
    private Instant getStartOfCurrentMonth() {

        YearMonth currentMonth = YearMonth.now(DASHBOARD_ZONE);

        return currentMonth.atDay(1).atStartOfDay(DASHBOARD_ZONE).toInstant();
    }

    /**
     * Calculates the beginning of the next calendar month.
     *
     * <p>This instant is used as the exclusive upper bound when
     * counting devices created during the current month.</p>
     *
     * @return the first instant of the next month
     */
    private Instant getStartOfNextMonth() {

        YearMonth nextMonth = YearMonth.now(DASHBOARD_ZONE).plusMonths(1);

        return nextMonth.atDay(1).atStartOfDay(DASHBOARD_ZONE).toInstant();
    }

    /**
     * Calculates the beginning of the dashboard's twelve-month
     * historical period.
     *
     * @return the first instant of the earliest month included
     * in the dashboard history
     */
    private Instant getMonthlyHistoryStart() {

        YearMonth firstMonth = YearMonth.now(DASHBOARD_ZONE).minusMonths(MONTH_HISTORY - 1L);

        return firstMonth
                .atDay(1)
                .atStartOfDay(DASHBOARD_ZONE)
                .toInstant();
    }
}
