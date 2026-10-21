package com.hiresphere.company.dto;

import com.hiresphere.company.domain.CompanySize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CreateCompanyRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 255) String website,
        @Size(max = 100) String industry,
        CompanySize size,
        Integer foundedYear,
        @Size(max = 5000) String description,
        @Size(max = 150) String headquarters,
        @Size(max = 512) String logoUrl,
        Map<String, String> socialLinks) {
}
