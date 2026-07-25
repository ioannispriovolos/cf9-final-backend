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

@Service
public class SshCommandServiceImpl implements ISshCommandService {

    private final DeviceRepository deviceRepository;

    private final SshCommandExecutor commandExecutor;

    private final SshCommandValidator commandValidator;

    private final SshProperties properties;

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

    private long elapsedMilliseconds(long startedAt) {
        return Duration.ofNanos(
                System.nanoTime() - startedAt
        ).toMillis();
    }
}
