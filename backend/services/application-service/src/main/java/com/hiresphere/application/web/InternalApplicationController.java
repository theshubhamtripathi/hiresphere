package com.hiresphere.application.web;

import com.hiresphere.application.dto.ApplicationRequests;
import com.hiresphere.application.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Called by ai-service to persist screening results. Not exposed via the gateway. */
@RestController
@RequestMapping("/api/internal/applications")
@RequiredArgsConstructor
public class InternalApplicationController {

    private final ApplicationService applicationService;

    @GetMapping("/{id}")
    public ApplicationService.InternalView get(@PathVariable Long id) {
        return applicationService.getInternal(id);
    }

    @PatchMapping("/{id}/ai-result")
    public void applyAiResult(@PathVariable Long id,
                              @RequestBody ApplicationRequests.AiScreeningResult result) {
        applicationService.applyAiResult(id, result);
    }
}
