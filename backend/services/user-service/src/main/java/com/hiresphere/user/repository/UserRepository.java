package com.hiresphere.user.repository;

import com.hiresphere.common.enums.Role;
import com.hiresphere.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByRole(Role role);

    /**
     * {@code pattern} is a non-null lower-cased LIKE pattern ({@code %} matches
     * all), avoiding untyped-null parameter issues on PostgreSQL.
     */
    @Query("""
            select u from User u
            where (:role is null or u.role = :role)
              and (:enabled is null or u.enabled = :enabled)
              and (lower(u.fullName) like :pattern or lower(u.email) like :pattern)
            """)
    Page<User> search(@Param("role") Role role,
                      @Param("enabled") Boolean enabled,
                      @Param("pattern") String pattern,
                      Pageable pageable);
}
