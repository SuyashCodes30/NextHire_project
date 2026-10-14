package com.nexthire.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * LearningResource — Represents educational deep-dive resource for an interview topic/question.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearningResource {

    private String title;
    private String description;
    private String url;

    public LearningResource() {}

    public LearningResource(String title, String description, String url) {
        this.title = title;
        this.description = description;
        this.url = url;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
