package com.nexthire.learning;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class LearningRepository {

    private final Map<String, LearningTopic> topics = new ConcurrentHashMap<>();

    public LearningRepository() {
        seedTopics();
    }

    private void seedTopics() {
        // 1. Grammar
        save(new LearningTopic(
                "grammar",
                "Grammar & Syntax Mastery",
                "Master core grammatical rules essential for confident, error-free interview speaking.",
                "ENGLISH",
                "📝",
                25,
                List.of(
                        new Lesson(1, "Subject-Verb Agreement in Technical Contexts",
                                "Singular subjects take singular verbs. Watch collective nouns like 'team' or indefinite pronouns like 'each' and 'neither'.",
                                List.of("Singular subjects = singular verb", "Neither/either of the managers takes 'was'", "Data can be treated as singular or plural consistently")),
                        new Lesson(2, "Mastering Tenses: Past vs Present Accomplishments",
                                "Use past tense for completed past projects ('I developed', 'we migrated') and present tense for current responsibilities ('I lead', 'I maintain').",
                                List.of("Maintain tense consistency throughout your STAR story", "Do not switch between 'we built' and 'we are building' randomly"))
                ),
                List.of(
                        "Incorrect: 'Each of the services have their own database.' -> Correct: 'Each of the services has its own database.'",
                        "Incorrect: 'Last year I am working on Redis caching.' -> Correct: 'Last year I worked on Redis caching.'"
                ),
                List.of(
                        new PracticeQuestion(1, "Fix the error: 'The list of microservices were reviewed by the architect.'",
                                "REPHRASE", "The list of microservices was reviewed by the architect.", "The subject is 'The list' (singular), not 'microservices'."),
                        new PracticeQuestion(2, "Convert this sentence to present active voice: 'The pipeline was optimized by our team.'",
                                "REPHRASE", "Our team optimizes the pipeline.", "Active voice creates a stronger impression during interviews.")
                )
        ));

        // 2. Vocabulary
        save(new LearningTopic(
                "vocabulary",
                "High-Impact Professional Vocabulary",
                "Replace casual everyday speech with strong, precise corporate and technical terminology.",
                "ENGLISH",
                "📚",
                20,
                List.of(
                        new Lesson(1, "Eliminating Weak Words: Action Verbs",
                                "Avoid vague phrases like 'I dealt with' or 'I did stuff'. Use strong impact verbs: 'orchestrated', 'streamlined', 'architected', 'remediated'.",
                                List.of("Use precise action verbs at the beginning of each sentence", "Anchor every verb to a tangible business or technical outcome")),
                        new Lesson(2, "Expressing Complexity with Nuance",
                                "Learn how to discuss bottlenecks, tradeoffs, latency constraints, and trade-offs using elevated technical vocabulary.",
                                List.of("Replace 'problem' with 'bottleneck', 'impediment', or 'discrepancy'", "Replace 'good' with 'scalable', 'resilient', or 'performant'"))
                ),
                List.of(
                        "Instead of: 'I helped fix the slow app' -> Use: 'I remediated database latency and streamlined queries.'",
                        "Instead of: 'We had big issues with the client' -> Use: 'We navigated misaligned expectations and achieved consensus.'"
                ),
                List.of(
                        new PracticeQuestion(1, "Replace 'made better' in: 'I made the checkout flow better.'",
                                "VOCABULARY", "I optimized / streamlined / enhanced the checkout flow.", "Using precise verbs demonstrates senior-level clarity.")
                )
        ));

        // 3. Sentence Formation
        save(new LearningTopic(
                "sentence-formation",
                "Sentence Formation & Conciseness",
                "Form structured, punchy sentences without rambling, run-ons, or awkward pauses.",
                "ENGLISH",
                "🧩",
                30,
                List.of(
                        new Lesson(1, "The Rule of One Idea Per Sentence",
                                "Long compound sentences cause hesitation. Deliver one clear message per breath, then pause with confidence.",
                                List.of("Limit sentences to 15-20 words max", "Embrace strategic silence over filler words like 'um' and 'basically'")),
                        new Lesson(2, "Signposting & Transitional Connectors",
                                "Use transitional signposts to guide the interviewer: 'First, let us examine the data schema; second, the caching layer; finally, the API contracts.'",
                                List.of("Signposts give the illusion of effortless organization", "Helps interviewers take structured notes during your answer"))
                ),
                List.of(
                        "Example signposting: 'There are two primary reasons why I chose PostgreSQL. First, ... Second, ...'",
                        "Example contrast: 'While MongoDB offered schema flexibility, PostgreSQL provided the strict transaction guarantees we required.'"
                ),
                List.of(
                        new PracticeQuestion(1, "Break down this run-on sentence into two clear statements: 'I noticed the memory leak and I checked the heap dumps and found the unclosed connections.'",
                                "REPHRASE", "I noticed a memory leak during load testing. By analyzing heap dumps, I identified several unclosed database connections.", "Two punchy sentences sound significantly more authoritative.")
                )
        ));

        // 4. Interview Communication
        save(new LearningTopic(
                "interview-communication",
                "Interview Communication & Tone",
                "Master corporate vocal presence, executive presence, and conversational confidence.",
                "ENGLISH",
                "🎙️",
                25,
                List.of(
                        new Lesson(1, "Executive Presence & Cadence",
                                "Speak at an intentional, moderate tempo (130-150 words per minute). Rushing indicates nervousness; pausing projects mastery.",
                                List.of("Breathe before speaking", "Do not apologize for taking 3 seconds to think")),
                        new Lesson(2, "The STAR Method Architecture",
                                "Every behavioral response must follow Situation (15%), Task (15%), Action (50%), Result (20%).",
                                List.of("Spend half your time detailing YOUR specific actions", "Always quantify the result with numbers or business impact"))
                ),
                List.of(
                        "Sample STAR cadence: 'Situation: Our API response time spiked to 1.8 seconds. Task: Reduce p99 latency under 200ms. Action: Added Redis caching and indexed foreign keys. Result: Latency decreased by 85%.'",
                        "Active clarification: 'Before I proceed into the architecture, would you like me to focus on frontend rendering or backend scalability?'"
                ),
                List.of(
                        new PracticeQuestion(1, "Formulate a quick clarification question for the prompt: 'Design an e-commerce shopping cart.'",
                                "COMMUNICATION", "Are we optimizing for a high-concurrency flash sale or standard persistence across devices?", "Clarifying scope signals senior engineering maturity.")
                )
        ));

        // 5. Common HR Questions
        save(new LearningTopic(
                "common-hr-questions",
                "Common HR Questions & Frameworks",
                "Win over HR and talent partners with authentic, well-crafted, and culturally resonant answers.",
                "ENGLISH",
                "🤝",
                30,
                List.of(
                        new Lesson(1, "Answering 'Tell Me About Yourself'",
                                "Use the Present -> Past -> Future formula. Never simply recite your CV chronologically.",
                                List.of("Present: Current role, scope, and key technical stack", "Past: 1-2 highlight achievements from previous roles", "Future: Why this company matches your immediate trajectory")),
                        new Lesson(2, "Addressing Weaknesses with Growth Mindset",
                                "Share a genuine non-fatal operational weakness, explain the concrete tooling/habit you adopted, and show measurable improvement.",
                                List.of("Avoid cliches like 'I work too hard'", "Highlight a system you put in place to overcome it"))
                ),
                List.of(
                        "Sample Weakness: 'Historically, I tended to dive into code before fully documenting API contracts. To address this, I adopted OpenAPI specs and write API schemas first, which reduced frontend integration cycles by 30%.'",
                        "Sample 'Why this company': 'Your recent migration to distributed event streaming directly aligns with my passion for high-throughput system architecture.'"
                ),
                List.of(
                        new PracticeQuestion(1, "Draft a 30-second opening hook for 'Why should we hire you?'",
                                "HR", "I bring a track record of reducing latency and delivering production systems, paired with a collaborative approach that elevates team delivery speed.", "Combine technical competence with cultural impact.")
                )
        ));

        // 6. Professional Communication
        save(new LearningTopic(
                "professional-communication",
                "Professional & Stakeholder Communication",
                "Learn how to manage cross-functional communication, conflict, and stakeholder alignment.",
                "ENGLISH",
                "💼",
                25,
                List.of(
                        new Lesson(1, "Communicating Trade-offs to Non-Technical Stakeholders",
                                "Translate technical debt and system architecture into business value, risk mitigation, and velocity.",
                                List.of("Frame decisions in terms of time, cost, reliability, and security", "Use intuitive analogies rather than acronym soup")),
                        new Lesson(2, "De-escalating Disagreements in Code Reviews",
                                "Focus on code standards and tests rather than personal opinions. Ask collaborative questions rather than giving directives.",
                                List.of("Say: 'What do you think about using a Set here to avoid O(n^2) nested loops?'", "Avoid: 'Your algorithm is inefficient.'"))
                ),
                List.of(
                        "Analogy for Tech Debt: 'Postponing index optimization now is like deferring vehicle maintenance; it runs today, but breakdowns on the highway cost 5x more.'",
                        "Constructive feedback: 'I like this approach. Could we benchmark the memory footprint against an iterative approach to ensure thread safety?'"
                ),
                List.of(
                        new PracticeQuestion(1, "Rephrase this confrontational statement: 'Your requirement is impossible to build this sprint.'",
                                "REPHRASE", "Given our current sprint commitments and quality benchmarks, we can deliver the core authentication flow this sprint and schedule the analytics export for sprint two.", "Offers proactive solutions instead of flat rejection.")
                )
        ));
    }

    public List<LearningTopic> findAll() {
        return new ArrayList<>(topics.values());
    }

    public Optional<LearningTopic> findById(String id) {
        if (id == null) return Optional.empty();
        String norm = id.toLowerCase().trim();
        return Optional.ofNullable(topics.get(norm));
    }

    public LearningTopic save(LearningTopic topic) {
        topics.put(topic.getId().toLowerCase().trim(), topic);
        return topic;
    }
}
