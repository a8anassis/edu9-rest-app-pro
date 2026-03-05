package gr.aueb.cf.eduapp.service;

import gr.aueb.cf.eduapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.eduapp.core.exceptions.EntityInvalidArgumentException;
import gr.aueb.cf.eduapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.eduapp.core.exceptions.FileUploadException;
//import gr.aueb.cf.eduapp.core.filters.Paginated;
//import gr.aueb.cf.eduapp.core.filters.TeacherFilters;
//import gr.aueb.cf.eduapp.dto.TeacherUpdateDTO;
import gr.aueb.cf.eduapp.core.filters.TeacherFilters;
import gr.aueb.cf.eduapp.dto.TeacherInsertDTO;
import gr.aueb.cf.eduapp.dto.TeacherReadOnlyDTO;
import gr.aueb.cf.eduapp.dto.TeacherUpdateDTO;
import gr.aueb.cf.eduapp.mapper.Mapper;
import gr.aueb.cf.eduapp.model.*;
import gr.aueb.cf.eduapp.model.static_data.Region;
import gr.aueb.cf.eduapp.repository.*;
//import gr.aueb.cf.eduapp.specification.TeacherSpecification;
import gr.aueb.cf.eduapp.specification.TeacherSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service                        // IoC Container
@RequiredArgsConstructor        // DI
@Slf4j                          // Logger
public class TeacherServiceImpl implements ITeacherService {

