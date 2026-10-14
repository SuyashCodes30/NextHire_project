package com.nexthire.service.impl;

import com.nexthire.exception.ResourceNotFoundException;
import com.nexthire.model.Question;
import com.nexthire.question.QuestionRepository;
import com.nexthire.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;

    @Autowired
    public QuestionServiceImpl(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    @Override
    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", id));
    }

    @Override
    public List<Question> getQuestionsByCategory(String category) {
        return questionRepository.findByCategory(category);
    }

    @Override
    public List<Question> getQuestionsByDifficulty(String difficulty) {
        return questionRepository.findByDifficulty(difficulty);
    }

    @Override
    public List<Question> getQuestionsByCategoryAndDifficulty(String category, String difficulty) {
        return questionRepository.search(category, difficulty, null, null);
    }

    @Override
    public List<Question> searchQuestions(String category, String difficulty, String topic, String questionType) {
        return questionRepository.search(category, difficulty, topic, questionType);
    }

    @Override
    public List<Question> getRandomQuestions(String category, String difficulty, int count) {
        List<Question> matching = questionRepository.search(category, difficulty, null, null);

        if (matching.isEmpty()) {
            matching = questionRepository.findByCategory(category);
        }
        if (matching.isEmpty()) {
            matching = questionRepository.findAll();
        }

        List<Question> shuffled = new ArrayList<>(matching);
        Collections.shuffle(shuffled);
        return shuffled.stream().limit(count).collect(Collectors.toList());
    }

    @Override
    public List<Question> getDiverseQuestions(String category, String difficulty, int count) {
        if (count <= 0) count = 5;

        // 1. Find primary candidates matching category and difficulty
        List<Question> primaryCandidates = questionRepository.search(category, difficulty, null, null);

        // 2. Fallback category pool if primary candidates alone cannot fulfill count
        List<Question> categoryPool = questionRepository.findByCategory(category);
        if (categoryPool.isEmpty()) {
            categoryPool = questionRepository.findAll();
        }

        List<Question> selected = new ArrayList<>();
        Set<Long> selectedIds = new HashSet<>();

        // Group primary candidates into Topic Buckets
        Map<String, List<Question>> topicBuckets = new LinkedHashMap<>();
        for (Question q : primaryCandidates) {
            String topic = (q.getTopic() != null && !q.getTopic().isBlank()) ? q.getTopic() : "General";
            topicBuckets.computeIfAbsent(topic, k -> new ArrayList<>()).add(q);
        }

        // Shuffle questions within each bucket for variance across sessions
        for (List<Question> bucket : topicBuckets.values()) {
            Collections.shuffle(bucket);
        }

        // 3. Round-Robin Topic Distribution across primary candidates
        List<String> topics = new ArrayList<>(topicBuckets.keySet());
        boolean addedInPass = true;
        while (selected.size() < count && addedInPass && !topics.isEmpty()) {
            addedInPass = false;
            for (String topic : topics) {
                if (selected.size() >= count) break;
                List<Question> bucket = topicBuckets.get(topic);
                for (Iterator<Question> it = bucket.iterator(); it.hasNext(); ) {
                    Question candidate = it.next();
                    if (!selectedIds.contains(candidate.getId())) {
                        selected.add(candidate);
                        selectedIds.add(candidate.getId());
                        it.remove();
                        addedInPass = true;
                        break; // Rotate to next topic
                    }
                }
            }
        }

        // 4. If we still need more questions, round-robin from other difficulty levels in same category
        if (selected.size() < count) {
            Map<String, List<Question>> fallbackBuckets = new LinkedHashMap<>();
            for (Question q : categoryPool) {
                if (!selectedIds.contains(q.getId())) {
                    String topic = (q.getTopic() != null && !q.getTopic().isBlank()) ? q.getTopic() : "General";
                    fallbackBuckets.computeIfAbsent(topic, k -> new ArrayList<>()).add(q);
                }
            }

            for (List<Question> bucket : fallbackBuckets.values()) {
                Collections.shuffle(bucket);
            }

            List<String> fallbackTopics = new ArrayList<>(fallbackBuckets.keySet());
            addedInPass = true;
            while (selected.size() < count && addedInPass && !fallbackTopics.isEmpty()) {
                addedInPass = false;
                for (String topic : fallbackTopics) {
                    if (selected.size() >= count) break;
                    List<Question> bucket = fallbackBuckets.get(topic);
                    for (Iterator<Question> it = bucket.iterator(); it.hasNext(); ) {
                        Question candidate = it.next();
                        if (!selectedIds.contains(candidate.getId())) {
                            selected.add(candidate);
                            selectedIds.add(candidate.getId());
                            it.remove();
                            addedInPass = true;
                            break;
                        }
                    }
                }
            }
        }

        return selected;
    }
}
