package com.hiresphere.job.service;

import com.hiresphere.job.domain.Job;
import com.hiresphere.job.domain.Skill;
import com.hiresphere.job.domain.Tag;
import com.hiresphere.job.dto.JobSearchCriteria;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** Builds a dynamic {@link Specification} from the optional search filters. */
public final class JobSpecifications {

    private JobSpecifications() {
    }

    public static Specification<Job> from(JobSearchCriteria c) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (c.status() != null) {
                predicates.add(cb.equal(root.get("status"), c.status()));
            }
            if (StringUtils.hasText(c.q())) {
                String like = "%" + c.q().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)));
            }
            if (c.categoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), c.categoryId()));
            }
            if (c.companyId() != null) {
                predicates.add(cb.equal(root.get("companyId"), c.companyId()));
            }
            if (StringUtils.hasText(c.location())) {
                predicates.add(cb.like(cb.lower(root.get("location")),
                        "%" + c.location().trim().toLowerCase() + "%"));
            }
            if (c.jobType() != null) {
                predicates.add(cb.equal(root.get("jobType"), c.jobType()));
            }
            if (c.workMode() != null) {
                predicates.add(cb.equal(root.get("workMode"), c.workMode()));
            }
            if (c.experienceLevel() != null) {
                predicates.add(cb.equal(root.get("experienceLevel"), c.experienceLevel()));
            }
            if (c.salaryMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salaryMax"), c.salaryMin()));
            }
            if (c.salaryMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salaryMin"), c.salaryMax()));
            }
            if (c.postedWithinDays() != null && c.postedWithinDays() > 0) {
                Instant since = Instant.now().minus(c.postedWithinDays(), ChronoUnit.DAYS);
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), since));
            }
            if (!CollectionUtils.isEmpty(c.skills())) {
                Join<Job, Skill> skillJoin = root.join("skills");
                predicates.add(cb.lower(skillJoin.get("name")).in(
                        c.skills().stream().map(String::toLowerCase).toList()));
                query.distinct(true);
            }
            if (!CollectionUtils.isEmpty(c.tags())) {
                Join<Job, Tag> tagJoin = root.join("tags");
                predicates.add(cb.lower(tagJoin.get("name")).in(
                        c.tags().stream().map(String::toLowerCase).toList()));
                query.distinct(true);
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
