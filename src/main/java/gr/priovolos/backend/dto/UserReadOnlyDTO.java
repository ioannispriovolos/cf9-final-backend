package gr.priovolos.backend.dto;

import java.util.UUID;

public record UserReadOnlyDTO(UUID uuid, String username, String role) {
}

