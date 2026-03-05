package gr.aueb.cf.eduapp.api;

import gr.aueb.cf.eduapp.core.exceptions.*;
import gr.aueb.cf.eduapp.core.filters.TeacherFilters;
import gr.aueb.cf.eduapp.dto.*;
import gr.aueb.cf.eduapp.service.ITeacherService;
import gr.aueb.cf.eduapp.validator.TeacherEditValidator;
import gr.aueb.cf.eduapp.validator.TeacherInsertValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teachers")
@RequiredArgsConstructor
public class TeacherRestController {

    private final ITeacherService teacherService;
    private final TeacherInsertValidator teacherInsertValidator;
    private final TeacherEditValidator teacherEditValidator;

    @Operation(
            summary = "Save a teacher",
            description = "Registers a new teacher in the system"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201", description = "Teacher created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeacherReadOnlyDTO.class))
            ),
            @ApiResponse(
                    responseCode = "409", description = "Teacher already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponseDTO.class))
            )
    })
    @PostMapping
    public ResponseEntity<TeacherReadOnlyDTO> saveTeacher(
            @Valid @RequestBody TeacherInsertDTO teacherInsertDTO,
            BindingResult bindingResult)
                throws EntityAlreadyExistsException, EntityInvalidArgumentException, ValidationException {

        teacherInsertValidator.validate(teacherInsertDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Teacher", "Invalid teacher data", bindingResult);
        }

        TeacherReadOnlyDTO teacherReadOnlyDTO = teacherService.saveTeacher(teacherInsertDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(teacherReadOnlyDTO.uuid())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(teacherReadOnlyDTO);
    }


    @Operation(
            summary = "Upload AMKA attachment file for a teacher",
            description = "Uploads a teacher's AMKA document file. Replaces existing file if present."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "File uploaded successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Teacher not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "File upload failed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @PostMapping("/{uuid}/amka-file")
    public ResponseEntity<Void> uploadAmkaFile(
            @PathVariable UUID uuid,
            @RequestParam("amkaFile") MultipartFile amkaFile
            ) throws EntityNotFoundException, FileUploadException {

        teacherService.saveAmkaFile(uuid, amkaFile);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Update a teacher")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Teacher updated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeacherReadOnlyDTO.class))
            ),
            @ApiResponse(
                    responseCode = "409", description = "Teacher already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Teacher not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401", description = "Not Authenticated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403", description = "Access Denied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @PutMapping("/{uuid}")
    public ResponseEntity<TeacherReadOnlyDTO> updateTeacher(
            @PathVariable UUID uuid,
            @Valid @RequestBody TeacherUpdateDTO teacherUpdateDTO,
            BindingResult bindingResult)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException, ValidationException, EntityNotFoundException {

        teacherEditValidator.validate(teacherUpdateDTO, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new ValidationException("Teacher", "Invalid teacher data", bindingResult);
        }

        TeacherReadOnlyDTO teacherReadOnlyDTO = teacherService.updateTeacher(teacherUpdateDTO);
        return ResponseEntity.ok(teacherReadOnlyDTO);
    }

    @Operation(summary = "Get all teachers paginated and filtered")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Teachers returned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403", description = "Access Denied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping
    public ResponseEntity<Page<TeacherReadOnlyDTO>> getFilteredAndPaginatedTeachers(
            @PageableDefault(page = 0, size = 5) Pageable pageable, @ModelAttribute TeacherFilters filters  // instantiates filters with no-args constructor
    ) throws EntityNotFoundException {
        Page<TeacherReadOnlyDTO> paginatedDTO = teacherService.getTeachersPaginatedFiltered(pageable, filters);
        return ResponseEntity.ok(paginatedDTO);
    }

    @Operation(summary = "Get one teacher by uuid")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Teacher returned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeacherReadOnlyDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404", description = "Teacher not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401", description = "Not Authenticated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403", description = "Access Denied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<TeacherReadOnlyDTO> getTeacherByUUID(@PathVariable UUID uuid)
            throws EntityNotFoundException {
//        TeacherReadOnlyDTO teacherReadOnlyDTO = teacherService.getTeacherByUUID(uuid);
        TeacherReadOnlyDTO teacherReadOnlyDTO = teacherService.getTeacherByUUIDDeletedFalse(uuid);
        return ResponseEntity.ok(teacherReadOnlyDTO);
    }


    @Operation(summary = "Deletes a teacher. It is a soft-delete design pattern.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Teacher deleted",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "404", description = "Teacher not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403", description = "Access Denied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<TeacherReadOnlyDTO> deleteTeacherByUUID(@PathVariable UUID uuid)
            throws EntityNotFoundException {
        TeacherReadOnlyDTO teacherReadOnlyDTO = teacherService.deleteTeacherByUUID(uuid);
        return ResponseEntity.ok(teacherReadOnlyDTO);
    }
}
