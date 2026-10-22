package com.hiresphere.application.repository;

import com.hiresphere.application.domain.ApplicationNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationNoteRepository extends JpaRepository<ApplicationNote, Long> {

    List<ApplicationNote> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);

    Optional<ApplicationNote> findByIdAndApplicationId(Long id, Long applicationId);
}
