package gr.priovolos.backend.ssh;

import gr.priovolos.backend.config.SshProperties;
import gr.priovolos.backend.core.exceptions.SshCommandTimeoutException;
import gr.priovolos.backend.dto.SshCommandResultDTO;
import gr.priovolos.backend.security.DevicePasswordEncryption;
import lombok.RequiredArgsConstructor;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.SshException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.EnumSet;
import java.util.Set;

/**
 * Component responsible for executing a validated SSH command on a
 * single managed network device.
 *
 * <p>This class encapsulates the low-level SSH workflow for one target,
 * including:</p>
 * <ul>
 *     <li>Validating the target device information.</li>
 *     <li>Decrypting the stored SSH password.</li>
 *     <li>Establishing the SSH connection.</li>
 *     <li>Authenticating the device session.</li>
 *     <li>Executing the command through an SSH exec channel.</li>
 *     <li>Capturing bounded standard and error output.</li>
 *     <li>Converting execution failures into safe response messages.</li>
 * </ul>
 *
 * <p>The executor never returns or logs device passwords. Detailed
 * exceptions are written only to debug logs, while API responses
 * receive generalized error messages that avoid exposing sensitive
 * infrastructure information.</p>
 *
 * <p>This component processes one device at a time. Batch coordination
 * and concurrent execution are handled by the SSH command service.</p>
 *
 * @author Ioannis Priovolos
 */
@Component
@RequiredArgsConstructor
public class SshCommandExecutor {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(SshCommandExecutor.class);

    private final SshClient sshClient;

    private final SshProperties properties;

    private final DevicePasswordEncryption passwordEncryption;

    /**
     * Executes an SSH command on a single target device.
     *
     * <p>The target is validated before the encrypted password is
     * decrypted. The plaintext password remains within the smallest
     * practical scope and is never logged or returned to the client.</p>
     *
     * <p>All expected and unexpected execution failures are converted
     * into a failed {@link SshCommandResultDTO}. This prevents a single
     * device failure from terminating an entire batch operation.</p>
     *
     * @param target the immutable SSH target containing the device
     *               connection information
     * @param command the validated command to execute
     * @return the successful or failed result of the SSH operation
     */
    public SshCommandResultDTO execute(
            SshTarget target,
            String command
    ) {
        long startedAt = System.nanoTime();

        try {
            validateTarget(target);

            /*
             * Keep the plaintext password in the smallest practical scope.
             * Never log it and never return it to the frontend.
             */
            String decryptedPassword =
                    passwordEncryption.decrypt(
                            target.encryptedPassword()
                    );

            try {
                return connectAuthenticateAndExecute(
                        target,
                        command,
                        decryptedPassword,
                        startedAt
                );
            } finally {
                /*
                 * This removes the reference, although Java Strings cannot
                 * be reliably erased from JVM memory.
                 */
                decryptedPassword = null;
            }

        } catch (Exception exception) {

            LOGGER.warn(
                    "SSH operation failed: deviceId={}, host={}, type={}",
                    target == null ? null : target.deviceId(),
                    target == null ? null : target.ipAddress(),
                    exception.getClass().getSimpleName()
            );

            LOGGER.debug(
                    "Detailed SSH failure for deviceId={}",
                    target == null ? null : target.deviceId(),
                    exception
            );

            return SshCommandResultDTO.executionFailure(
                    target == null ? null : target.deviceId(),
                    target == null ? null : target.title(),
                    target == null ? null : target.ipAddress(),
                    safeErrorMessage(exception),
                    elapsedMilliseconds(startedAt)
            );
        }
    }

