package com.hiresphere.company.dto;

import com.hiresphere.company.domain.CompanySize;
import com.hiresphere.company.domain.CompanyStatus;

import java.time.Instant;
import java.util.Map;

public record CompanyResponse(
        Long id,
        String name,
        String slug,
        String logoUrl,
        String website,
        String industry,
        CompanySize size,
        Integer foundedYear,
        String description,
        String headquarters,
        Map<String, String> socialLinks,
        boolean verified,
        CompanyStatus status,
        Long ownerUserId,
        Instant createdAt) {
}
