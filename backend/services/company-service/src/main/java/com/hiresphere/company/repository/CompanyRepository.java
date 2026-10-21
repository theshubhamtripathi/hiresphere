package com.hiresphere.company.repository;

import com.hiresphere.company.domain.Company;
import com.hiresphere.company.domain.CompanyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findBySlug(String slug);

    Optional<Company> findByOwnerUserId(Long ownerUserId);

    boolean existsByOwnerUserId(Long ownerUserId);

    boolean existsBySlug(String slug);

    @Query("""
            select c from Company c
            where (:status is null or c.status = :status)
              and (:verified is null or c.verified = :verified)
              and (lower(c.name) like :pattern or lower(coalesce(c.industry, '')) like :pattern)
            """)
    Page<Company> search(@Param("status") CompanyStatus status,
                         @Param("verified") Boolean verified,
                         @Param("pattern") String pattern,
                         Pageable pageable);
}
