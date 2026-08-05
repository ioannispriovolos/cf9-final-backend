package gr.priovolos.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.*;

/**
 * Entity representing an authenticated application user.
 *
 * <p>A user is responsible for accessing the application and
 * performing operations according to the permissions granted by
 * their assigned role. The application implements a Role-Based
 * Access Control (RBAC) model with fine-grained capabilities,
 * where users inherit authorities through their assigned
 * {@link Role}.</p>
 *
 * <p>This entity implements Spring Security's
 * {@link UserDetails} interface, allowing it to be used directly
 * by the authentication and authorization infrastructure.</p>
 *
 * <p>The entity inherits auditing and soft-delete functionality
 * from {@link AbstractEntity}. Soft-deleted users are considered
 * disabled and cannot authenticate.</p>
 *
 * @author Ioannis Priovolos
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class User extends AbstractEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID uuid;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)       // Hash, BCrypt
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /**
     * Creates a new user using the supplied username and password.
     *
     * <p>This constructor is typically used during user creation.
     * The password should be encoded before the entity is persisted.</p>
     *
     * @param username the user's username
     * @param password the user's password
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Returns all authorities granted to the user.
     *
     * <p>The returned collection includes:</p>
     * <ul>
     *     <li>The user's role as a Spring Security role
     *     (for example, {@code ROLE_ADMIN}).</li>
     *     <li>Every capability assigned to that role
     *     (for example, {@code VIEW_USERS},
     *     {@code INSERT_DEVICE}).</li>
     * </ul>
     *
     * <p>This allows authorization checks using both
     * {@code hasRole(...)} and {@code hasAuthority(...)}
     * expressions.</p>
     *
     * @return the granted authorities of the authenticated user
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> grantedAuthorities =  new HashSet<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        role.getCapabilities()
                .forEach(capability -> grantedAuthorities.add(new SimpleGrantedAuthority(capability.getName())));
        return grantedAuthorities;
    }

    /**
     * Indicates whether the user account has expired.
     *
     * @return always {@code true}, since account expiration is
     * not implemented
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user account is locked.
     *
     * @return always {@code true}, since account locking is
     * not implemented
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials have expired.
     *
     * @return always {@code true}, since credential expiration
     * is not implemented
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled.
     *
     * <p>Soft-deleted users are considered disabled and cannot
     * authenticate.</p>
     *
     * @return {@code true} if the user has not been soft deleted;
     * otherwise {@code false}
     */
    @Override
    public boolean isEnabled() {
        return !isDeleted();
    }

    /**
     * Determines whether another object represents the same user.
     *
     * <p>Users are considered equal when they have the same unique
     * username.</p>
     *
     * @param o the object to compare with this user
     * @return {@code true} if both users have the same username;
     * otherwise {@code false}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;

        return Objects.equals(getUsername(), user.getUsername());
    }

    /**
     * Returns the hash code of this user.
     *
     * <p>The hash code is derived solely from the username,
     * ensuring consistency with the {@link #equals(Object)}
     * implementation.</p>
     *
     * @return the hash code of this user
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(username);
    }

    /**
     * Initializes the entity before it is first persisted.
     *
     * <p>A random UUID is generated and the auditing timestamps
     * are initialized if they have not already been assigned.</p>
     */
    @PrePersist
    public void initializeUUID() {
        this.uuid = UUID.randomUUID();

        if (this.getCreatedAt() == null) {
            this.setCreatedAt(Instant.now());
        }

        if (this.getUpdatedAt() == null) {
            this.setUpdatedAt(Instant.now());
        }
    }

    /**
     * Updates the modification timestamp immediately before the
     * entity is updated.
     */
    @PreUpdate
    public void updateTimestampOnUpdate() {
        this.setUpdatedAt(Instant.now());
    }
}