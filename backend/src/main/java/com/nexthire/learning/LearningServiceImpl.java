package com.nexthire.learning;

import com.nexthire.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LearningServiceImpl implements LearningService {

    private final LearningRepository learningRepository;

    @Autowired
    public LearningServiceImpl(LearningRepository learningRepository) {
        this.learningRepository = learningRepository;
    }

    @Override
    public Map<String, Object> getLearningOverview() {
        List<LearningTopic> englishTopics = learningRepository.findAll();
        int totalLessons = englishTopics.stream().mapToInt(t -> t.getLessons().size()).sum();
        int totalMinutes = englishTopics.stream().mapToInt(LearningTopic::getEstimatedMinutes).sum();

        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("platform", "NextHire Career & Communication Academy");
        overview.put("availableModules", List.of("English & Communication", "Technical Interview Prep", "Behavioral Leadership"));
        overview.put("englishTopicCount", englishTopics.size());
        overview.put("totalLessons", totalLessons);
        overview.put("totalEstimatedMinutes", totalMinutes);
        return overview;
    }

    @Override
    public Map<String, Object> getEnglishOverview() {
        List<LearningTopic> topics = learningRepository.findAll();
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("trackName", "English & Professional Communication");
        overview.put("description", "Designed specifically for developers, data scientists, and engineers to elevate verbal clarity and interview readiness.");
        overview.put("topicsCount", topics.size());
        overview.put("topics", topics.stream().map(t -> Map.of(
                "id", t.getId(),
                "title", t.getTitle(),
                "description", t.getDescription(),
                "icon", t.getIcon(),
                "estimatedMinutes", t.getEstimatedMinutes(),
                "lessonsCount", t.getLessons().size()
        )).toList());
        return overview;
    }

    @Override
    public List<LearningTopic> getEnglishTopics() {
        return learningRepository.findAll();
    }

    @Override
    public LearningTopic getEnglishTopicById(String topicId) {
        return learningRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning topic not found: " + topicId));
    }
}
