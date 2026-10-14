package com.nexthire.service;

import com.nexthire.model.Question;

import java.util.List;

public interface QuestionService {
    List<Question> getAllQuestions();
    Question getQuestionById(Long id);
    List<Question> getQuestionsByCategory(String category);
    List<Question> getQuestionsByDifficulty(String difficulty);
    List<Question> getQuestionsByCategoryAndDifficulty(String category, String difficulty);
    List<Question> searchQuestions(String category, String difficulty, String topic, String questionType);
    List<Question> getRandomQuestions(String category, String difficulty, int count);
    List<Question> getDiverseQuestions(String category, String difficulty, int count);
}
