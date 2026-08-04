package gr.priovolos.backend.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Generic Data Transfer Object representing a paginated API response.
 *
 * <p>This DTO wraps a page of data together with the pagination
 * metadata required by client applications. It provides a consistent
 * response format for all paginated REST endpoints within the
 * application.</p>
 *
 * <p>The DTO is independent of Spring Data's {@link Page} interface,
 * preventing persistence-specific classes from being exposed through
 * the REST API.</p>
 *
 * <p>Instances of this record are immutable and intended exclusively
 * for API responses.</p>
 *
 * @param <T> the type of objects contained in the page
 * @param content the records contained in the current page
 * @param page the zero-based page index
 * @param size the number of records contained in the page
 * @param totalElements the total number of records available
 * @param totalPages the total number of available pages
 * @param first indicates whether this is the first page
 * @param last indicates whether this is the last page
 *
 * @author Ioannis Priovolos
 */
public record PageResponseDTO<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    /**
     * Creates a {@code PageResponseDTO} from a Spring Data
     * {@link Page}.
     *
     * <p>Each entity contained in the supplied page is converted
     * into its corresponding DTO using the provided mapping
     * function.</p>
     *
     * <p>This helper method enables service classes to return
     * paginated DTOs without exposing entity classes to the
     * presentation layer.</p>
     *
     * @param page the source page containing entity objects
     * @param mapper the function used to convert each entity into
     *               its corresponding DTO
     * @param <E> the entity type
     * @param <D> the DTO type
     * @return a paginated response containing the mapped DTOs and
     *         pagination metadata
     */
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