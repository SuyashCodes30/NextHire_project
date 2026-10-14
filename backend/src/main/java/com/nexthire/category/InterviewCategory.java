package com.nexthire.category;

import java.util.List;

/**
 * InterviewCategory — Domain model representing an interview category.
 *
 * Supported categories:
 *   - general-interview: General Interview
 *   - data-analyst: Data Analyst (SQL, Excel, Statistics, Data Visualization, Analytical Thinking)
 *   - data-scientist: Data Scientist (Python, ML, Statistics, Deep Learning, DS Concepts)
 *   - software-developer: Software Developer (Programming, DSA, OOP, DBMS, Networks)
 *   - english-communication: English / Communication (Grammar, Vocabulary, Communication, HR, Speaking)
 */
public class InterviewCategory {

    private String categoryId;
    private String categoryName;
    private String description;
    private String icon;
    private List<String> difficultyLevels;
    private List<String> questionTypes;
    private List<String> topics;
    private boolean active;

    public InterviewCategory() {}

    public InterviewCategory(String categoryId, String categoryName, String description, String icon,
                             List<String> difficultyLevels, List<String> questionTypes, List<String> topics, boolean active) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
        this.icon = icon;
        this.difficultyLevels = difficultyLevels;
        this.questionTypes = questionTypes;
        this.topics = topics;
        this.active = active;
    }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public List<String> getDifficultyLevels() { return difficultyLevels; }
    public void setDifficultyLevels(List<String> difficultyLevels) { this.difficultyLevels = difficultyLevels; }

    public List<String> getQuestionTypes() { return questionTypes; }
    public void setQuestionTypes(List<String> questionTypes) { this.questionTypes = questionTypes; }

    public List<String> getTopics() { return topics; }
    public void setTopics(List<String> topics) { this.topics = topics; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
