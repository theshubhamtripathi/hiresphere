package com.hiresphere.job.repository;

import com.hiresphere.job.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Tag> findByNameInIgnoreCase(List<String> names);

    List<Tag> findAllByOrderByNameAsc();
}
