package gr.aueb.cf.eduapp.mapper;

import gr.aueb.cf.eduapp.dto.*;
import gr.aueb.cf.eduapp.model.PersonalInfo;
import gr.aueb.cf.eduapp.model.Teacher;
import gr.aueb.cf.eduapp.model.User;
import org.springframework.stereotype.Component;

@Component
public class Mapper {

    public User mapToUserEntity(UserInsertDTO insertDTO) {
        return new User(insertDTO.username(), insertDTO.password());
    }

    public UserReadOnlyDTO mapToUserReadOnlyDTO(User user) {
        return new UserReadOnlyDTO(user.getUuid().toString(), user.getUsername(), user.getRole().getName());
    }

    public TeacherReadOnlyDTO mapToTeacherReadOnlyDTO(Teacher teacher) {
        return new TeacherReadOnlyDTO(teacher.getUuid().toString(), teacher.getFirstname(), teacher.getLastname(),
                teacher.getVat(), teacher.getRegion().getName());
    }

    public Teacher mapToTeacherEntity(TeacherInsertDTO dto) {
        Teacher teacher = new Teacher();
        teacher.setFirstname(dto.firstname());
        teacher.setLastname(dto.lastname());
        teacher.setVat(dto.vat());

        UserInsertDTO userDTO = dto.userInsertDTO();
        User user = new User();
        user.setUsername(userDTO.username());
        user.setPassword(userDTO.password());

        teacher.addUser(user);

        PersonalInfoInsertDTO personalInfoDTO = dto.personalInfoInsertDTO();
        PersonalInfo personalInfo = new PersonalInfo();
        personalInfo.setAmka(personalInfoDTO.amka());
        personalInfo.setIdentityNumber(personalInfoDTO.identityNumber());
        personalInfo.setPlaceOfBirth(personalInfoDTO.placeOfBirth());
        personalInfo.setMunicipalityOfRegistration(personalInfoDTO
                .municipalityOfRegistration());
        teacher.setPersonalInfo(personalInfo);  // Set PersonalInfo entity to Teacher

        return teacher;
    }

}
