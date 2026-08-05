package gr.priovolos.backend.repository;

import gr.priovolos.backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Role} entities.
 *
 * <p>This repository provides persistence operations for application
 * roles. In addition to the standard CRUD functionality inherited
 * from {@link JpaRepository}, it exposes query methods used for
 * retrieving roles in a predictable order.</p>
 *
 * <p>Roles define the set of capabilities (permissions) granted to
 * users within the application's Role-Based Access Control (RBAC)
 * model.</p>
 *
 * @author Ioannis Priovolos
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Retrieves all application roles ordered alphabetically
     * by their name.
     *
     * <p>This method is used to populate role selection
     * lists in the user management interface.</p>
     *
     * @return a list of all roles sorted in ascending order by name
     */
    List<Role> findAllByOrderByNameAsc();
}