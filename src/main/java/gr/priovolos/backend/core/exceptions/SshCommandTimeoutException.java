package gr.priovolos.backend.core.exceptions;

/**
 * Exception thrown when an SSH command does not complete within the
 * configured execution timeout.
 *
 * <p>This exception indicates that the remote network device did not
 * return a response before the configured command timeout elapsed.
 * The timeout may be caused by slow device performance, network
 * latency, an unresponsive device, or a long-running command.</p>
 *
 * <p>This is an unchecked exception because it represents an
 * execution-time failure that may occur during SSH communication
 * and is typically handled by the application's global exception
 * handling mechanism or by the SSH execution service.</p>
 *
 * @author Ioannis Priovolos
 */
public class SshCommandTimeoutException extends RuntimeException {

    /**
     * Creates a new exception indicating that an SSH command exceeded
     * the configured execution timeout.
     *
     * @param message the human-readable description of the timeout
     */
    public SshCommandTimeoutException(String message) {
        super(message);
    }
}
