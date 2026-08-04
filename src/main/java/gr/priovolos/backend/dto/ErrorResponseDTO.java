package gr.priovolos.backend.dto;

/**
 * Data Transfer Object representing a standardized API error response.
 *
 * <p>This DTO is returned by the application's global exception
 * handling mechanism whenever a request cannot be completed
 * successfully.</p>
 *
 * <p>Each response contains an application-specific error code and
 * an optional human-readable description explaining the cause of the
 * failure.</p>
 *
 * <p>Using a consistent error response structure allows client
 * applications to handle errors in a predictable manner regardless
 * of the exception that generated them.</p>
 *
 * <p>Instances of this record are immutable and intended exclusively
 * for API responses.</p>
 *
 * @param code the application-specific error code identifying
 *             the type of error
 * @param description a human-readable description of the error;
 *                    may be empty when no additional details
 *                    are provided
 *
 * @author Ioannis Priovolos
 */
public record ErrorResponseDTO(String code, String description) {

    /**
     * Creates an error response containing only the application-
     * specific error code.
     *
     * <p>The description is initialized to an empty string.</p>
     *
     * @param code the application-specific error code
     */
    public ErrorResponseDTO(String code) {
        this(code, "");     // Calls the canonical constructor
    }
}

