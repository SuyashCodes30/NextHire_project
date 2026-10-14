package com.nexthire.learning;

import java.util.List;

public class LearningTopic {
    private String id;
    private String title;
    private String description;
    private String category;
    private String icon;
    private int estimatedMinutes;
    private List<Lesson> lessons;
    private List<String> examples;
    private List<PracticeQuestion> practiceQuestions;

    public LearningTopic() {}

    public LearningTopic(String id, String title, String description, String category, String icon,
                         int estimatedMinutes, List<Lesson> lessons, List<String> examples,
                         List<PracticeQuestion> practiceQuestions) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.icon = icon;
        this.estimatedMinutes = estimatedMinutes;
        this.lessons = lessons;
        this.examples = examples;
        this.practiceQuestions = practiceQuestions;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTopicId() { return id; }
    public void setTopicId(String topicId) { this.id = topicId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public int getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(int estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }

    public List<Lesson> getLessons() { return lessons; }
    public void setLessons(List<Lesson> lessons) { this.lessons = lessons; }

    public List<String> getExamples() { return examples; }
    public void setExamples(List<String> examples) { this.examples = examples; }

    public List<PracticeQuestion> getPracticeQuestions() { return practiceQuestions; }
    public void setPracticeQuestions(List<PracticeQuestion> practiceQuestions) { this.practiceQuestions = practiceQuestions; }
}
