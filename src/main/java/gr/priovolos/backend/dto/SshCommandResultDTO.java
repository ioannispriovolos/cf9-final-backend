package gr.priovolos.backend.dto;

/**
 * Read-only Data Transfer Object representing the result of executing
 * an SSH command on a single network device.
 *
 * <p>This DTO is part of a batch SSH execution response and contains
 * both execution metadata and the command output for an individual
 * network device.</p>
 *
 * <p>Each instance represents exactly one target device, regardless
 * of whether the command completed successfully, failed on the remote
 * device, or could not be executed due to a communication or runtime
 * error.</p>
 *
 * <p>Instances of this record are immutable and intended exclusively
 * for API responses.</p>
 *
 * @param deviceId the unique identifier of the target device
 * @param deviceTitle the descriptive name of the target device
 * @param ipAddress the IPv4 address of the target device
 * @param successful indicates whether the SSH command completed
 *                   successfully
 * @param exitStatus the exit status returned by the remote command;
 *                   may be {@code null} if the command was not
 *                   executed
 * @param output the standard output produced by the command
 * @param errorOutput the standard error output produced by the
 *                    remote command
 * @param errorMessage a human-readable error message describing
 *                     the failure when the command could not be
 *                     executed successfully
 * @param durationMs the execution time for this device,
 *                   expressed in milliseconds
 *
 * @author Ioannis Priovolos
 */
public record SshCommandResultDTO(

        Long deviceId,
        String deviceTitle,
        String ipAddress,
        boolean successful,
        Integer exitStatus,
        String output,
        String errorOutput,
        String errorMessage,
        long durationMs
) {

    /**
     * Creates a successful SSH command execution result.
     *
     * <p>This factory method should be used when the SSH command
     * completes successfully and the remote device returns an
     * execution result.</p>
     *
     * @param deviceId the identifier of the target device
     * @param deviceTitle the device title
     * @param ipAddress the device IP address
     * @param exitStatus the exit status returned by the remote command
     * @param output the command's standard output
     * @param errorOutput the command's standard error output
     * @param durationMs the execution duration in milliseconds
     * @return a successful SSH command result
     */
    public static SshCommandResultDTO success(
            Long deviceId,
            String deviceTitle,
            String ipAddress,
            Integer exitStatus,
            String output,
            String errorOutput,
            long durationMs
    ) {
        return new SshCommandResultDTO(
                deviceId,
                deviceTitle,
                ipAddress,
                true,
                exitStatus,
                output,
                errorOutput,
                null,
                durationMs
        );
    }

    /**
     * Creates a result indicating that the SSH connection succeeded,
     * but the remote command itself reported a failure.
     *
     * <p>Examples include invalid command syntax, insufficient
     * privileges, or other command-level errors returned by the
     * remote operating system.</p>
     *
     * @param deviceId the identifier of the target device
     * @param deviceTitle the device title
     * @param ipAddress the device IP address
     * @param exitStatus the exit status returned by the remote command
     * @param output the command's standard output
     * @param errorOutput the command's standard error output
     * @param durationMs the execution duration in milliseconds
     * @return a failed SSH command result representing a remote
     *         command failure
     */
    public static SshCommandResultDTO remoteCommandFailure(
            Long deviceId,
            String deviceTitle,
            String ipAddress,
            Integer exitStatus,
            String output,
            String errorOutput,
            long durationMs
    ) {
        return new SshCommandResultDTO(
                deviceId,
                deviceTitle,
                ipAddress,
                false,
                exitStatus,
                output,
                errorOutput,
                "The remote command reported a failure.",
                durationMs
        );
    }

    /**
     * Creates a result indicating that the SSH command could not
     * be executed.
     *
     * <p>This factory method is intended for failures occurring
     * before or during command execution, such as connection
     * failures, authentication errors, timeouts, or unexpected
     * runtime exceptions.</p>
     *
     * @param deviceId the identifier of the target device
     * @param deviceTitle the device title
     * @param ipAddress the device IP address
     * @param errorMessage a description of the execution failure
     * @param durationMs the execution duration in milliseconds
     * @return a failed SSH command result representing an execution
     *         failure
     */
    public static SshCommandResultDTO executionFailure(
            Long deviceId,
            String deviceTitle,
            String ipAddress,
            String errorMessage,
            long durationMs
    ) {
        return new SshCommandResultDTO(
                deviceId,
                deviceTitle,
                ipAddress,
                false,
                null,
                "",
                "",
                errorMessage,
                durationMs
        );
    }
}
