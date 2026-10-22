package com.hiresphere.preference.web;

import com.hiresphere.common.dto.ApiResponse;
import com.hiresphere.common.security.UserContextHolder;
import com.hiresphere.preference.dto.PreferenceDtos.SavedJobResponse;
import com.hiresphere.preference.service.SavedJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/saved-jobs")
@RequiredArgsConstructor
public class SavedJobController {

    private final SavedJobService savedJobService;

    private Long uid() {
        return UserContextHolder.currentUserId();
    }

    @PostMapping("/{jobId}")
    public ApiResponse<Void> save(@PathVariable Long jobId) {
        savedJobService.save(uid(), jobId);
        return ApiResponse.message("Job saved");
    }

    @DeleteMapping("/{jobId}")
    public ApiResponse<Void> unsave(@PathVariable Long jobId) {
        savedJobService.unsave(uid(), jobId);
        return ApiResponse.message("Job removed from saved");
    }

    @GetMapping
    public ApiResponse<List<SavedJobResponse>> list() {
        return ApiResponse.ok(savedJobService.listSaved(uid()));
    }

    @GetMapping("/ids")
    public ApiResponse<List<Long>> ids() {
        return ApiResponse.ok(savedJobService.savedIds(uid()));
    }
}
