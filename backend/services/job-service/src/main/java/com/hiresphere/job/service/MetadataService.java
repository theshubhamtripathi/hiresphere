package com.hiresphere.job.service;

import com.hiresphere.common.exception.ConflictException;
import com.hiresphere.common.exception.ResourceNotFoundException;
import com.hiresphere.job.config.CacheConfig;
import com.hiresphere.job.domain.JobCategory;
import com.hiresphere.job.domain.Skill;
import com.hiresphere.job.domain.Tag;
import com.hiresphere.job.dto.CategoryRequest;
import com.hiresphere.job.dto.CategoryResponse;
import com.hiresphere.job.dto.NameRequest;
import com.hiresphere.job.dto.NamedResponse;
import com.hiresphere.job.repository.JobCategoryRepository;
import com.hiresphere.job.repository.SkillRepository;
import com.hiresphere.job.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MetadataService {

    private final JobCategoryRepository categoryRepository;
    private final SkillRepository skillRepository;
    private final TagRepository tagRepository;

    // ---- Categories ----

    @Cacheable(CacheConfig.JOB_CATEGORIES)
    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getSlug(), c.getIcon(), c.getDescription()))
                .toList();
    }

    @CacheEvict(value = CacheConfig.JOB_CATEGORIES, allEntries = true)
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String slug = slugify(request.name());
        if (categoryRepository.existsBySlug(slug)) {
            throw new ConflictException("Category already exists");
        }
        JobCategory category = new JobCategory();
        category.setName(request.name());
        category.setSlug(slug);
        category.setIcon(request.icon());
        category.setDescription(request.description());
        category = categoryRepository.save(category);
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug(),
                category.getIcon(), category.getDescription());
    }

    @CacheEvict(value = CacheConfig.JOB_CATEGORIES, allEntries = true)
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Category", id);
        }
        categoryRepository.deleteById(id);
    }

    // ---- Skills ----

    @Cacheable(CacheConfig.SKILLS)
    @Transactional(readOnly = true)
    public List<NamedResponse> listSkills() {
        return skillRepository.findAllByOrderByNameAsc().stream()
                .map(s -> new NamedResponse(s.getId(), s.getName())).toList();
    }

    @CacheEvict(value = CacheConfig.SKILLS, allEntries = true)
    @Transactional
    public NamedResponse createSkill(NameRequest request) {
        if (skillRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Skill already exists");
        }
        Skill skill = new Skill();
        skill.setName(request.name());
        skill = skillRepository.save(skill);
        return new NamedResponse(skill.getId(), skill.getName());
    }

    @CacheEvict(value = CacheConfig.SKILLS, allEntries = true)
    @Transactional
    public void deleteSkill(Long id) {
        if (!skillRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Skill", id);
        }
        skillRepository.deleteById(id);
    }

    // ---- Tags ----

    @Cacheable(CacheConfig.TAGS)
    @Transactional(readOnly = true)
    public List<NamedResponse> listTags() {
        return tagRepository.findAllByOrderByNameAsc().stream()
                .map(t -> new NamedResponse(t.getId(), t.getName())).toList();
    }

    @CacheEvict(value = CacheConfig.TAGS, allEntries = true)
    @Transactional
    public NamedResponse createTag(NameRequest request) {
        if (tagRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Tag already exists");
        }
        Tag tag = new Tag();
        tag.setName(request.name());
        tag = tagRepository.save(tag);
        return new NamedResponse(tag.getId(), tag.getName());
    }

    @CacheEvict(value = CacheConfig.TAGS, allEntries = true)
    @Transactional
    public void deleteTag(Long id) {
        if (!tagRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Tag", id);
        }
        tagRepository.deleteById(id);
    }

    private String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
