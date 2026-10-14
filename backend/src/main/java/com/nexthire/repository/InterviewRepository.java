package com.nexthire.repository;

import com.nexthire.model.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * InterviewRepository — Spring Data JPA repository for Interview entity.
 */
@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByUserId(Long userId);
    List<Interview> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Interview> findByUserIdAndStatus(Long userId, String status);
    int countByUserId(Long userId);
    int countByUserIdAndStatus(Long userId, String status);
}
