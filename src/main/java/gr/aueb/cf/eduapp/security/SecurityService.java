package gr.aueb.cf.eduapp.security;

import gr.aueb.cf.eduapp.model.User;
import gr.aueb.cf.eduapp.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.nio.file.attribute.UserPrincipal;
import java.util.UUID;

@Service("securityService")
public class SecurityService {

    @Autowired
    private TeacherRepository teacherRepository;

    public boolean isOwnTeacherProfile(UUID teacherUuid, Authentication authentication) {
        User principal = (User) authentication.getPrincipal();
        // Find the teacher record and check if its user uuid matches the logged-in user
        return teacherRepository.existsByUuidAndUser_Uuid(teacherUuid, principal.getUuid());
    }
}
