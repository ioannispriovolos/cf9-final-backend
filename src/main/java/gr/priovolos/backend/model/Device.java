package gr.priovolos.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Entity representing a managed network device.
 *
 * <p>A device corresponds to a physical or virtual network appliance
 * that can be remotely administered through the application using
 * the SSH protocol.</p>
 *
 * <p>Each device stores its identifying information, network
 * connectivity details, and the credentials required for SSH
 * authentication. Device credentials are stored in encrypted form
 * and are decrypted only when an authorized SSH operation is
 * performed.</p>
 *
 * <p>This entity inherits auditing and soft-delete functionality
 * from {@link AbstractEntity}, allowing device creation,
 * modification, and logical deletion to be tracked automatically.</p>
 *
 * @author Ioannis Priovolos
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "devices")
public class Device extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 100)
    private String manufacturer;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "ssh_port", nullable = false)
    private Integer sshPort = 22;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    /**
     * Initializes auditing timestamps before the entity is first
     * persisted.
     *
     * <p>If the creation or modification timestamps have not already
     * been assigned, they are initialized with the current UTC
     * timestamp.</p>
     */
    @PrePersist
    protected void onCreate() {
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
     *
     * <p>The {@code updatedAt} field is refreshed with the current
     * UTC timestamp, allowing the application to track the most
     * recent modification.</p>
     */
    @PreUpdate
    protected void onUpdate() {
        this.setUpdatedAt(Instant.now());
    }
}