    private final TeacherRepository teacherRepository;
    private final RegionRepository regionRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PersonalInfoRepository personalInfoRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Override
    @Transactional(rollbackFor = { EntityAlreadyExistsException.class, EntityInvalidArgumentException.class } )
    public TeacherReadOnlyDTO saveTeacher(TeacherInsertDTO dto)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException {

        try {
            if (dto.vat() != null && teacherRepository.findByVat(dto.vat()).isPresent()) {
                throw new EntityAlreadyExistsException("Teacher", "Teacher with vat=" + dto.vat() + " already exists");
            }
            if (personalInfoRepository.findByAmka(dto.personalInfoInsertDTO().amka()).isPresent()) {
                throw new EntityAlreadyExistsException("AMKA", "User with AMKA " + dto.personalInfoInsertDTO().amka() + " already exists");
            }

            if (userRepository.findByUsername(dto.userInsertDTO().username()).isPresent()) {
                throw new EntityAlreadyExistsException("Username", "User with username " + dto.userInsertDTO().username() + " already exists");
            }

            if (personalInfoRepository.findByIdentityNumber(dto.personalInfoInsertDTO().identityNumber()).isPresent()) {
                throw new EntityAlreadyExistsException("IdentityNumber", "User with identity number " + dto.personalInfoInsertDTO().identityNumber() + " already exists");
            }

            Region region = regionRepository.findById(dto.regionId())
                    .orElseThrow(() -> new EntityInvalidArgumentException("Region", "Region id=" + dto.regionId() + " invalid"));

            final Long teacherRoleId = 3L;    // Πάντα ο ρόλος είναι teacher - TODO να αλλάξει το DTO
//            Role role = roleRepository.findById(dto.userInsertDTO().roleId())
//                    .orElseThrow(() -> new EntityInvalidArgumentException("Role","Role id=" + dto.userInsertDTO().roleId() + " invalid"));
            Role role = roleRepository.findById(teacherRoleId)
                    .orElseThrow(() -> new EntityInvalidArgumentException("Role","Role id=" + teacherRoleId + " invalid"));

            Teacher teacher = mapper.mapToTeacherEntity(dto);
//            User user = mapper.mapToUserEntity(dto.userInsertDTO());
            User user = teacher.getUser();
            user.setPassword(passwordEncoder.encode(dto.userInsertDTO().password()));
            region.addTeacher(teacher);
            role.addUser(user);
//            teacher.addUser(user); added to mapper TODO
            teacherRepository.save(teacher);        // saved teacher
            log.info("Teacher with vat={} saved successfully.", dto.vat());
            return mapper.mapToTeacherReadOnlyDTO(teacher);
        } catch (EntityAlreadyExistsException e) {
            log.error("Save failed for teacher with vat={}. Teacher already exists", dto.vat(), e);     // Structured Logging
            throw e;
        } catch (EntityInvalidArgumentException e) {
            log.error("Save failed for teacher with vat={} and region id={} invalid", dto.vat(), dto.regionId());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTeacherExists(String vat) {
        return teacherRepository.findByVat(vat).isPresent();
    }


    @Override
    @PreAuthorize("hasAuthority('VIEW_TEACHERS')")
    @Transactional(readOnly = true)
    public Page<TeacherReadOnlyDTO> getPaginatedTeachers(Pageable pageable) {
        Page<Teacher> teachersPage = teacherRepository.findAll(pageable);
        log.debug("Get paginated returned successfully page={} and size={}", teachersPage.getNumber(), teachersPage.getSize());
        return teachersPage.map(mapper::mapToTeacherReadOnlyDTO);
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_TEACHERS')")
    @Transactional(readOnly = true)
    public Page<TeacherReadOnlyDTO> getPaginatedTeachersDeletedFalse(Pageable pageable) {
        Page<Teacher> teachersPage = teacherRepository.findAllByDeletedFalse(pageable);
        log.debug("Get paginated not deleted returned successfully page={} and size={}", teachersPage.getNumber(), teachersPage.getSize());
        return teachersPage.map(mapper::mapToTeacherReadOnlyDTO);
    }

    @Override
    @PreAuthorize("hasAuthority('EDIT_TEACHER')")
    @Transactional(rollbackFor = { EntityNotFoundException.class, EntityAlreadyExistsException.class, EntityInvalidArgumentException.class} )
    public TeacherReadOnlyDTO updateTeacher(TeacherUpdateDTO dto)
            throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException {
        try {
            Teacher teacher = teacherRepository.findByUuid(dto.uuid())
                    .orElseThrow(() -> new EntityNotFoundException("Teacher", "Teacher with uuid=" + dto.uuid() + " not found"));

            teacher.setFirstname(dto.firstname());
            teacher.setLastname(dto.lastname());

            if (!teacher.getVat().equals(dto.vat())) {
                if (teacherRepository.findByVat(dto.vat()).isPresent()) {
                    throw new EntityAlreadyExistsException("","Teacher with vat=" + dto.vat() + " already exists");
                }
                teacher.setVat(dto.vat());
            }

            if (!teacher.getPersonalInfo().getIdentityNumber().equals(dto.personalInfoUpdateDTO().identityNumber()) &&
                    personalInfoRepository.findByIdentityNumber(dto.personalInfoUpdateDTO().identityNumber()).isPresent()) {
                throw new EntityAlreadyExistsException("Teacher", "Teacher with identity number " + dto.personalInfoUpdateDTO().identityNumber() + " already exists");
            }

            if (!Objects.equals(dto.regionId(), teacher.getRegion().getId())) {
                Region region = regionRepository.findById(dto.regionId())
                        .orElseThrow(() -> new EntityInvalidArgumentException("Region","Region id=" + dto.regionId() + " invalid"));
                Region oldRegion = teacher.getRegion();
                if (oldRegion != null) {
                    oldRegion.removeTeacher(teacher);
                }
                region.addTeacher(teacher);
            }
            // user username and password updated TODO
            // other features to be updated TODO

            teacherRepository.save(teacher);    // προαιρετικό αν είναι managed
            log.info("Teacher with uuid={} updated successfully", dto.uuid());
            return mapper.mapToTeacherReadOnlyDTO(teacher);
        } catch (EntityNotFoundException e) {
            log.error("Update failed for teacher with uuid={}. Teacher not found", dto.uuid(), e);
            throw e;
        } catch (EntityAlreadyExistsException e) {
            log.error("Update failed for teacher with uuid={}. Teacher with vat={} already exists", dto.uuid(), dto.vat(), e);
            throw e;
        } catch (EntityInvalidArgumentException e) {
            log.error("Update failed for teacher with uuid={}. Region id={} invalid", dto.uuid(), dto.regionId(), e);
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasAuthority('DELETE_TEACHER')")
    @Transactional(rollbackFor = EntityNotFoundException.class)
    public TeacherReadOnlyDTO deleteTeacherByUUID(UUID uuid) throws EntityNotFoundException {
        try {
            Teacher teacher = teacherRepository.findByUuidAndDeletedFalse(uuid)
                    .orElseThrow(() -> new EntityNotFoundException("Teacher","Teacher with uuid=" + uuid + " not found"));

            teacher.softDelete();
            teacher.getPersonalInfo().softDelete();
            teacher.getUser().softDelete();
            // No save needed if Teacher is managed
//            teacherRepository.save(teacher);
            log.info("Teacher with uuid={} deleted successfully", uuid);
            return mapper.mapToTeacherReadOnlyDTO(teacher);
        } catch (EntityNotFoundException e) {
            log.error("Update failed for teacher with uuid={}. Teacher not found", uuid, e);

            // Automatic rollback due to @Transactional annotation
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public TeacherReadOnlyDTO getTeacherByUUID(UUID uuid) throws EntityNotFoundException {

        try {
            Teacher teacher = teacherRepository.findByUuid(uuid)
                    .orElseThrow(() -> new EntityNotFoundException("Teacher","Teacher with uuid=" + uuid + " not found"));
            log.debug("Get teacher by uuid={} returned successfully", uuid);
            return mapper.mapToTeacherReadOnlyDTO(teacher);
        } catch (EntityNotFoundException e) {
            log.error("Get teacher by uuid={} failed", uuid, e);
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_TEACHER') or (hasAuthority('VIEW_ONLY_TEACHER') and @securityService.isOwnTeacherProfile(#uuid, authentication))")
    @Transactional(readOnly = true)
    public TeacherReadOnlyDTO getTeacherByUUIDDeletedFalse(UUID uuid) throws EntityNotFoundException {
        try {
            Teacher teacher = teacherRepository.findByUuidAndDeletedFalse(uuid)
                    .orElseThrow(() -> new EntityNotFoundException("Teacher","Teacher with uuid=" + uuid + " not found"));
            log.debug("Get non-deleted teacher by uuid={} returned successfully", uuid);
            return mapper.mapToTeacherReadOnlyDTO(teacher);
        } catch (EntityNotFoundException e) {
            log.error("Get teacher by uuid={} failed", uuid, e);
            throw e;
        }
    }

    @Override
    @Retryable(
            retryFor = { IOException.class, HttpServerErrorException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2, maxDelay = 10000)
    )
    @Transactional(rollbackFor = EntityNotFoundException.class)
    public void saveAmkaFile(UUID uuid, MultipartFile amkaFile)
            throws FileUploadException, EntityNotFoundException {
        try {
            String originalFilename = amkaFile.getOriginalFilename();
            String savedName = UUID.randomUUID() + getFileExtension(originalFilename);

            String uploadDirectory = uploadDir;
            Path filePath = Paths.get(uploadDirectory + savedName);

            Files.createDirectories(filePath.getParent());
    //        Files.write(filePath, amkaFile.getBytes());
            amkaFile.transferTo(filePath);  // safe for large files, more efficient

            Attachment attachment = new Attachment();
            attachment.setFilename(originalFilename);
            attachment.setSavedName(savedName);
            attachment.setFilePath(filePath.toString());

            Tika tika = new Tika();
            String contentType = tika.detect(amkaFile.getBytes());
//            attachment.setContentType(amkaFile.getContentType());
            attachment.setContentType(contentType);
            attachment.setExtension(getFileExtension(originalFilename));

            Teacher teacher = teacherRepository.findByUuid(uuid).orElseThrow(()
                    -> new EntityNotFoundException("Teacher", "Teacher with uuid=" + uuid + " not found."));

            PersonalInfo personalInfo = teacher.getPersonalInfo();

            if (personalInfo.getAmkaFile() != null) {
                Files.deleteIfExists(Path.of(personalInfo.getAmkaFile().getFilePath()));
                personalInfo.removeAmkaFile();  // orphanRemoval handles DB deletion
            }

            personalInfo.addAmkaFile(attachment);
            teacherRepository.save(teacher);
            log.info("Attachment for teacher with amka={} saved", personalInfo.getAmka());
        } catch (EntityNotFoundException e) {
            log.error("Attachment for teacher with amka={} not found", uuid, e);
            throw e;
        } catch (IOException | HttpServerErrorException e) {
            log.error("Error saving attachment for teacher with amka={}", uuid, e);
            throw new FileUploadException("TeacherAmka","Error saving attachment for teacher with amka=" + uuid);
        }
    }



    @Override
    @PreAuthorize("hasAuthority('VIEW_TEACHERS')")
    @Transactional(readOnly = true)
    public Page<TeacherReadOnlyDTO> getTeachersPaginatedFiltered(Pageable pageable, TeacherFilters filters)
            throws EntityNotFoundException {
        try {
            if (filters.getUuid() != null) {
                Teacher teacher = teacherRepository.findByUuid(filters.getUuid())
                        .orElseThrow(() -> new EntityNotFoundException("Teacher", "uuid=" + filters.getUuid() + " not found"));
                return singleResultPage(teacher, pageable);
            }

            if (filters.getVat() != null) {
                Teacher teacher = teacherRepository.findByVat(filters.getVat())
                        .orElseThrow(() -> new EntityNotFoundException("Teacher", "vat=" + filters.getVat() + " not found"));
                return singleResultPage(teacher, pageable);
            }

            if (filters.getAmka() != null) {
                Teacher teacher = teacherRepository.findByPersonalInfo_Amka(filters.getAmka())
                        .orElseThrow(() -> new EntityNotFoundException("Teacher", "amka=" + filters.getAmka() + " not found"));
                return singleResultPage(teacher, pageable);
            }

            var filtered = teacherRepository.findAll(TeacherSpecification.build(filters), pageable);

            log.debug("Filtered and paginated teachers were returned successfully with page={} and size={}", pageable.getPageNumber(),
                    pageable.getPageSize());
            return filtered.map(mapper::mapToTeacherReadOnlyDTO);
        } catch (EntityNotFoundException e) {
            log.error("Filtered and paginated teachers were not found", e);
            throw e;
        }
    }

    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return "";
    }

    private Page<TeacherReadOnlyDTO> singleResultPage(Teacher teacher, Pageable pageable) {
        return new PageImpl<>(
                List.of(mapper.mapToTeacherReadOnlyDTO(teacher)),
                pageable,
                1
        );
    }
}
