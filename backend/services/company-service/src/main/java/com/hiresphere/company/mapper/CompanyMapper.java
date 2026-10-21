package com.hiresphere.company.mapper;

import com.hiresphere.company.domain.Company;
import com.hiresphere.company.dto.CompanyResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    CompanyResponse toResponse(Company company);
}
