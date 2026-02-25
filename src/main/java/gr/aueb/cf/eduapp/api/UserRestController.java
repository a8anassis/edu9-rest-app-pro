package gr.aueb.cf.eduapp.api;

import gr.aueb.cf.eduapp.core.exception.EntityAlreadyExistsException;
import gr.aueb.cf.eduapp.core.exception.EntityInvalidArgumentException;
import gr.aueb.cf.eduapp.core.exception.EntityNotFoundException;
import gr.aueb.cf.eduapp.core.exception.ValidationException;
import gr.aueb.cf.eduapp.dto.UserInsertDTO;
import gr.aueb.cf.eduapp.dto.UserReadOnlyDTO;
import gr.aueb.cf.eduapp.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserRestController {

    private final IUserService userService;

    @PostMapping
    public ResponseEntity<UserReadOnlyDTO> registerUser(@Valid @RequestBody UserInsertDTO userInsertDTO,
                                                        BindingResult bindingResult)
            throws ValidationException, EntityAlreadyExistsException, EntityInvalidArgumentException {

        // TODO implement validator for business rules
        if (bindingResult.hasErrors()) {
            throw new ValidationException("User", "Invalid user data", bindingResult);
        }

        UserReadOnlyDTO userReadOnlyDTO = userService.saveUser(userInsertDTO);

        // Παράγει το πλήρες URL
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(userReadOnlyDTO.uuid())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(userReadOnlyDTO);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<UserReadOnlyDTO> getUserByUUID(@PathVariable UUID uuid)
            throws EntityNotFoundException {

        return ResponseEntity.ok(userService.getUserByUUIDDeletedFalse(uuid));
    }

}
