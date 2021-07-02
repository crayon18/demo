package com.example.demo.Study;

import com.example.demo.domain.Study;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface StudyRepositroy extends JpaRepository<Study, Long> {

    boolean existsByPath(String path);

    @EntityGraph(value = "Study.withAll", type = EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);

    @EntityGraph(value = "Study.withAll", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithTagsByPath(String path);

    @EntityGraph(value = "Study.withAll", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithZonesByPath(String path);

    @EntityGraph(value = "Study.withAll", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithMangersByPath(String path);

    @EntityGraph(value = "Study.withAll", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithMembersByPath(String path);

    Study findStudyOnlyByPath(String path);
}