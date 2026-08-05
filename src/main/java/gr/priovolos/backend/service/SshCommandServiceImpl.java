package gr.priovolos.backend.service;

import gr.priovolos.backend.config.SshProperties;
import gr.priovolos.backend.core.exceptions.EntityNotFoundException;
import gr.priovolos.backend.dto.ExecuteSshCommandRequestDTO;
import gr.priovolos.backend.dto.SshBatchExecutionResponseDTO;
import gr.priovolos.backend.dto.SshCommandResultDTO;
import gr.priovolos.backend.model.Device;
import gr.priovolos.backend.repository.DeviceRepository;
import gr.priovolos.backend.ssh.SshCommandExecutor;
import gr.priovolos.backend.ssh.SshCommandValidator;
import gr.priovolos.backend.ssh.SshTarget;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service implementation responsible for executing SSH commands on
 * one or more managed network devices.
 *
 * <p>This service coordinates the complete batch execution workflow,
 * including request validation, device retrieval, concurrent command
 * execution, result aggregation, and response generation.</p>
 *
 * <p>Each requested device is executed independently using a dedicated
 * worker thread from the configured SSH executor service. This allows
 * multiple devices to be processed concurrently while preserving the
 * order of the devices supplied by the client.</p>
 *
 * <p>The service itself does not establish SSH connections directly.
 * Instead, it delegates command execution to {@link SshCommandExecutor},
 * allowing the orchestration logic to remain separate from the SSH
 * communication implementation.</p>
 *
 * <p>Only active (non-deleted) devices may participate in a batch
 * execution request.</p>
 *
 * @author Ioannis Priovolos
 */
@Service
public class SshCommandServiceImpl implements ISshCommandService {

    private final DeviceRepository deviceRepository;

    private final SshCommandExecutor commandExecutor;

    private final SshCommandValidator commandValidator;

    private final SshProperties properties;

    /**
     * Creates a new SSH command service.
     *
     * @param deviceRepository repository used to retrieve target devices
     * @param commandExecutor component responsible for executing SSH commands
     * @param commandValidator validator for user supplied commands
     * @param properties SSH configuration properties
     * @param sshExecutorService executor responsible for concurrent
     *                           command execution
     */
    public SshCommandServiceImpl(
            DeviceRepository deviceRepository,
            SshCommandExecutor commandExecutor,
            SshCommandValidator commandValidator,
            SshProperties properties,
            @Qualifier("sshExecutorService")
            ExecutorService sshExecutorService
    ) {
        this.deviceRepository = deviceRepository;
        this.commandExecutor = commandExecutor;
        this.commandValidator = commandValidator;
        this.properties = properties;
        this.sshExecutorService = sshExecutorService;
    }

    @Qualifier("sshExecutorService")
    private final ExecutorService sshExecutorService;

    /**
     * Executes an SSH command on the selected network devices.
     *
     * <p>The execution workflow consists of:</p>
     * <ol>
     *     <li>Validating the requested device identifiers.</li>
     *     <li>Validating and normalizing the SSH command.</li>
     *     <li>Retrieving all active devices.</li>
     *     <li>Verifying that every requested device exists.</li>
     *     <li>Creating immutable SSH execution targets.</li>
     *     <li>Executing commands concurrently.</li>
     *     <li>Collecting and aggregating execution results.</li>
     *     <li>Returning a batch execution summary.</li>
     * </ol>
     *
     * <p>The order of the returned results matches the order of
     * the device identifiers supplied by the client.</p>
     *
     * <p>Access requires the {@code INSERT_DEVICE} capability.</p>
     *
     * @param request the SSH execution request
     * @return the aggregated batch execution result
     * @throws EntityNotFoundException if one or more requested
     *                                 devices do not exist
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('INSERT_DEVICE')")
    public SshBatchExecutionResponseDTO executeOnSelectedDevices(
            ExecuteSshCommandRequestDTO request
    ) throws EntityNotFoundException {
        long batchStartedAt = System.nanoTime();

        Set<Long> requestedIds =
                new LinkedHashSet<>(request.deviceIds());

        validateRequestedDeviceIds(requestedIds);

        String command =
                commandValidator.validateAndNormalize(
                        request.command()
                );

        List<Device> devices =
                deviceRepository.findAllByIdInAndDeletedFalse(
                        requestedIds
                );

        ensureAllDevicesWereFound(
                requestedIds,
                devices
        );

        /*
         * Preserve the order in which the frontend supplied device IDs.
         */
        Map<Long, Device> devicesById =
                devices.stream()
                        .collect(Collectors.toMap(
                                Device::getId,
                                Function.identity()
                        ));

