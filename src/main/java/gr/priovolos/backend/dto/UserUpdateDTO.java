package gr.priovolos.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserUpdateDTO(
        @NotNull
        @Size(min = 2, max = 20)
        String username,

        Long roleId
) {
}
