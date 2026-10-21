package com.hiresphere.job.web;

import com.hiresphere.job.dto.CategoryResponse;
import com.hiresphere.job.dto.NamedResponse;
import com.hiresphere.job.service.MetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public metadata reads. */
@RestController
@RequiredArgsConstructor
public class MetadataController {

    private final MetadataService metadataService;

    @GetMapping("/api/job-categories")
    public List<CategoryResponse> categories() {
        return metadataService.listCategories();
    }

    @GetMapping("/api/skills")
    public List<NamedResponse> skills() {
        return metadataService.listSkills();
    }

    @GetMapping("/api/tags")
    public List<NamedResponse> tags() {
        return metadataService.listTags();
    }
}
