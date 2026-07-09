package gr.priovolos.backend.dto;

public record ErrorResponseDTO(String code, String description) {

    public ErrorResponseDTO(String code) {
        this(code, "");     // Calls the canonical constructor
    }
}

