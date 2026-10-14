package com.nexthire.learning;

import java.util.List;

public class Lesson {
    private int order;
    private String title;
    private String content;
    private List<String> keyTakeaways;

    public Lesson() {}

    public Lesson(int order, String title, String content, List<String> keyTakeaways) {
        this.order = order;
        this.title = title;
        this.content = content;
        this.keyTakeaways = keyTakeaways;
    }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getKeyTakeaways() { return keyTakeaways; }
    public void setKeyTakeaways(List<String> keyTakeaways) { this.keyTakeaways = keyTakeaways; }
}
