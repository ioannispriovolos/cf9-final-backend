package gr.priovolos.backend.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PageResponseDTO<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public static <E, D> PageResponseDTO<D> from(
            Page<E> page,
            Function<E, D> mapper
    ) {

        return new PageResponseDTO<>(
                page.getContent()
                        .stream()
                        .map(mapper)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}