    /**
     * Establishes an SSH connection, authenticates the session, and
     * delegates command execution.
     *
     * <p>The connection and authentication phases use their respective
     * configurable timeouts. The session is always closed in the
     * {@code finally} block, regardless of success or failure.</p>
     *
     * @param target the target device
     * @param command the command to execute
     * @param decryptedPassword the plaintext SSH password
     * @param startedAt the batch-relative execution start time obtained
     *                  from {@link System#nanoTime()}
     * @return the command execution result
     * @throws Exception if connection, authentication, or execution fails
     */
    private SshCommandResultDTO connectAuthenticateAndExecute(
            SshTarget target,
            String command,
            String decryptedPassword,
            long startedAt
    ) throws Exception {

        ClientSession session = null;

        try {
            session = sshClient
                    .connect(
                            target.username(),
                            target.ipAddress(),
                            target.sshPort()
                    )
                    .verify(properties.connectionTimeout())
                    .getSession();

            session.addPasswordIdentity(decryptedPassword);

            session.auth()
                    .verify(properties.authenticationTimeout());

            return executeCommand(
                    session,
                    target,
                    command,
                    startedAt
            );

        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Executes the command through an SSH exec channel.
     *
     * <p>An exec channel is used instead of an interactive shell,
     * and no pseudo-terminal is requested. Standard output and
     * standard error are captured separately using bounded output
     * streams to prevent excessive memory consumption.</p>
     *
     * <p>If execution exceeds the configured command timeout, the
     * channel is closed and an {@link SshCommandTimeoutException}
     * is thrown.</p>
     *
     * <p>A missing exit status is treated as success because some
     * network appliances close the SSH channel without sending an
     * explicit exit-status message.</p>
     *
     * @param session the authenticated SSH session
     * @param target the target device
     * @param command the command to execute
     * @param startedAt the operation start time obtained from
     *                  {@link System#nanoTime()}
     * @return the command execution result
     * @throws Exception if channel creation or command execution fails
     */
    private SshCommandResultDTO executeCommand(
            ClientSession session,
            SshTarget target,
            String command,
            long startedAt
    ) throws Exception {

        LimitedOutputStream stdout =
                new LimitedOutputStream(
                        properties.maximumOutputBytes()
                );

        LimitedOutputStream stderr =
                new LimitedOutputStream(
                        properties.maximumOutputBytes()
                );

        try (ChannelExec channel =
                     session.createExecChannel(command)) {

            /*
             * An exec channel is used instead of an interactive shell.
             * No pseudo-terminal is requested.
             */
            channel.setOut(stdout);
            channel.setErr(stderr);

            channel.open()
                    .verify(properties.connectionTimeout());

            Set<ClientChannelEvent> events =
                    channel.waitFor(
                            EnumSet.of(
                                    ClientChannelEvent.CLOSED,
                                    ClientChannelEvent.TIMEOUT
                            ),
                            properties.commandTimeout().toMillis()
                    );

            if (events.contains(ClientChannelEvent.TIMEOUT)) {
                channel.close(true);

                throw new SshCommandTimeoutException(
                        "The SSH command timed out."
                );
            }

            Integer exitStatus = channel.getExitStatus();

            long duration =
                    elapsedMilliseconds(startedAt);

            /*
             * Some network appliances may close the channel without
             * returning an SSH exit-status message.
             */
            if (exitStatus == null || exitStatus == 0) {
                return SshCommandResultDTO.success(
                        target.deviceId(),
                        target.title(),
                        target.ipAddress(),
                        exitStatus,
                        stdout.asString(),
                        stderr.asString(),
                        duration
                );
            }

            return SshCommandResultDTO.remoteCommandFailure(
                    target.deviceId(),
                    target.title(),
                    target.ipAddress(),
                    exitStatus,
                    stdout.asString(),
                    stderr.asString(),
                    duration
            );
        }
    }

    /**
     * Validates the SSH target before any connection or decryption
     * operation is attempted.
     *
     * <p>The validation ensures that the target contains a device ID,
     * IP address, valid SSH port, username, and encrypted password.</p>
     *
     * @param target the target to validate
     * @throws IllegalArgumentException if the target or one of its
     *                                  required values is invalid
     */
    private void validateTarget(SshTarget target) {

        if (target == null) {
            throw new IllegalArgumentException(
                    "SSH target is required."
            );
        }

        if (target.deviceId() == null) {
            throw new IllegalArgumentException(
                    "Device ID is missing."
            );
        }

        if (target.ipAddress() == null
                || target.ipAddress().isBlank()) {
            throw new IllegalArgumentException(
                    "Device IP address is missing."
            );
        }

        if (target.sshPort() < 1
                || target.sshPort() > 65_535) {
            throw new IllegalArgumentException(
                    "Device SSH port is invalid."
            );
        }

        if (target.username() == null
                || target.username().isBlank()) {
            throw new IllegalArgumentException(
                    "Device SSH username is missing."
            );
        }

        if (target.encryptedPassword() == null
                || target.encryptedPassword().isBlank()) {
            throw new IllegalArgumentException(
                    "Device SSH password is missing."
            );
        }
    }

    /**
     * Converts an internal exception into a safe client-facing error
     * message.
     *
     * <p>The returned messages intentionally avoid exposing stack
     * traces, credentials, implementation details, or low-level
     * network information.</p>
     *
     * @param exception the exception raised during SSH execution
     * @return a generalized error message suitable for an API response
     */

    private String safeErrorMessage(Exception exception) {

        if (exception instanceof SshCommandTimeoutException) {
            return "The command exceeded the execution timeout.";
        }

        if (exception instanceof ConnectException) {
            return "The device could not be reached.";
        }

        if (exception instanceof SocketTimeoutException) {
            return "The SSH connection timed out.";
        }

        if (exception instanceof SshException) {
            return "SSH connection or authentication failed.";
        }

        return "The SSH command could not be executed.";
    }

    /**
     * Calculates the elapsed execution time in milliseconds.
     *
     * <p>{@link System#nanoTime()} is used because it is monotonic
     * and therefore appropriate for measuring elapsed durations.</p>
     *
     * @param startedAt the start time obtained from
     *                  {@link System#nanoTime()}
     * @return the elapsed duration in milliseconds
     */
    private long elapsedMilliseconds(long startedAt) {
        return Duration.ofNanos(
                System.nanoTime() - startedAt
        ).toMillis();
    }
}
