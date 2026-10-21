package com.hiresphere.resume.repository;

import com.hiresphere.resume.domain.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<Resume> findByIdAndUserId(Long id, Long userId);

    Optional<Resume> findByUserIdAndIsDefaultTrue(Long userId);

    boolean existsByUserId(Long userId);
}
