package com.hiresphere.resume.web;

import com.hiresphere.resume.dto.ResumeResponses;
import com.hiresphere.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Full-resume fetch for other services (e.g. application snapshotting). */
@RestController
@RequestMapping("/api/internal/resumes")
@RequiredArgsConstructor
public class InternalResumeController {

    private final ResumeService resumeService;

    @GetMapping("/{id}")
    public ResumeResponses.ResumeDetail getFull(@PathVariable Long id) {
        return resumeService.getFull(id);
    }
}
