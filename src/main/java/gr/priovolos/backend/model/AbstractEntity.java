package gr.priovolos.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Base class for all persistent entities in the application.
 *
 * <p>This mapped superclass provides common auditing and soft-delete
 * functionality shared by multiple domain entities. By centralizing
 * these properties, duplication is reduced and consistent behavior
 * is enforced throughout the persistence layer.</p>
 *
 * <p>Spring Data JPA auditing automatically populates the creation
 * and last modification timestamps through the
 * {@link AuditingEntityListener}.</p>
 *
 * <p>Entities inheriting from this class support logical
 * (soft) deletion. Instead of being physically removed from the
 * database, they are marked as deleted and the deletion timestamp
 * is recorded, allowing historical data to be preserved.</p>
 *
 * @author Ioannis Priovolos
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant createdAt;              // UTC

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant updatedAt;

    @Column(nullable = false)
    private boolean deleted;

    @Column(name = "deleted_at", columnDefinition = "TIMESTAMPTZ")
    private Instant deletedAt;

    /**
     * Performs a logical (soft) deletion of the entity.
     *
     * <p>Instead of permanently removing the entity from the
     * database, this method marks it as deleted and records the
     * current UTC timestamp. Soft-deleted entities remain stored
     * for auditing and historical purposes but are typically
     * excluded from normal application operations.</p>
     */
    public void softDelete() {
        this.deleted = true;
        this.deletedAt = Instant.now();
    }
}
