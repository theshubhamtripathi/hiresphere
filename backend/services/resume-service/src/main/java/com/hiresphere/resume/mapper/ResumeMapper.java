package com.hiresphere.resume.mapper;

import com.hiresphere.resume.domain.Award;
import com.hiresphere.resume.domain.Certification;
import com.hiresphere.resume.domain.Education;
import com.hiresphere.resume.domain.Language;
import com.hiresphere.resume.domain.Project;
import com.hiresphere.resume.domain.Resume;
import com.hiresphere.resume.domain.ResumeSkill;
import com.hiresphere.resume.domain.WorkExperience;
import com.hiresphere.resume.dto.ResumeResponses;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResumeMapper {

    public ResumeResponses.ResumeSummary toSummary(Resume r) {
        return new ResumeResponses.ResumeSummary(r.getId(), r.getTitle(), r.isDefault(),
                r.getTemplate(), r.getCompletionScore(), r.getUpdatedAt());
    }

    public ResumeResponses.ResumeDetail toDetail(Resume r) {
        return new ResumeResponses.ResumeDetail(
                r.getId(), r.getTitle(), r.isDefault(), r.getTemplate(), r.getCompletionScore(),
                r.getFullName(), r.getEmail(), r.getPhone(), r.getLocation(), r.getHeadline(),
                r.getWebsite(), r.getLinkedin(), r.getGithub(), r.getSummary(),
                map(r.getExperiences(), this::experience),
                map(r.getEducation(), this::education),
                map(r.getSkills(), this::skill),
                map(r.getProjects(), this::project),
                map(r.getCertifications(), this::certification),
                map(r.getAwards(), this::award),
                map(r.getLanguages(), this::language),
                r.getCreatedAt(), r.getUpdatedAt());
    }

    private <E, D> List<D> map(List<E> source, java.util.function.Function<E, D> fn) {
        return source.stream().map(fn).toList();
    }

    private ResumeResponses.Experience experience(WorkExperience e) {
        return new ResumeResponses.Experience(e.getId(), e.getCompany(), e.getTitle(), e.getLocation(),
                e.getStartDate(), e.getEndDate(), e.isCurrent(), e.getDescription(), e.getDisplayOrder());
    }

    private ResumeResponses.Education education(Education e) {
        return new ResumeResponses.Education(e.getId(), e.getInstitution(), e.getDegree(),
                e.getFieldOfStudy(), e.getStartDate(), e.getEndDate(), e.getGrade(),
                e.getDescription(), e.getDisplayOrder());
    }

    private ResumeResponses.Skill skill(ResumeSkill s) {
        return new ResumeResponses.Skill(s.getId(), s.getName(), s.getLevel(), s.getDisplayOrder());
    }

    private ResumeResponses.Project project(Project p) {
        return new ResumeResponses.Project(p.getId(), p.getName(), p.getDescription(), p.getUrl(),
                p.getStartDate(), p.getEndDate(), p.getDisplayOrder());
    }

    private ResumeResponses.Certification certification(Certification c) {
        return new ResumeResponses.Certification(c.getId(), c.getName(), c.getIssuer(), c.getIssueDate(),
                c.getExpiryDate(), c.getCredentialId(), c.getUrl(), c.getDisplayOrder());
    }

    private ResumeResponses.Award award(Award a) {
        return new ResumeResponses.Award(a.getId(), a.getTitle(), a.getIssuer(), a.getDate(),
                a.getDescription(), a.getDisplayOrder());
    }

    private ResumeResponses.Language language(Language l) {
        return new ResumeResponses.Language(l.getId(), l.getName(), l.getProficiency(), l.getDisplayOrder());
    }
}
