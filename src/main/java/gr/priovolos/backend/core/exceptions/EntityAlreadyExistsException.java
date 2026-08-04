package gr.priovolos.backend.core.exceptions;

/**
 * Exception thrown when an attempt is made to create an entity that
 * already exists in the system.
 *
 * <p>This exception is typically raised when a business rule requiring
 * uniqueness is violated, such as attempting to create a user with an
 * existing username.</p>
 *
 * <p>The supplied error code is automatically suffixed with
 * {@code "AlreadyExists"} to produce a standardized application-specific
 * error code used by the global exception handling mechanism.</p>
 *
 * <p>Example:</p>
 * <pre>
 * new EntityAlreadyExistsException("USER_", "Username already exists.")
 * </pre>
 *
 * produces the error code:
 *
 * <pre>
 * USER_AlreadyExists
 * </pre>
 *
 * @author Ioannis Priovolos
 */
public class EntityAlreadyExistsException extends AppGenericException {

    /**
     * Default suffix appended to all entity already exists error codes.
     */
    private static final String DEFAULT_CODE = "AlreadyExists";

    /**
     * Creates a new exception indicating that an entity already exists.
     *
     * @param code the application-specific error code prefix
     * @param message the human-readable description of the error
     */
    public EntityAlreadyExistsException(String code, String message) {
        super(code + DEFAULT_CODE, message);
    }
}
