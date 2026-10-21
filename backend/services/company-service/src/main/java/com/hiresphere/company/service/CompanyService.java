package com.hiresphere.company.service;

import com.hiresphere.common.exception.ConflictException;
import com.hiresphere.common.exception.ResourceNotFoundException;
import com.hiresphere.company.domain.Company;
import com.hiresphere.company.domain.CompanyStatus;
import com.hiresphere.company.dto.CompanyResponse;
import com.hiresphere.company.dto.CreateCompanyRequest;
import com.hiresphere.company.dto.UpdateCompanyRequest;
import com.hiresphere.company.mapper.CompanyMapper;
import com.hiresphere.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    @Transactional
    public CompanyResponse create(Long ownerUserId, CreateCompanyRequest request) {
        if (companyRepository.existsByOwnerUserId(ownerUserId)) {
            throw new ConflictException("You already own a company profile");
        }
        Company company = new Company();
        company.setOwnerUserId(ownerUserId);
        company.setName(request.name());
        company.setSlug(SlugGenerator.generate(request.name(), companyRepository::existsBySlug));
        apply(company, request.website(), request.industry(), request.foundedYear(),
                request.description(), request.headquarters(), request.logoUrl());
        company.setSize(request.size());
        company.setSocialLinks(request.socialLinks() == null ? new HashMap<>() : request.socialLinks());
        return companyMapper.toResponse(companyRepository.save(company));
    }

    @Transactional(readOnly = true)
    public CompanyResponse getByIdOrSlug(String idOrSlug) {
        return companyMapper.toResponse(findByIdOrSlug(idOrSlug));
    }

    @Transactional(readOnly = true)
    public CompanyResponse getMine(Long ownerUserId) {
        return companyMapper.toResponse(requireOwned(ownerUserId));
    }

    @Transactional
    public CompanyResponse updateMine(Long ownerUserId, UpdateCompanyRequest request) {
        Company company = requireOwned(ownerUserId);
        if (StringUtils.hasText(request.name())) {
            company.setName(request.name());
        }
        apply(company, request.website(), request.industry(), request.foundedYear(),
                request.description(), request.headquarters(), request.logoUrl());
        if (request.size() != null) {
            company.setSize(request.size());
        }
        if (request.socialLinks() != null) {
            company.setSocialLinks(request.socialLinks());
        }
        return companyMapper.toResponse(company);
    }

    @Transactional(readOnly = true)
    public Page<CompanyResponse> search(CompanyStatus status, Boolean verified, String q, Pageable pageable) {
        String pattern = StringUtils.hasText(q) ? "%" + q.trim().toLowerCase() + "%" : "%";
        return companyRepository.search(status, verified, pattern, pageable).map(companyMapper::toResponse);
    }

    @Transactional
    public CompanyResponse setVerified(Long id, boolean verified) {
        Company company = find(id);
        company.setVerified(verified);
        return companyMapper.toResponse(company);
    }

    @Transactional
    public CompanyResponse setStatus(Long id, CompanyStatus status) {
        Company company = find(id);
        company.setStatus(status);
        return companyMapper.toResponse(company);
    }

    private void apply(Company company, String website, String industry, Integer foundedYear,
                       String description, String headquarters, String logoUrl) {
        company.setWebsite(website);
        company.setIndustry(industry);
        company.setFoundedYear(foundedYear);
        company.setDescription(description);
        company.setHeadquarters(headquarters);
        company.setLogoUrl(logoUrl);
    }

    private Company find(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Company", id));
    }

    private Company requireOwned(Long ownerUserId) {
        return companyRepository.findByOwnerUserId(ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("You have not created a company yet"));
    }

    private Company findByIdOrSlug(String idOrSlug) {
        if (idOrSlug.matches("\\d+")) {
            return find(Long.parseLong(idOrSlug));
        }
        return companyRepository.findBySlug(idOrSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + idOrSlug));
    }
}
