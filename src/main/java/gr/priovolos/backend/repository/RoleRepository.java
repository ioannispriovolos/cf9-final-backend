package gr.priovolos.backend.repository;

import gr.priovolos.backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findAllByOrderByNameAsc();
}