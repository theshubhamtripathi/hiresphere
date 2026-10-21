package com.hiresphere.company.web;

import com.hiresphere.common.dto.ApiResponse;
import com.hiresphere.common.dto.PageResponse;
import com.hiresphere.common.exception.ForbiddenException;
import com.hiresphere.common.security.UserContext;
import com.hiresphere.common.security.UserContextHolder;
import com.hiresphere.company.domain.CompanyStatus;
import com.hiresphere.company.dto.CompanyResponse;
import com.hiresphere.company.dto.CreateCompanyRequest;
import com.hiresphere.company.dto.UpdateCompanyRequest;
import com.hiresphere.company.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> create(@Valid @RequestBody CreateCompanyRequest request) {
        UserContext ctx = UserContextHolder.require();
        if (!ctx.isEmployer()) {
            throw new ForbiddenException("Only employers can create a company");
        }
        CompanyResponse company = companyService.create(ctx.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Company created", company));
    }

    @GetMapping
    public PageResponse<CompanyResponse> list(@RequestParam(required = false) String q,
                                              @RequestParam(required = false) Boolean verified,
                                              @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(companyService.search(CompanyStatus.ACTIVE, verified, q, pageable));
    }

    @GetMapping("/my")
    public ApiResponse<CompanyResponse> my() {
        return ApiResponse.ok(companyService.getMine(UserContextHolder.require().userId()));
    }

    @PutMapping("/my")
    public ApiResponse<CompanyResponse> updateMy(@Valid @RequestBody UpdateCompanyRequest request) {
        return ApiResponse.ok(companyService.updateMine(UserContextHolder.require().userId(), request));
    }

    @GetMapping("/{idOrSlug}")
    public ApiResponse<CompanyResponse> get(@PathVariable String idOrSlug) {
        return ApiResponse.ok(companyService.getByIdOrSlug(idOrSlug));
    }
}
