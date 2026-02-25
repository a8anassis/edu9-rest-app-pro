package gr.aueb.cf.eduapp.api;

import gr.aueb.cf.eduapp.core.exception.EntityAlreadyExistsException;
import gr.aueb.cf.eduapp.core.exception.EntityInvalidArgumentException;
import gr.aueb.cf.eduapp.core.exception.ValidationException;
import gr.aueb.cf.eduapp.dto.UserInsertDTO;
import gr.aueb.cf.eduapp.dto.UserReadOnlyDTO;
import gr.aueb.cf.eduapp.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

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

        URI location = URI.create("/api/v1/users/" + userReadOnlyDTO.uuid());
        return ResponseEntity
                .created(location)
                .body(userReadOnlyDTO);
    }

}
