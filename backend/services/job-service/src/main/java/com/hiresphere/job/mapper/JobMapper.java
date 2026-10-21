package com.hiresphere.job.mapper;

import com.hiresphere.job.domain.Job;
import com.hiresphere.job.domain.Skill;
import com.hiresphere.job.domain.Tag;
import com.hiresphere.job.dto.JobResponse;
import com.hiresphere.job.dto.JobSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface JobMapper {

    @org.mapstruct.Mapping(target = "skills", source = "skills", qualifiedByName = "skillNames")
    @org.mapstruct.Mapping(target = "tags", source = "tags", qualifiedByName = "tagNames")
    JobResponse toResponse(Job job);

    @org.mapstruct.Mapping(target = "skills", source = "skills", qualifiedByName = "skillNames")
    JobSummaryResponse toSummary(Job job);

    @Named("skillNames")
    default List<String> skillNames(Set<Skill> skills) {
        return skills == null ? List.of() : skills.stream().map(Skill::getName).sorted().toList();
    }

    @Named("tagNames")
    default List<String> tagNames(Set<Tag> tags) {
        return tags == null ? List.of() : tags.stream().map(Tag::getName).sorted().toList();
    }
}
