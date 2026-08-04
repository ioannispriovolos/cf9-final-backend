package gr.priovolos.backend.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Data Transfer Object representing pagination and sorting
 * information supplied by a client.
 *
 * <p>This DTO provides a convenient way of transferring pagination
 * parameters for endpoints that return large collections of data.</p>
 *
 * <p>The supplied values are converted into Spring Data's
 * {@link Pageable} abstraction, allowing repository methods to
 * perform efficient paginated queries.</p>
 *
 * <p>To protect the application from excessively large requests,
 * the page size is automatically limited to a maximum of
 * {@value #MAX_SIZE} records per page.</p>
 *
 * <p>Instances of this record are immutable and intended for
 * request payloads.</p>
 *
 * @param page the zero-based page index to retrieve
 * @param size the requested number of records per page
 * @param sortBy the entity property used for sorting
 * @param direction the sort direction, either
 *                  {@code "asc"} or {@code "desc"}
 *
 * @author Ioannis Priovolos
 */
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
