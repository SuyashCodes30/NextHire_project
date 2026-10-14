package com.nexthire.question;

import com.nexthire.model.Question;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository {
    List<Question> findAll();
    Optional<Question> findById(Long id);
    List<Question> findByCategory(String category);
    List<Question> findByDifficulty(String difficulty);
    List<Question> findByTopic(String topic);
    List<Question> search(String category, String difficulty, String topic, String questionType);
    Question save(Question question);
}
