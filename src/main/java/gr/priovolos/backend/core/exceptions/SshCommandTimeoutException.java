package gr.priovolos.backend.core.exceptions;

public class SshCommandTimeoutException extends RuntimeException {
    public SshCommandTimeoutException(String message) {
        super(message);
    }
}
