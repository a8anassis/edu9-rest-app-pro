package gr.aueb.cf.eduapp.specification;

import gr.aueb.cf.eduapp.core.filters.TeacherFilters;
import gr.aueb.cf.eduapp.model.Teacher;
import org.springframework.data.jpa.domain.Specification;

public class TeacherSpecification {

    public static Specification<Teacher> build(TeacherFilters filters) {
        return Specification.allOf(
                hasLastname(filters.getLastname()),
                hasRegion(filters.getRegion()),
                isDeleted(filters.isDeleted())
        );
    }

    private static Specification<Teacher> hasLastname(String lastname) {
        return (root, query, cb) -> lastname == null ? cb.conjunction() :
                cb.like(cb.lower(root.get("lastname")), lastname.toLowerCase() + "%");
    }

    private static Specification<Teacher> hasRegion(String region) {
        return (root, query, cb) -> region == null ? cb.conjunction() :
                cb.equal(cb.lower(root.get("region").get("name")), region.toLowerCase());
    }

    private static Specification<Teacher> isDeleted(boolean deleted) {
        return (root, query, cb) -> cb.equal(root.get("deleted"), deleted);
    }
}
