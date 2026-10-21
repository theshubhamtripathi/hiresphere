package com.hiresphere.job.web;

import com.hiresphere.common.dto.ApiResponse;
import com.hiresphere.common.exception.ForbiddenException;
import com.hiresphere.common.security.UserContextHolder;
import com.hiresphere.job.dto.CategoryRequest;
import com.hiresphere.job.dto.CategoryResponse;
import com.hiresphere.job.dto.NameRequest;
import com.hiresphere.job.dto.NamedResponse;
import com.hiresphere.job.service.MetadataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminMetadataController {

    private final MetadataService metadataService;

    @PostMapping("/job-categories")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        requireAdmin();
        return created(ApiResponse.ok("Category created", metadataService.createCategory(request)));
    }

    @DeleteMapping("/job-categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        requireAdmin();
        metadataService.deleteCategory(id);
        return ApiResponse.message("Category deleted");
    }

    @PostMapping("/skills")
    public ResponseEntity<ApiResponse<NamedResponse>> createSkill(@Valid @RequestBody NameRequest request) {
        requireAdmin();
        return created(ApiResponse.ok("Skill created", metadataService.createSkill(request)));
    }

    @DeleteMapping("/skills/{id}")
    public ApiResponse<Void> deleteSkill(@PathVariable Long id) {
        requireAdmin();
        metadataService.deleteSkill(id);
        return ApiResponse.message("Skill deleted");
    }

    @PostMapping("/tags")
    public ResponseEntity<ApiResponse<NamedResponse>> createTag(@Valid @RequestBody NameRequest request) {
        requireAdmin();
        return created(ApiResponse.ok("Tag created", metadataService.createTag(request)));
    }

    @DeleteMapping("/tags/{id}")
    public ApiResponse<Void> deleteTag(@PathVariable Long id) {
        requireAdmin();
        metadataService.deleteTag(id);
        return ApiResponse.message("Tag deleted");
    }

    private <T> ResponseEntity<ApiResponse<T>> created(ApiResponse<T> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    private void requireAdmin() {
        if (!UserContextHolder.require().isAdmin()) {
            throw new ForbiddenException("Administrator access required");
        }
    }
}
