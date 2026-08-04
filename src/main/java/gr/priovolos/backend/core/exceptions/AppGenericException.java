package gr.priovolos.backend.core.exceptions;

import lombok.Getter;

/**
 * Base checked exception for application-specific errors.
 *
 * <p>This exception provides a common structure for errors generated
 * by the application's business and service layers. In addition to
 * the standard exception message, each exception contains an
 * application-specific error code.</p>
 *
 * <p>Specialized exceptions can extend this class to represent
 * specific error conditions, such as entities that are not found,
 * already exist, or contain invalid data.</p>
 *
 * <p>The error code can be used by the application's global exception
 * handling mechanism to produce consistent API error responses.</p>
 *
 * @author Ioannis Priovolos
 */
@Getter
public class AppGenericException extends Exception {

    /**
     * Application-specific code identifying the type of error.
     */
    private final String code;

    /**
     * Creates a new application exception with the specified error
     * code and descriptive message.
     *
     * @param code the application-specific error code
     * @param message the human-readable description of the error
     */
    public AppGenericException(String code, String message) {
        super(message);
        this.code = code;
    }
}
