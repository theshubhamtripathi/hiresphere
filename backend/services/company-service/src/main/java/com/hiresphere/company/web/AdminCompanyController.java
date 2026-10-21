package com.hiresphere.company.web;

import com.hiresphere.common.dto.ApiResponse;
import com.hiresphere.common.dto.PageResponse;
import com.hiresphere.common.exception.ForbiddenException;
import com.hiresphere.common.security.UserContextHolder;
import com.hiresphere.company.domain.CompanyStatus;
import com.hiresphere.company.dto.CompanyResponse;
import com.hiresphere.company.dto.StatusRequest;
import com.hiresphere.company.dto.VerifyRequest;
import com.hiresphere.company.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/companies")
@RequiredArgsConstructor
public class AdminCompanyController {

    private final CompanyService companyService;

    @GetMapping
    public PageResponse<CompanyResponse> list(@RequestParam(required = false) CompanyStatus status,
                                              @RequestParam(required = false) Boolean verified,
                                              @RequestParam(required = false) String q,
                                              @PageableDefault(size = 20) Pageable pageable) {
        requireAdmin();
        return PageResponse.from(companyService.search(status, verified, q, pageable));
    }

    @PatchMapping("/{id}/verify")
    public ApiResponse<CompanyResponse> verify(@PathVariable Long id, @Valid @RequestBody VerifyRequest request) {
        requireAdmin();
        return ApiResponse.ok(companyService.setVerified(id, request.verified()));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<CompanyResponse> status(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        requireAdmin();
        return ApiResponse.ok(companyService.setStatus(id, request.status()));
    }

    private void requireAdmin() {
        if (!UserContextHolder.require().isAdmin()) {
            throw new ForbiddenException("Administrator access required");
        }
    }
}
