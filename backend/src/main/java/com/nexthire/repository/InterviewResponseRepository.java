package com.nexthire.repository;

import com.nexthire.model.InterviewResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * InterviewResponseRepository — Spring Data JPA repository for InterviewResponse entity.
 */
@Repository
public interface InterviewResponseRepository extends JpaRepository<InterviewResponse, Long> {
    List<InterviewResponse> findByInterviewId(Long interviewId);
    List<InterviewResponse> findByInterviewIdOrderBySubmittedAtAsc(Long interviewId);
    int countByInterviewId(Long interviewId);
    void deleteByInterviewId(Long interviewId);
}
