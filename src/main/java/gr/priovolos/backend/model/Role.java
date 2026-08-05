package gr.priovolos.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entity representing an application role.
 *
 * <p>A role groups together one or more application capabilities
 * (permissions) and can be assigned to multiple users. Users inherit
 * their authorization privileges through the capabilities associated
 * with their assigned role, implementing a Role-Based Access Control
 * (RBAC) model with fine-grained capabilities.</p>
 *
 * <p>Examples of roles include <strong>ADMIN</strong>,
 * <strong>NETWORK_ENGINEER</strong>, and <strong>VIEWER</strong>.</p>
 *
 * <p>A role maintains bidirectional relationships with both
 * {@link User} and {@link Capability} entities.</p>
 *
 * @author Ioannis Priovolos
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.PROTECTED)
    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.PROTECTED)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "roles_capabilities",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "capability_id")
    )
    private Set<Capability> capabilities = new HashSet<>();

    public Set<Capability> getAllCapabilities() {
        return Set.copyOf(capabilities);
    }

    public Set<User> getAllUsers() {
        return Set.copyOf(users);
    }

    public void addCapability(Capability capability) {
        capabilities.add(capability);
        capability.getRoles().add(this);
    }

    public void removeCapability(Capability capability) {
        capabilities.remove(capability);
        capability.getRoles().remove(this);
    }

    /**
     * Assigns a user to this role.
     *
     * <p>This method updates both sides of the bidirectional
     * one-to-many relationship.</p>
     *
     * @param user the user to assign
     */
    public void addUser(User user) {
        users.add(user);
        user.setRole(this);
    }

    public void removeUser(User user) {
        users.remove(user);
        user.setRole(null);
    }

    public void addUsers(Collection<User> users) {
        users.forEach(this::addUser);
    }

    /**
     * Determines whether another object represents the same role.
     *
     * <p>Two roles are considered equal when they have the same
     * unique role name.</p>
     *
     * @param o the object to compare with this role
     * @return {@code true} if both roles have the same name;
     * otherwise {@code false}
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Role role)) return false;
        return Objects.equals(getName(), role.getName());
    }

    /**
     * Returns the hash code of this role.
     *
     * <p>The hash code is derived solely from the role name,
     * ensuring consistency with the {@link #equals(Object)}
     * implementation.</p>
     *
     * @return the hash code of this role
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(getName());
    }
}