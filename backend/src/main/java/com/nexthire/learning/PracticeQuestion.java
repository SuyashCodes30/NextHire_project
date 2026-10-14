package com.nexthire.learning;

public class PracticeQuestion {
    private int id;
    private String prompt;
    private String type; // EXERCISE, FILL_BLANK, REPHRASE
    private String sampleAnswer;
    private String explanation;

    public PracticeQuestion() {}

    public PracticeQuestion(int id, String prompt, String type, String sampleAnswer, String explanation) {
        this.id = id;
        this.prompt = prompt;
        this.type = type;
        this.sampleAnswer = sampleAnswer;
        this.explanation = explanation;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSampleAnswer() { return sampleAnswer; }
    public void setSampleAnswer(String sampleAnswer) { this.sampleAnswer = sampleAnswer; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
