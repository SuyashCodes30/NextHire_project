package com.nexthire.chatbot;

import java.util.List;

public class ChatbotResponse {

    private String message;
    private List<String> suggestions;
    private String intent;

    public ChatbotResponse() {}

    public ChatbotResponse(String message, List<String> suggestions, String intent) {
        this.message = message;
        this.suggestions = suggestions;
        this.intent = intent;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }
}
