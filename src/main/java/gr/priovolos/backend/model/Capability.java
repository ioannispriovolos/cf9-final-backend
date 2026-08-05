package gr.priovolos.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entity representing an application capability (permission).
 *
 * <p>A capability defines a specific action that can be performed
 * within the application, such as viewing users, creating network
 * devices, executing SSH commands, or deleting entities.</p>
 *
 * <p>Capabilities are assigned to one or more {@link Role} entities,
 * implementing a Role-Based Access Control (RBAC) model in which
 * users inherit permissions through their assigned role.</p>
 *
 * <p>The relationship between roles and capabilities is many-to-many,
 * allowing a single capability to be shared by multiple roles and
 * each role to contain multiple capabilities.</p>
 *
 * @author Ioannis Priovolos
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "capabilities")
public class Capability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.NONE)
    @ManyToMany(mappedBy = "capabilities", fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();

    public Set<Role> getAllRoles() {
        return Set.copyOf(roles);
    }

    public void addRole(Role role) {
        roles.add(role);
        role.getCapabilities().add(this);
    }

    public void removeRole(Role role) {
        roles.remove(role);
        role.getCapabilities().remove(this);
    }

    /**
     * Determines whether another object represents the same
     * capability.
     *
     * <p>Capabilities are considered equal when they have the same
     * unique capability name.</p>
     *
     * @param o the object to compare with this capability
     * @return {@code true} if both capabilities have the same name;
     *         otherwise {@code false}
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Capability that)) return false;
        return Objects.equals(getName(), that.getName());
    }

    /**
     * Returns the hash code of this capability.
     *
     * <p>The hash code is derived solely from the capability name,
     * ensuring consistency with the {@link #equals(Object)}
     * implementation.</p>
     *
     * @return the hash code of this capability
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(getName());
    }
}