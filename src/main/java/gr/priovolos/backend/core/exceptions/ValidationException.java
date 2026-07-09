package gr.priovolos.backend.core.exceptions;

import lombok.Getter;
import org.springframework.validation.BindingResult;

@Getter
public class ValidationException extends AppGenericException {
    private static final String DEFAULT_CODE = "ValidationError";
    private final BindingResult bindingResult;

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
