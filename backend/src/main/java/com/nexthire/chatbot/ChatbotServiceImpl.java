package com.nexthire.chatbot;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    @Override
    public ChatbotResponse processMessage(ChatbotRequest request) {
        String msg = (request == null || request.getMessage() == null) ? "" : request.getMessage().trim().toLowerCase();

        // 1. "How do I start an interview?"
        if (msg.contains("start") && (msg.contains("interview") || msg.contains("how") || msg.contains("practice"))) {
            return new ChatbotResponse(
                    "To start an interview on NextHire:\n" +
                    "1. Click the 'Start practicing' button in the navbar or 'Launch AI Evaluation' on the homepage.\n" +
                    "2. Or browse the 'Practice Any Interview' section and select a track like Data Analyst, Software Developer, or English.\n" +
                    "3. An interactive session window will open with real questions and simulated audio waves.\n" +
                    "4. Answer each question via typing or dictation and submit for instant AI evaluation!",
                    List.of("Which interview category should I choose?", "How does the interview work?", "What is Data Analyst interview?"),
                    "START_INTERVIEW"
            );
        }

        // 2. "Which interview category should I choose?"
        if (msg.contains("which") && (msg.contains("category") || msg.contains("choose") || msg.contains("track"))) {
            return new ChatbotResponse(
                    "Choose based on your immediate job target:\n" +
                    "• Software Developer: If preparing for SWE, Full Stack, or Backend roles (DSA, OOP, System Design, DBMS).\n" +
                    "• Data Analyst: If targeting BI, SQL querying, dashboards, Excel modeling, and KPI analytics.\n" +
                    "• Data Scientist: If targeting ML, predictive modeling, statistics, Python, and Deep Learning.\n" +
                    "• English / Communication: If you want to refine spoken fluency, professional tone, and vocabulary.\n" +
                    "• General Interview: Ideal for foundational behavioral and STAR method readiness across all roles.",
                    List.of("What is Data Analyst interview?", "What is Data Scientist interview?", "How can I practice English?"),
                    "CHOOSE_CATEGORY"
            );
        }

        // 3. "What is Data Analyst interview?"
        if (msg.contains("data analyst") || (msg.contains("analyst") && msg.contains("what"))) {
            return new ChatbotResponse(
                    "The Data Analyst interview track evaluates your ability to turn raw data into actionable business intelligence:\n" +
                    "• SQL: Complex joins, window functions (ROW_NUMBER, DENSE_RANK), aggregations, and subqueries.\n" +
                    "• Excel: Advanced modeling, INDEX/MATCH, dynamic arrays, and pivot tables.\n" +
                    "• Statistics: Correlation vs causation, distributions, and A/B test metric interpretations.\n" +
                    "• Data Visualization: Chart selection (box plot vs bar) and executive dashboard storytelling.\n" +
                    "• Analytical Thinking: Diagnosing sudden metric drops (e.g. 15% DAU dip).",
                    List.of("Start Data Analyst interview", "Frequently Asked SQL Questions", "What is Data Scientist interview?"),
                    "DATA_ANALYST_INFO"
            );
        }

        // 4. "What is Data Scientist interview?"
        if (msg.contains("data scientist") || (msg.contains("scientist") && msg.contains("what"))) {
            return new ChatbotResponse(
                    "The Data Scientist interview track evaluates core statistical learning and production ML deployment:\n" +
                    "• Python: GIL nuances, vectorized computing (NumPy/Pandas), and memory efficiency.\n" +
                    "• Machine Learning: Bias-variance tradeoff, regularization (L1/L2), gradient boosting, and cross-validation.\n" +
                    "• Statistics: P-values, statistical power, Type I/II errors, and hypothesis testing.\n" +
                    "• Deep Learning: Vanishing gradients, transformers, CNNs, and residual architectures.\n" +
                    "• Concepts: Handling severe class imbalance (SMOTE, PR-AUC) in production.",
                    List.of("Start Data Scientist interview", "Common Data Science Interview Questions", "What is Data Analyst interview?"),
                    "DATA_SCIENTIST_INFO"
            );
        }

        // 5. "How does the interview work?"
        if (msg.contains("how") && (msg.contains("work") || msg.contains("flow") || msg.contains("process"))) {
            return new ChatbotResponse(
                    "Here is how NextHire interviews work:\n" +
                    "1. Role & Difficulty Setup: A custom question set is selected for your domain.\n" +
                    "2. Live Question Delivery: Questions appear sequentially with difficulty badges and topic tags.\n" +
                    "3. Response Submission: Type or speak your answer. Your answer duration and word count are recorded.\n" +
                    "4. Immediate AI Evaluation: NextHire's evaluation engine scores your answers across 5 core pillars: Communication, Confidence, Technical Knowledge, Clarity, and Structure.",
                    List.of("How are results calculated?", "How can I improve my score?", "Start practicing"),
                    "HOW_IT_WORKS"
            );
        }

        // 6. "How are results calculated?"
        if (msg.contains("calculated") || msg.contains("scored") || msg.contains("score work") || msg.contains("scoring")) {
            return new ChatbotResponse(
                    "NextHire results are calculated across a 100-point composite score:\n" +
                    "• Technical Accuracy (30%): Depth of domain principles, algorithm correctness, and trade-off analysis.\n" +
                    "• Communication & Clarity (25%): Sentence structure, professional vocabulary, and conciseness.\n" +
                    "• Structure & STAR Method (20%): Logical organization (Situation, Task, Action, Result).\n" +
                    "• Delivery Confidence (15%): Optimal pacing without excessive filler words.\n" +
                    "• Readiness Rating: 85+ (Interview Ready), 70-84 (Proficient), <70 (Needs Practice).",
                    List.of("How can I improve my score?", "What should I do after completing an interview?"),
                    "SCORING_INFO"
            );
        }

        // 7. "How can I improve my score?"
        if (msg.contains("improve") && (msg.contains("score") || msg.contains("performance") || msg.contains("better"))) {
            return new ChatbotResponse(
                    "3 Proven Tactics to Elevate Your Score:\n" +
                    "1. Quantify Everything: Instead of 'I sped up queries', state 'I indexed foreign keys, reducing p99 latency by 65%'.\n" +
                    "2. Adhere to STAR: Dedicate 50% of your time to your explicit personal Actions.\n" +
                    "3. Use Signposting: Say 'There are two key tradeoffs: first, memory overhead; second, consistency.' This projects senior-level clarity.",
                    List.of("How can I practice English?", "How are results calculated?", "Start practicing"),
                    "IMPROVE_SCORE"
            );
        }

        // 8. "How can I practice English?"
        if (msg.contains("english") || msg.contains("communication") || msg.contains("grammar") || msg.contains("vocabulary")) {
            return new ChatbotResponse(
                    "You can practice English on NextHire through two pathways:\n" +
                    "1. NextHire Learning Academy: Visit '/api/learning/english' to study 6 curated modules covering Grammar, Impact Vocabulary, Sentence Formation, and HR Frameworks.\n" +
                    "2. English & Communication Mock Interview: Start a session in the 'English / Communication' track to practice verbal cadence with instant AI evaluation!",
                    List.of("What is English Learning Module?", "Which interview category should I choose?", "Start practicing"),
                    "ENGLISH_PRACTICE"
            );
        }

        // 9. "What should I do after completing an interview?"
        if (msg.contains("after") || (msg.contains("completed") && msg.contains("interview")) || msg.contains("next steps")) {
            return new ChatbotResponse(
                    "After completing your interview:\n" +
                    "1. Review your AI Evaluation Report: Examine your key strengths and specific improvement suggestions.\n" +
                    "2. Check your Dashboard: Track your updated 7-day streak, earned XP, and readiness milestone unlock.\n" +
                    "3. Study Weak Areas: Dive into the English Learning module or review Trending Questions for your track.\n" +
                    "4. Retake in 24 Hours: Practice with different difficulties (Easy ➡️ Medium ➡️ Hard) to build muscle memory!",
                    List.of("How do I start an interview?", "How are results calculated?", "How can I practice English?"),
                    "AFTER_INTERVIEW"
            );
        }

        // Default greeting / helpful fallback
        return new ChatbotResponse(
                "Hello! I am your NextHire AI Career & Platform Assistant. I can guide you through interview tracks, explain scoring, or help you practice.\n\n" +
                "Here are popular topics you can ask me about:",
                List.of(
                        "How do I start an interview?",
                        "Which interview category should I choose?",
                        "What is Data Analyst interview?",
                        "What is Data Scientist interview?",
                        "How are results calculated?",
                        "How can I practice English?"
                ),
                "GENERAL_ASSISTANCE"
        );
    }
}
