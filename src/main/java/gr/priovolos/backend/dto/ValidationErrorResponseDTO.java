package gr.priovolos.backend.dto;

import java.util.Map;

/**
 * Data Transfer Object representing a validation error response.
 *
 * <p>This DTO is returned when a client request fails validation.
 * In addition to a general error code and description, it contains
 * a collection of field-specific validation errors that explain
 * why the request could not be processed.</p>
 *
 * <p>The {@code errors} map associates each invalid request field
 * with its corresponding validation error message, allowing client
 * applications to display meaningful feedback to end users.</p>
 *
 * <p>Instances of this record are immutable and intended exclusively
 * for API responses.</p>
 *
 * @param code the application-specific validation error code
 * @param description a human-readable description of the validation failure
 * @param errors a map containing field names as keys and their
 *               corresponding validation error messages as values
 *
 * @author Ioannis Priovolos
 */
public record ValidationErrorResponseDTO(String code, String description, Map<String, String> errors) {}

