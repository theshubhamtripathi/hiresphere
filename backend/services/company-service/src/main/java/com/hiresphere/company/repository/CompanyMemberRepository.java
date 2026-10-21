package com.hiresphere.company.repository;

import com.hiresphere.company.domain.CompanyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyMemberRepository extends JpaRepository<CompanyMember, Long> {

    List<CompanyMember> findByCompanyId(Long companyId);

    boolean existsByCompanyIdAndUserId(Long companyId, Long userId);
}
