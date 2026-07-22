package gr.priovolos.backend.service;

import gr.priovolos.backend.core.exceptions.EntityAlreadyExistsException;
import gr.priovolos.backend.core.exceptions.EntityInvalidArgumentException;
import gr.priovolos.backend.core.exceptions.EntityNotFoundException;
import gr.priovolos.backend.dto.PageResponseDTO;
import gr.priovolos.backend.dto.UserInsertDTO;
import gr.priovolos.backend.dto.UserReadOnlyDTO;
import gr.priovolos.backend.dto.UserUpdateDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface IUserService {
    UserReadOnlyDTO saveUser(UserInsertDTO userInsertDTO)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException;

    UserReadOnlyDTO getUserByUUID(UUID uuid) throws EntityNotFoundException;
    UserReadOnlyDTO getUserByUUIDDeletedFalse(UUID uuid) throws EntityNotFoundException;
    UserReadOnlyDTO deleteUserByUUID(UUID uuid) throws EntityNotFoundException;

    //List<UserReadOnlyDTO> getAllUsersReadOnly();

    UserReadOnlyDTO updateUserByUuid(UUID uuid, UserUpdateDTO dto) throws EntityNotFoundException;

    PageResponseDTO<UserReadOnlyDTO> getAllUsersPaginated(Pageable pageable);
}
