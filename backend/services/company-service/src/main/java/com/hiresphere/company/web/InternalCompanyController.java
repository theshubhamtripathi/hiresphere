package com.hiresphere.company.web;

import com.hiresphere.company.dto.CompanyResponse;
import com.hiresphere.company.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Service-to-service lookups. Never exposed through the gateway. */
@RestController
@RequestMapping("/api/internal/companies")
@RequiredArgsConstructor
public class InternalCompanyController {

    private final CompanyService companyService;

    @GetMapping("/{id}")
    public CompanyResponse getById(@PathVariable Long id) {
        return companyService.getByIdOrSlug(String.valueOf(id));
    }

    @GetMapping("/by-owner/{ownerUserId}")
    public CompanyResponse getByOwner(@PathVariable Long ownerUserId) {
        return companyService.getMine(ownerUserId);
    }
}
