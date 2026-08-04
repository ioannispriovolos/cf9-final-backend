package gr.priovolos.backend.core.exceptions;

/**
 * Exception thrown when a requested entity cannot be found.
 *
 * <p>This exception is used when an operation attempts to access an
 * entity that does not exist or is not available for retrieval.
 * Typical scenarios include requesting a non-existent user, device,
 * or other domain entity by its identifier.</p>
 *
 * <p>The supplied error code is automatically suffixed with
 * {@code "NotFound"} to produce a standardized application-specific
 * error code used by the global exception handling mechanism.</p>
 *
 * <p>Example:</p>
 * <pre>
 * new EntityNotFoundException(
 *         "DEVICE_",
 *         "Device not found."
 * );
 * </pre>
 *
 * produces the error code:
 *
 * <pre>
 * DEVICE_NotFound
 * </pre>
 *
 * @author Ioannis Priovolos
 */
public class EntityNotFoundException extends AppGenericException {

    /**
     * Default suffix appended to all entity not found error codes.
     */
    private static final String DEFAULT_CODE = "NotFound";

    /**
     * Creates a new exception indicating that the requested entity
     * could not be found.
     *
     * @param code the application-specific error code prefix
     * @param message the human-readable description of the error
     */
    public EntityNotFoundException(String code, String message) {
        super(code + DEFAULT_CODE, message);
    }
}
