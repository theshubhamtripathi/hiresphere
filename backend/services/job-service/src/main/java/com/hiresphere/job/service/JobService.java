package com.hiresphere.job.service;

import com.hiresphere.common.exception.ForbiddenException;
import com.hiresphere.common.exception.ResourceNotFoundException;
import com.hiresphere.job.client.CompanyClient;
import com.hiresphere.job.config.CacheConfig;
import com.hiresphere.job.domain.Job;
import com.hiresphere.job.domain.JobStatus;
import com.hiresphere.job.domain.Skill;
import com.hiresphere.job.domain.Tag;
import com.hiresphere.job.dto.CreateJobRequest;
import com.hiresphere.job.dto.JobResponse;
import com.hiresphere.job.dto.JobSearchCriteria;
import com.hiresphere.job.dto.JobSummaryResponse;
import com.hiresphere.job.dto.UpdateJobRequest;
import com.hiresphere.job.mapper.JobMapper;
import com.hiresphere.job.repository.JobRepository;
import com.hiresphere.job.repository.SkillRepository;
import com.hiresphere.job.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;
    private final TagRepository tagRepository;
    private final JobMapper jobMapper;
    private final CompanyClient companyClient;
    private final JobEventPublisher eventPublisher;

    @Transactional
    public JobResponse create(Long ownerUserId, CreateJobRequest req) {
        Long companyId = companyClient.getByOwner(ownerUserId).id();
        Job job = new Job();
        job.setCompanyId(companyId);
        job.setPostedBy(ownerUserId);
        applyCreate(job, req);
        job.setStatus(Boolean.TRUE.equals(req.publish()) ? JobStatus.OPEN : JobStatus.DRAFT);
        job.setSkills(resolveSkills(req.skills()));
        job.setTags(resolveTags(req.tags()));
        job = jobRepository.save(job);
        if (job.getStatus() == JobStatus.OPEN) {
            eventPublisher.publishJobPosted(job);
        }
        return jobMapper.toResponse(job);
    }

    @CacheEvict(value = CacheConfig.JOB_DETAILS, key = "#id")
    @Transactional
    public JobResponse update(Long id, Long ownerUserId, UpdateJobRequest req) {
        Job job = requireOwned(id, ownerUserId);
        if (StringUtils.hasText(req.title())) job.setTitle(req.title());
        if (StringUtils.hasText(req.description())) job.setDescription(req.description());
        job.setResponsibilities(req.responsibilities());
        job.setRequirements(req.requirements());
        job.setBenefits(req.benefits());
        if (req.categoryId() != null) job.setCategoryId(req.categoryId());
        if (req.jobType() != null) job.setJobType(req.jobType());
        if (req.workMode() != null) job.setWorkMode(req.workMode());
        if (req.experienceLevel() != null) job.setExperienceLevel(req.experienceLevel());
        job.setMinExperience(req.minExperience());
        job.setMaxExperience(req.maxExperience());
        job.setSalaryMin(req.salaryMin());
        job.setSalaryMax(req.salaryMax());
        if (StringUtils.hasText(req.currency())) job.setCurrency(req.currency());
        job.setLocation(req.location());
        if (req.openings() != null) job.setOpenings(req.openings());
        job.setDeadline(req.deadline());
        if (req.skills() != null) job.setSkills(resolveSkills(req.skills()));
        if (req.tags() != null) job.setTags(resolveTags(req.tags()));
        return jobMapper.toResponse(job);
    }

    @CacheEvict(value = CacheConfig.JOB_DETAILS, key = "#id")
    @Transactional
    public void delete(Long id, Long ownerUserId) {
        Job job = requireOwned(id, ownerUserId);
        jobRepository.delete(job);
    }

    @CacheEvict(value = CacheConfig.JOB_DETAILS, key = "#id")
    @Transactional
    public JobResponse changeStatus(Long id, Long ownerUserId, JobStatus status) {
        Job job = requireOwned(id, ownerUserId);
        JobStatus previous = job.getStatus();
        job.setStatus(status);
        if (status == JobStatus.OPEN && previous != JobStatus.OPEN) {
            eventPublisher.publishJobPosted(job);
        }
        return jobMapper.toResponse(job);
    }

    @Transactional
    public void recordView(Long id) {
        jobRepository.incrementViews(id);
    }

    @Cacheable(value = CacheConfig.JOB_DETAILS, key = "#id")
    @Transactional(readOnly = true)
    public JobResponse getById(Long id) {
        return jobMapper.toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public Page<JobSummaryResponse> search(JobSearchCriteria criteria, Pageable pageable) {
        return jobRepository.findAll(JobSpecifications.from(criteria), pageable).map(jobMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public Page<JobSummaryResponse> getCompanyJobs(Long ownerUserId, Pageable pageable) {
        Long companyId = companyClient.getByOwner(ownerUserId).id();
        return jobRepository.findByCompanyId(companyId, pageable).map(jobMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public List<JobSummaryResponse> similar(Long id) {
        Job job = find(id);
        if (job.getCategoryId() == null) {
            return List.of();
        }
        return jobRepository
                .findTop6ByCategoryIdAndStatusAndIdNot(job.getCategoryId(), JobStatus.OPEN, id)
                .stream().map(jobMapper::toSummary).toList();
    }

    @Transactional
    public void adjustApplicationsCount(Long id, long delta) {
        jobRepository.adjustApplicationsCount(id, delta);
    }

    @Transactional(readOnly = true)
    public List<JobSummaryResponse> getByIds(List<Long> ids) {
        return jobRepository.findAllById(ids).stream().map(jobMapper::toSummary).toList();
    }

    private void applyCreate(Job job, CreateJobRequest req) {
        job.setTitle(req.title());
        job.setDescription(req.description());
        job.setResponsibilities(req.responsibilities());
        job.setRequirements(req.requirements());
        job.setBenefits(req.benefits());
        job.setCategoryId(req.categoryId());
        job.setJobType(req.jobType());
        job.setWorkMode(req.workMode());
        job.setExperienceLevel(req.experienceLevel());
        job.setMinExperience(req.minExperience());
        job.setMaxExperience(req.maxExperience());
        job.setSalaryMin(req.salaryMin());
        job.setSalaryMax(req.salaryMax());
        if (StringUtils.hasText(req.currency())) job.setCurrency(req.currency());
        job.setLocation(req.location());
        if (req.openings() != null) job.setOpenings(req.openings());
        job.setDeadline(req.deadline());
    }

    private Set<Skill> resolveSkills(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return new HashSet<>();
        }
        Set<Skill> result = new HashSet<>(skillRepository.findByNameInIgnoreCase(names));
        Set<String> existing = result.stream().map(s -> s.getName().toLowerCase()).collect(java.util.stream.Collectors.toSet());
        for (String name : names) {
            String trimmed = name.trim();
            if (!trimmed.isEmpty() && !existing.contains(trimmed.toLowerCase())) {
                Skill skill = new Skill();
                skill.setName(trimmed);
                result.add(skillRepository.save(skill));
                existing.add(trimmed.toLowerCase());
            }
        }
        return result;
    }

    private Set<Tag> resolveTags(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return new HashSet<>();
        }
        Set<Tag> result = new HashSet<>(tagRepository.findByNameInIgnoreCase(names));
        Set<String> existing = result.stream().map(t -> t.getName().toLowerCase()).collect(java.util.stream.Collectors.toSet());
        for (String name : names) {
            String trimmed = name.trim();
            if (!trimmed.isEmpty() && !existing.contains(trimmed.toLowerCase())) {
                Tag tag = new Tag();
                tag.setName(trimmed);
                result.add(tagRepository.save(tag));
                existing.add(trimmed.toLowerCase());
            }
        }
        return result;
    }

    private Job find(Long id) {
        return jobRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Job", id));
    }

    private Job requireOwned(Long id, Long ownerUserId) {
        Job job = find(id);
        if (!job.getPostedBy().equals(ownerUserId)) {
            throw new ForbiddenException("You can only manage jobs you posted");
        }
        return job;
    }
}
