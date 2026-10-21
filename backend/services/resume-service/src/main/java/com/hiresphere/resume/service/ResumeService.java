package com.hiresphere.resume.service;

import com.hiresphere.common.exception.ResourceNotFoundException;
import com.hiresphere.resume.domain.Award;
import com.hiresphere.resume.domain.Certification;
import com.hiresphere.resume.domain.Education;
import com.hiresphere.resume.domain.Language;
import com.hiresphere.resume.domain.Project;
import com.hiresphere.resume.domain.Resume;
import com.hiresphere.resume.domain.ResumeSkill;
import com.hiresphere.resume.domain.SkillLevel;
import com.hiresphere.resume.domain.LanguageProficiency;
import com.hiresphere.resume.domain.WorkExperience;
import com.hiresphere.resume.dto.ResumeRequests;
import com.hiresphere.resume.dto.ResumeResponses;
import com.hiresphere.resume.mapper.ResumeMapper;
import com.hiresphere.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeMapper mapper;

    // ---- Resume lifecycle ----

    @Transactional
    public ResumeResponses.ResumeDetail create(Long userId, ResumeRequests.CreateResume req) {
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setTitle(req.title());
        if (StringUtils.hasText(req.template())) {
            resume.setTemplate(req.template());
        }
        resume.setDefault(!resumeRepository.existsByUserId(userId)); // first resume is default
        return save(resume);
    }

    @Transactional(readOnly = true)
    public List<ResumeResponses.ResumeSummary> list(Long userId) {
        return resumeRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(mapper::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public ResumeResponses.ResumeDetail getDetail(Long id, Long userId) {
        return mapper.toDetail(require(id, userId));
    }

    @Transactional(readOnly = true)
    public ResumeResponses.ResumeDetail getFull(Long id) {
        return mapper.toDetail(resumeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Resume", id)));
    }

    @Transactional
    public ResumeResponses.ResumeDetail updateMeta(Long id, Long userId, ResumeRequests.CreateResume req) {
        Resume resume = require(id, userId);
        resume.setTitle(req.title());
        if (StringUtils.hasText(req.template())) {
            resume.setTemplate(req.template());
        }
        return save(resume);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Resume resume = require(id, userId);
        boolean wasDefault = resume.isDefault();
        resumeRepository.delete(resume);
        if (wasDefault) {
            resumeRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream().findFirst()
                    .ifPresent(next -> {
                        next.setDefault(true);
                        resumeRepository.save(next);
                    });
        }
    }

    @Transactional
    public ResumeResponses.ResumeDetail setDefault(Long id, Long userId) {
        Resume resume = require(id, userId);
        resumeRepository.findByUserIdAndIsDefaultTrue(userId).ifPresent(current -> {
            if (!current.getId().equals(id)) {
                current.setDefault(false);
            }
        });
        resume.setDefault(true);
        return mapper.toDetail(resume);
    }

    @Transactional
    public ResumeResponses.ResumeDetail updatePersonalInfo(Long id, Long userId, ResumeRequests.PersonalInfo p) {
        Resume r = require(id, userId);
        r.setFullName(p.fullName());
        r.setEmail(p.email());
        r.setPhone(p.phone());
        r.setLocation(p.location());
        r.setHeadline(p.headline());
        r.setWebsite(p.website());
        r.setLinkedin(p.linkedin());
        r.setGithub(p.github());
        return save(r);
    }

    @Transactional
    public ResumeResponses.ResumeDetail updateSummary(Long id, Long userId, ResumeRequests.Summary s) {
        Resume r = require(id, userId);
        r.setSummary(s.summary());
        return save(r);
    }

    // ---- Work experience ----

    @Transactional
    public ResumeResponses.ResumeDetail addExperience(Long id, Long userId, ResumeRequests.Experience req) {
        Resume r = require(id, userId);
        WorkExperience e = new WorkExperience();
        e.setResume(r);
        applyExperience(e, req);
        r.getExperiences().add(e);
        return save(r);
    }

    @Transactional
    public ResumeResponses.ResumeDetail updateExperience(Long id, Long userId, Long sectionId, ResumeRequests.Experience req) {
        Resume r = require(id, userId);
        applyExperience(findChild(r.getExperiences(), sectionId, "Experience"), req);
        return save(r);
    }

    @Transactional
    public ResumeResponses.ResumeDetail deleteExperience(Long id, Long userId, Long sectionId) {
        return removeChild(require(id, userId), rr -> rr.getExperiences().removeIf(matches(sectionId)));
    }

    private void applyExperience(WorkExperience e, ResumeRequests.Experience req) {
        e.setCompany(req.company());
        e.setTitle(req.title());
        e.setLocation(req.location());
        e.setStartDate(req.startDate());
        e.setEndDate(req.endDate());
        e.setCurrent(req.current());
        e.setDescription(req.description());
        e.setDisplayOrder(order(req.displayOrder()));
    }

    // ---- Education ----

    @Transactional
    public ResumeResponses.ResumeDetail addEducation(Long id, Long userId, ResumeRequests.Education req) {
        Resume r = require(id, userId);
        Education e = new Education();
        e.setResume(r);
        applyEducation(e, req);
        r.getEducation().add(e);
        return save(r);
    }

    @Transactional
    public ResumeResponses.ResumeDetail updateEducation(Long id, Long userId, Long sectionId, ResumeRequests.Education req) {
        Resume r = require(id, userId);
        applyEducation(findChild(r.getEducation(), sectionId, "Education"), req);
        return save(r);
    }

    @Transactional
    public ResumeResponses.ResumeDetail deleteEducation(Long id, Long userId, Long sectionId) {
        return removeChild(require(id, userId), rr -> rr.getEducation().removeIf(matches(sectionId)));
    }

    private void applyEducation(Education e, ResumeRequests.Education req) {
        e.setInstitution(req.institution());
        e.setDegree(req.degree());
        e.setFieldOfStudy(req.fieldOfStudy());
        e.setStartDate(req.startDate());
        e.setEndDate(req.endDate());
        e.setGrade(req.grade());
        e.setDescription(req.description());
        e.setDisplayOrder(order(req.displayOrder()));
    }

    // ---- Skills ----

    @Transactional
    public ResumeResponses.ResumeDetail addSkill(Long id, Long userId, ResumeRequests.Skill req) {
        Resume r = require(id, userId);
        ResumeSkill s = new ResumeSkill();
        s.setResume(r);
        applySkill(s, req);
        r.getSkills().add(s);
        return save(r);
    }

    @Transactional
    public ResumeResponses.ResumeDetail updateSkill(Long id, Long userId, Long sectionId, ResumeRequests.Skill req) {
        Resume r = require(id, userId);
        applySkill(findChild(r.getSkills(), sectionId, "Skill"), req);
        return save(r);
    }

    @Transactional
    public ResumeResponses.ResumeDetail deleteSkill(Long id, Long userId, Long sectionId) {
        return removeChild(require(id, userId), rr -> rr.getSkills().removeIf(matches(sectionId)));
    }

    private void applySkill(ResumeSkill s, ResumeRequests.Skill req) {
        s.setName(req.name());
        s.setLevel(req.level() == null ? SkillLevel.INTERMEDIATE : req.level());
        s.setDisplayOrder(order(req.displayOrder()));
    }

    // ---- Projects ----

    @Transactional
    public ResumeResponses.ResumeDetail addProject(Long id, Long userId, ResumeRequests.Project req) {
        Resume r = require(id, userId);
        Project p = new Project();
        p.setResume(r);
        applyProject(p, req);
        r.getProjects().add(p);
        return save(r);
    }

    @Transactional
    public ResumeResponses.ResumeDetail updateProject(Long id, Long userId, Long sectionId, ResumeRequests.Project req) {
        Resume r = require(id, userId);
        applyProject(findChild(r.getProjects(), sectionId, "Project"), req);
        return save(r);
    }

    @Transactional
    public ResumeResponses.ResumeDetail deleteProject(Long id, Long userId, Long sectionId) {
        return removeChild(require(id, userId), rr -> rr.getProjects().removeIf(matches(sectionId)));
    }

    private void applyProject(Project p, ResumeRequests.Project req) {
        p.setName(req.name());
        p.setDescription(req.description());
        p.setUrl(req.url());
        p.setStartDate(req.startDate());
        p.setEndDate(req.endDate());
        p.setDisplayOrder(order(req.displayOrder()));
    }

    // ---- Certifications ----

    @Transactional
    public ResumeResponses.ResumeDetail addCertification(Long id, Long userId, ResumeRequests.Certification req) {
        Resume r = require(id, userId);
        Certification c = new Certification();
        c.setResume(r);
        applyCertification(c, req);
        r.getCertifications().add(c);
        return save(r);
    }

    @Transactional
    public ResumeResponses.ResumeDetail updateCertification(Long id, Long userId, Long sectionId, ResumeRequests.Certification req) {
        Resume r = require(id, userId);
        applyCertification(findChild(r.getCertifications(), sectionId, "Certification"), req);
        return save(r);
    }

    @Transactional
    public ResumeResponses.ResumeDetail deleteCertification(Long id, Long userId, Long sectionId) {
        return removeChild(require(id, userId), rr -> rr.getCertifications().removeIf(matches(sectionId)));
    }

    private void applyCertification(Certification c, ResumeRequests.Certification req) {
        c.setName(req.name());
        c.setIssuer(req.issuer());
        c.setIssueDate(req.issueDate());
        c.setExpiryDate(req.expiryDate());
        c.setCredentialId(req.credentialId());
        c.setUrl(req.url());
        c.setDisplayOrder(order(req.displayOrder()));
    }

    // ---- Awards ----

    @Transactional
    public ResumeResponses.ResumeDetail addAward(Long id, Long userId, ResumeRequests.Award req) {
        Resume r = require(id, userId);
        Award a = new Award();
        a.setResume(r);
        applyAward(a, req);
        r.getAwards().add(a);
        return save(r);
    }

    @Transactional
    public ResumeResponses.ResumeDetail updateAward(Long id, Long userId, Long sectionId, ResumeRequests.Award req) {
        Resume r = require(id, userId);
        applyAward(findChild(r.getAwards(), sectionId, "Award"), req);
        return save(r);
    }

    @Transactional
    public ResumeResponses.ResumeDetail deleteAward(Long id, Long userId, Long sectionId) {
        return removeChild(require(id, userId), rr -> rr.getAwards().removeIf(matches(sectionId)));
    }

    private void applyAward(Award a, ResumeRequests.Award req) {
        a.setTitle(req.title());
        a.setIssuer(req.issuer());
        a.setDate(req.date());
        a.setDescription(req.description());
        a.setDisplayOrder(order(req.displayOrder()));
    }

    // ---- Languages ----

    @Transactional
    public ResumeResponses.ResumeDetail addLanguage(Long id, Long userId, ResumeRequests.Language req) {
        Resume r = require(id, userId);
        Language l = new Language();
        l.setResume(r);
        applyLanguage(l, req);
        r.getLanguages().add(l);
        return save(r);
    }

    @Transactional
    public ResumeResponses.ResumeDetail updateLanguage(Long id, Long userId, Long sectionId, ResumeRequests.Language req) {
        Resume r = require(id, userId);
        applyLanguage(findChild(r.getLanguages(), sectionId, "Language"), req);
        return save(r);
    }

    @Transactional
    public ResumeResponses.ResumeDetail deleteLanguage(Long id, Long userId, Long sectionId) {
        return removeChild(require(id, userId), rr -> rr.getLanguages().removeIf(matches(sectionId)));
    }

    private void applyLanguage(Language l, ResumeRequests.Language req) {
        l.setName(req.name());
        l.setProficiency(req.proficiency() == null ? LanguageProficiency.PROFESSIONAL : req.proficiency());
        l.setDisplayOrder(order(req.displayOrder()));
    }

    // ---- Helpers ----

    private ResumeResponses.ResumeDetail save(Resume resume) {
        resume.setCompletionScore(CompletionScore.calculate(resume));
        return mapper.toDetail(resumeRepository.save(resume));
    }

    private ResumeResponses.ResumeDetail removeChild(Resume resume, Predicate<Resume> removal) {
        if (!removal.test(resume)) {
            throw new ResourceNotFoundException("Section not found");
        }
        return save(resume);
    }

    private Resume require(Long id, Long userId) {
        return resumeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Resume", id));
    }

    private <T extends com.hiresphere.common.domain.BaseEntity> T findChild(List<T> children, Long sectionId, String label) {
        Optional<T> match = children.stream().filter(c -> c.getId().equals(sectionId)).findFirst();
        return match.orElseThrow(() -> ResourceNotFoundException.of(label, sectionId));
    }

    private Predicate<com.hiresphere.common.domain.BaseEntity> matches(Long sectionId) {
        return c -> c.getId().equals(sectionId);
    }

    private int order(Integer displayOrder) {
        return displayOrder == null ? 0 : displayOrder;
    }
}
