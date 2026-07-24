package gr.priovolos.backend.repository;

import gr.priovolos.backend.model.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    //Page<Device> findAllByDeletedFalse(Pageable pageable);
    Optional<Device> findByIdAndDeletedFalse(Long id);
    List<Device> findAllByDeletedFalse();
}
