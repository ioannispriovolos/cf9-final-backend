package gr.priovolos.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

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

    @PrePersist
    protected void onCreate() {
        if (this.getCreatedAt() == null) {
            this.setCreatedAt(Instant.now());
        }

        if (this.getUpdatedAt() == null) {
            this.setUpdatedAt(Instant.now());
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.setUpdatedAt(Instant.now());
    }
}