        List<SshTarget> targets =
                requestedIds.stream()
                        .map(devicesById::get)
                        .map(SshTarget::from)
                        .toList();

        /*
         * The database transaction is not used by the worker threads.
         * They receive only immutable SSH target records.
         */
        List<CompletableFuture<SshCommandResultDTO>> futures =
                targets.stream()
                        .map(target ->
                                CompletableFuture.supplyAsync(
                                        () -> commandExecutor.execute(
                                                target,
                                                command
                                        ),
                                        sshExecutorService
                                )
                        )
                        .toList();

        List<SshCommandResultDTO> results =
                futures.stream()
                        .map(this::joinSafely)
                        .toList();

        int successful =
                (int) results.stream()
                        .filter(SshCommandResultDTO::successful)
                        .count();

        return new SshBatchExecutionResponseDTO(
                results.size(),
                successful,
                results.size() - successful,
                elapsedMilliseconds(batchStartedAt),
                results
        );
    }

    /**
     * Retrieves the result of an asynchronous SSH execution.
     *
     * <p>If an unexpected exception occurs while processing a worker
     * thread, a synthetic failure result is returned instead of
     * allowing the entire batch execution to fail.</p>
     *
     * @param future the asynchronous SSH execution
     * @return the execution result
     */
    private SshCommandResultDTO joinSafely(
            CompletableFuture<SshCommandResultDTO> future
    ) {
        try {
            return future.join();
        } catch (CompletionException exception) {
            /*
             * Normally the executor converts connection errors to result
             * objects. This protects the complete batch from an unexpected
             * worker failure.
             */
            return SshCommandResultDTO.executionFailure(
                    null,
                    "Unknown device",
                    null,
                    "An unexpected SSH worker error occurred.",
                    0
            );
        }
    }

    /**
     * Validates the requested device identifiers.
     *
     * <p>The validation ensures that:</p>
     * <ul>
     *     <li>At least one device has been selected.</li>
     *     <li>The maximum configured batch size is not exceeded.</li>
     *     <li>Every identifier is positive.</li>
     * </ul>
     *
     * @param requestedIds the requested device identifiers
     * @throws IllegalArgumentException if validation fails
     */
    private void validateRequestedDeviceIds(
            Set<Long> requestedIds
    ) {
        if (requestedIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one device must be selected."
            );
        }

        if (requestedIds.size()
                > properties.maximumDevicesPerRequest()) {
            throw new IllegalArgumentException(
                    "Too many devices were selected."
            );
        }

        boolean invalidId =
                requestedIds.stream()
                        .anyMatch(id ->
                                id == null || id <= 0
                        );

        if (invalidId) {
            throw new IllegalArgumentException(
                    "All device IDs must be positive."
            );
        }
    }

    /**
     * Verifies that every requested device exists and has not been
     * softly deleted.
     *
     * @param requestedIds the identifiers supplied by the client
     * @param devices the devices retrieved from the repository
     * @throws EntityNotFoundException if one or more requested
     *                                 devices cannot be found
     */
    private void ensureAllDevicesWereFound(
            Set<Long> requestedIds,
            List<Device> devices
    ) throws EntityNotFoundException {
        Set<Long> foundIds =
                devices.stream()
                        .map(Device::getId)
                        .collect(Collectors.toSet());

        Set<Long> missingIds =
                new LinkedHashSet<>(requestedIds);

        missingIds.removeAll(foundIds);

        if (!missingIds.isEmpty()) {
            throw new EntityNotFoundException(
                    "DEVICE_NOT_FOUND",
                    "One or more selected devices do not exist "
                            + "or have been deleted."
            );
        }
    }

    /**
     * Calculates the elapsed execution time.
     *
     * @param startedAt the start time expressed using
     *                  {@link System#nanoTime()}
     * @return the elapsed execution time in milliseconds
     */
    private long elapsedMilliseconds(long startedAt) {
        return Duration.ofNanos(
                System.nanoTime() - startedAt
        ).toMillis();
    }
}
