package com.nexthire.trending;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class TrendingRepository {

    private final Map<Long, TrendingItem> store = new ConcurrentHashMap<>();

    public TrendingRepository() {
        seedTrending();
    }

    private void seedTrending() {
        save(new TrendingItem(1L, "Popular Data Analyst Interview Topics", "data-analyst",
                "SQL & BI Analytics", 98,
                "Window functions, ETL pipelines, Power BI/Tableau dashboarding, and revenue churn diagnostic reasoning.",
                List.of("SQL", "WindowFunctions", "KPIs", "Tableau")));

        save(new TrendingItem(2L, "Frequently Asked SQL Questions in 2026", "data-analyst",
                "Advanced SQL", 96,
                "CTEs, recursive queries, self-joins, performance indexing, and duplicate row deduplication without primary keys.",
                List.of("SQL", "Performance", "Indexing", "Joins")));

        save(new TrendingItem(3L, "Common Data Science Interview Questions", "data-scientist",
                "Machine Learning & LLMs", 95,
                "Fine-tuning vs RAG architecture, transformer attention mechanisms, cross-entropy loss, and gradient boosting vs Random Forests.",
                List.of("ML", "LLM", "RAG", "Transformers")));

        save(new TrendingItem(4L, "Popular HR & Culture Fit Interview Questions", "general-interview",
                "Behavioral & STAR", 94,
                "Navigating team misalignment, overcoming deliverable setbacks, career motivation, and cross-functional leadership.",
                List.of("HR", "STAR", "Leadership", "Culture")));

        save(new TrendingItem(5L, "Trending Technical Skills in Software Engineering", "software-developer",
                "Distributed Systems", 97,
                "Event-driven architecture with Kafka, microservices observability, Redis caching strategies, and idempotent API design.",
                List.of("Microservices", "Kafka", "Redis", "SystemDesign")));

        save(new TrendingItem(6L, "High-Frequency Executive Communication Patterns", "english-communication",
                "Spoken Fluency", 92,
                "Executive presence, pacing, structuring answers into 3 punchy points, and eliminating verbal crutches.",
                List.of("Communication", "ExecutivePresence", "Signposting")));
    }

    public List<TrendingItem> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparingInt(TrendingItem::getPopularity).reversed())
                .collect(Collectors.toList());
    }

    public List<TrendingItem> findByCategory(String category) {
        if (category == null || category.isBlank()) return findAll();
        String catNorm = category.toLowerCase().trim().replace("-", "").replace("_", "");
        return store.values().stream()
                .filter(item -> item.getCategory().toLowerCase().replace("-", "").contains(catNorm))
                .sorted(Comparator.comparingInt(TrendingItem::getPopularity).reversed())
                .collect(Collectors.toList());
    }

    public TrendingItem save(TrendingItem item) {
        store.put(item.getId(), item);
        return item;
    }
}
