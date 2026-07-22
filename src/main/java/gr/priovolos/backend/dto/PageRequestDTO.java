package gr.priovolos.backend.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record PageRequestDTO(
        int page,
        int size,
        String sortBy,
        String direction
) {

    private static final int MAX_SIZE = 50;


    public Pageable toPageable() {

        int safeSize = Math.min(size, MAX_SIZE);

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();


        return PageRequest.of(
                Math.max(page, 0),
                safeSize,
                sort
        );
    }
}
