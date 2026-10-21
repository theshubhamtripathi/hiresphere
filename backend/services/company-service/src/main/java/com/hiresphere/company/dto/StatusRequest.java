package com.hiresphere.company.dto;

import com.hiresphere.company.domain.CompanyStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull CompanyStatus status) {
}
