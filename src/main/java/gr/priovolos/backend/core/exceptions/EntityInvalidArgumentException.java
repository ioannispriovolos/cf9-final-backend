package gr.priovolos.backend.core.exceptions;

/**
 * Exception thrown when an entity contains one or more invalid
 * business values.
 *
 * <p>This exception is used when the supplied data passes basic
 * request validation but violates one or more business rules.
 * Examples include invalid entity state, unsupported values,
 * or other domain-specific validation failures.</p>
 *
 * <p>The supplied error code is automatically suffixed with
 * {@code "InvalidArgument"} to produce a standardized
 * application-specific error code that is returned by the
 * global exception handling mechanism.</p>
 *
 * <p>Example:</p>
 * <pre>
 * new EntityInvalidArgumentException(
 *         "USER_",
 *         "The selected role is invalid."
 * );
 * </pre>
 *
 * produces the error code:
 *
 * <pre>
 * USER_InvalidArgument
 * </pre>
 *
 * @author Ioannis Priovolos
 */
public class EntityInvalidArgumentException extends AppGenericException {

    /**
     * Default suffix appended to all invalid argument error codes.
     */
    private static final String DEFAULT_CODE = "InvalidArgument";

    /**
     * Creates a new exception indicating that one or more supplied
     * entity values violate business validation rules.
     *
     * @param code the application-specific error code prefix
     * @param message the human-readable description of the validation
     *                failure
     */
    public EntityInvalidArgumentException(String code, String message) {
        super(code + DEFAULT_CODE, message);
    }
}
