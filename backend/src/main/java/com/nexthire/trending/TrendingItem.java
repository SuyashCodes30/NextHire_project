package com.nexthire.trending;

import java.util.List;

public class TrendingItem {
    private Long id;
    private String title;
    private String category;
    private String topic;
    private int popularity; // 0-100
    private String description;
    private List<String> tags;

    public TrendingItem() {}

    public TrendingItem(Long id, String title, String category, String topic, int popularity,
                        String description, List<String> tags) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.topic = topic;
        this.popularity = popularity;
        this.description = description;
        this.tags = tags;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public int getPopularity() { return popularity; }
    public void setPopularity(int popularity) { this.popularity = popularity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
