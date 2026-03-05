package gr.aueb.cf.eduapp.repository;

import gr.aueb.cf.eduapp.dto.TeacherStatusReportView;
import gr.aueb.cf.eduapp.model.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query(value = """
        SELECT
            r.name AS periochi,
            t.firstname AS onoma,
            t.lastname AS eponymo,
            pi.amka AS amka,
            t.vat AS afm,
            CASE WHEN t.deleted = 1 THEN 'ΔΙΕΓΡΑΜΜΕΝΟΣ' ELSE 'ΕΝΕΡΓΟΣ' END AS katastasi,
            CASE 
                WHEN t.created_at > '2025-01-01' THEN 'ΝΕΟΣ'
                WHEN t.created_at > '2023-01-01' THEN 'ΜΕΣΑΙΟΣ'
                WHEN t.created_at > '2020-01-01' THEN 'ΕΜΠΕΙΡΟΣ'
                ELSE 'ΠΑΛΙΟΣ'
            END AS empeiria
        FROM teachers t
        JOIN personal_information pi ON t.personal_info_id = pi.id
        JOIN regions r ON t.region_id = r.id
        WHERE t.deleted = 0
        ORDER BY t.deleted DESC, r.name
        """, nativeQuery = true)
    List<TeacherStatusReportView> findAllTeachersReport();
}
