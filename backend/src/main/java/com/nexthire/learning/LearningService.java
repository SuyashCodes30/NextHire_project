package com.nexthire.learning;

import java.util.List;
import java.util.Map;

public interface LearningService {
    Map<String, Object> getLearningOverview();
    Map<String, Object> getEnglishOverview();
    List<LearningTopic> getEnglishTopics();
    LearningTopic getEnglishTopicById(String topicId);
}
