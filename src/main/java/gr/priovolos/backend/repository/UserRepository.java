package gr.priovolos.backend.repository;

import gr.priovolos.backend.model.Role;
import gr.priovolos.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link User} entities.
 *
 * <p>This repository provides persistence operations for application
 * users. In addition to the standard CRUD functionality inherited
 * from {@link JpaRepository}, it supports dynamic query construction
 * through {@link JpaSpecificationExecutor} and provides custom query
 * methods used by the authentication and user management modules.</p>
 *
 * <p>Entity graphs are employed where appropriate to eagerly fetch
 * related entities and reduce the number of SQL queries executed
 * during authentication and user retrieval.</p>
 *
 * @author Ioannis Priovolos
 */
public interface UserRepository extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    /**
     * Retrieves a user by their public UUID.
     *
     * <p>This method returns both active and soft-deleted users.</p>
     *
     * @param uuid the user's public UUID
     * @return an {@link Optional} containing the matching user,
     *         or an empty {@link Optional} if no user exists
     */
    Optional<User> findByUuid(UUID uuid);


    /**
     * Retrieves an active (non-deleted) user by their public UUID.
     *
     * @param uuid the user's public UUID
     * @return an {@link Optional} containing the matching active
     *         user, or an empty {@link Optional} if no matching
     *         user exists
     */
    Optional<User> findByUuidAndDeletedFalse(UUID uuid);

    /**
     * Retrieves a user by their username together with the user's
     * assigned role and capabilities.
     *
     * <p>The associated {@link Role} and its capabilities are eagerly
     * loaded using an {@link EntityGraph} to avoid additional SQL
     * queries during authentication and authorization.</p>
     *
     * <p>This method is primarily used by Spring Security's
     * {@code UserDetailsService} during the authentication process.</p>
     *
     * @param username the username of the user
     * @return an {@link Optional} containing the matching user,
     *         or an empty {@link Optional} if no user exists
     */
    @EntityGraph(attributePaths = {"role", "role.capabilities"})
    Optional<User> findByUsername(String username);

    /**
     * Retrieves a paginated list of active (non-deleted) users.
     *
     * <p>The associated {@link Role} is eagerly loaded using an
     * {@link EntityGraph} to avoid additional lazy-loading queries
     * when constructing API responses.</p>
     *
     * @param pageable pagination and sorting information
     * @return a page containing active users
     */
    @EntityGraph(attributePaths = {"role"})
    Page<User> findAllByDeletedFalse(Pageable pageable);
}
