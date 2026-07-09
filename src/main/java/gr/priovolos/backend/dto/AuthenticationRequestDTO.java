package gr.priovolos.backend.dto;

import jakarta.validation.constraints.NotNull;

public record AuthenticationRequestDTO(@NotNull String username, @NotNull String password) {

}