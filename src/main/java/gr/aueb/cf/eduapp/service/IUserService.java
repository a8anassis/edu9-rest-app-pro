package gr.aueb.cf.eduapp.service;

import gr.aueb.cf.eduapp.core.exception.EntityAlreadyExistsException;
import gr.aueb.cf.eduapp.core.exception.EntityInvalidArgumentException;
import gr.aueb.cf.eduapp.core.exception.EntityNotFoundException;
import gr.aueb.cf.eduapp.dto.UserInsertDTO;
import gr.aueb.cf.eduapp.dto.UserReadOnlyDTO;

import java.util.UUID;

public interface IUserService {
    UserReadOnlyDTO saveUser(UserInsertDTO userInsertDTO)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException;

    UserReadOnlyDTO getUserByUUID(UUID uuid) throws EntityNotFoundException;
    UserReadOnlyDTO getUserByUUIDDeletedFalse(UUID uuid) throws EntityNotFoundException;
}
