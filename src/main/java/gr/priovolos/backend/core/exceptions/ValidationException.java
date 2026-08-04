package gr.priovolos.backend.core.exceptions;

import lombok.Getter;
import org.springframework.validation.BindingResult;

/**
 * Exception thrown when request validation fails.
 *
 * <p>This exception encapsulates Spring's {@link BindingResult},
 * allowing detailed validation errors to be propagated to the
 * application's global exception handler.</p>
 *
 * <p>It is typically thrown when an incoming request contains
 * invalid or missing data that violates Bean Validation
 * constraints, such as {@code @NotBlank}, {@code @Size},
 * {@code @Pattern}, or custom validation rules.</p>
 *
 * <p>The exception generates a standardized application-specific
 * error code by appending {@code "ValidationError"} to the supplied
 * code prefix. When no prefix is provided, the default error code
 * {@code "ValidationError"} is used.</p>
 *
 * <p>The contained {@link BindingResult} provides access to all
 * field and object validation errors so they can be returned to
 * API clients in a structured format.</p>
 *
 * @author Ioannis Priovolos
 */
@Getter
public class ValidationException extends AppGenericException {

    /**
     * Default suffix appended to validation error codes.
     */
    private static final String DEFAULT_CODE = "ValidationError";

    /**
     * Contains all validation errors detected during request binding.
     */
    private final BindingResult bindingResult;

    /**
     * Creates a validation exception using the supplied error code
     * prefix and validation results.
     *
     * <p>The final application-specific error code is created by
     * appending {@code "ValidationError"} to the supplied prefix.</p>
     *
     * <p>Example:</p>
     * <pre>
     * new ValidationException(
     *         "USER_",
     *         "User validation failed.",
     *         bindingResult
     * );
     * </pre>
     *
     * produces the error code:
     *
     * <pre>
     * USER_ValidationError
     * </pre>
     *
     * @param code the application-specific error code prefix
     * @param message the human-readable validation error message
     * @param bindingResult contains the validation errors
     */
    public ValidationException(String code, String message, BindingResult bindingResult) {
        super(code + DEFAULT_CODE, message);
        this.bindingResult = bindingResult;
    }

    // generic fallback
    public ValidationException(BindingResult bindingResult) {
        super(DEFAULT_CODE, "Validation failed");
        this.bindingResult = bindingResult;
    }
}
