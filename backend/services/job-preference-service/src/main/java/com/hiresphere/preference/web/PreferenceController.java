package com.hiresphere.preference.web;

import com.hiresphere.common.dto.ApiResponse;
import com.hiresphere.common.security.UserContextHolder;
import com.hiresphere.preference.dto.PreferenceDtos.PreferenceRequest;
import com.hiresphere.preference.dto.PreferenceDtos.PreferenceResponse;
import com.hiresphere.preference.service.PreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final PreferenceService preferenceService;

    @GetMapping
    public ApiResponse<PreferenceResponse> get() {
        return ApiResponse.ok(preferenceService.get(UserContextHolder.currentUserId()));
    }

    @PutMapping
    public ApiResponse<PreferenceResponse> update(@Valid @RequestBody PreferenceRequest request) {
        return ApiResponse.ok(preferenceService.update(UserContextHolder.currentUserId(), request));
    }
}
