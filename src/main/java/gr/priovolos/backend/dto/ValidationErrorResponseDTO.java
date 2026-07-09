package gr.priovolos.backend.dto;

import java.util.Map;

public record ValidationErrorResponseDTO(String code, String description, Map<String, String> errors) {}

