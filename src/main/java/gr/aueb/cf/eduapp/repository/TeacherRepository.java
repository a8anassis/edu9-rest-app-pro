package gr.aueb.cf.eduapp.repository;

import gr.aueb.cf.eduapp.model.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long>,
        JpaSpecificationExecutor<Teacher> {

    Optional<Teacher> findByUuid(UUID uuid);
    Optional<Teacher> findByVat(String vat);
    Optional<Teacher> findByPersonalInfo_Amka(String amka);

    @EntityGraph(attributePaths = {"personalInfo", "region"})
    Page<Teacher> findAllByDeletedFalse(Pageable pageable);

    Optional<Teacher> findByUuidAndDeletedFalse(UUID uuid);


    boolean existsByUuidAndUser_Uuid(UUID teacherUuid, UUID userUuid);
}
