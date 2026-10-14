package com.nexthire.repository;

import com.nexthire.model.InterviewResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * InterviewResultRepository — Spring Data JPA repository for InterviewResult entity.
 */
@Repository
public interface InterviewResultRepository extends JpaRepository<InterviewResult, Long> {
    Optional<InterviewResult> findByInterviewId(Long interviewId);
    boolean existsByInterviewId(Long interviewId);
    void deleteByInterviewId(Long interviewId);
}
