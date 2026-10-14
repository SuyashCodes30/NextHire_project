package com.nexthire.category;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CategoryRepository — In-memory repository for interview categories.
 * Designed to easily transition to Spring Data JPA / MySQL when ready.
 */
@Repository
public class CategoryRepository {

    private final Map<String, InterviewCategory> categories = new ConcurrentHashMap<>();

    public CategoryRepository() {
        seedCategories();
    }

    private void seedCategories() {
        save(new InterviewCategory(
                "general-interview",
                "General Interview",
                "Core behavioral questions, self-introduction, STAR method scenarios, and universal workplace evaluation.",
                "🎯",
                List.of("EASY", "MEDIUM", "HARD"),
                List.of("HR", "COMMUNICATION", "DESCRIPTIVE"),
                List.of("Self Introduction", "Behavioral & STAR Method", "Workplace Scenarios", "Career Goals", "Problem Solving"),
                true
        ));

        save(new InterviewCategory(
                "data-analyst",
                "Data Analyst",
                "Practical data querying, dashboarding, statistical reasoning, spreadsheet modeling, and business KPI analysis.",
                "📊",
                List.of("EASY", "MEDIUM", "HARD"),
                List.of("TECHNICAL", "DESCRIPTIVE", "MULTIPLE_CHOICE"),
                List.of("SQL", "Excel", "Statistics", "Data Visualization", "Analytical Thinking"),
                true
        ));

        save(new InterviewCategory(
                "data-scientist",
                "Data Scientist",
                "Machine learning algorithms, Python libraries, probabilistic modeling, deep learning architectures, and experimentation.",
                "🤖",
                List.of("EASY", "MEDIUM", "HARD"),
                List.of("TECHNICAL", "DESCRIPTIVE"),
                List.of("Python", "Machine Learning", "Statistics", "Deep Learning", "Data Science Concepts"),
                true
        ));

        save(new InterviewCategory(
                "software-developer",
                "Software Developer",
                "Algorithms, data structures, system design, object-oriented principles, database management, and networking.",
                "💻",
                List.of("EASY", "MEDIUM", "HARD"),
                List.of("TECHNICAL", "DESCRIPTIVE", "MULTIPLE_CHOICE"),
                List.of("Programming", "DSA", "OOP", "DBMS", "Computer Networks"),
                true
        ));

        save(new InterviewCategory(
                "english-communication",
                "English / Communication Interview",
                "Spoken fluency, professional vocabulary, clear grammar structure, corporate tone, and effective articulate speaking.",
                "🗣️",
                List.of("EASY", "MEDIUM", "HARD"),
                List.of("COMMUNICATION", "HR", "DESCRIPTIVE"),
                List.of("Grammar", "Vocabulary", "Communication", "HR questions", "Speaking practice"),
                true
        ));
    }

    public List<InterviewCategory> findAll() {
        return new ArrayList<>(categories.values());
    }

    public Optional<InterviewCategory> findById(String id) {
        if (id == null) return Optional.empty();
        // Support flexible lookup (slug or normalized name)
        String normalized = id.toLowerCase().trim().replace(" ", "-").replace("_", "-");
        if (categories.containsKey(normalized)) {
            return Optional.of(categories.get(normalized));
        }
        return categories.values().stream()
                .filter(c -> c.getCategoryId().equalsIgnoreCase(normalized) || c.getCategoryName().equalsIgnoreCase(id))
                .findFirst();
    }

    public InterviewCategory save(InterviewCategory category) {
        categories.put(category.getCategoryId(), category);
        return category;
    }

    public boolean existsById(String id) {
        return findById(id).isPresent();
    }
}
