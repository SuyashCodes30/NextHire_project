package com.nexthire.service;

import com.nexthire.dto.response.InterviewResultResponse;
import com.nexthire.dto.response.QuestionEvaluationDto;
import com.nexthire.model.InterviewResponse;
import com.nexthire.model.Question;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AnswerEvaluationService — Evaluates candidate answers to interview questions.
 *
 * Verifies whether each answer is Right (Correct), Partially Correct, or Incorrect (Not Right),
 * assigns accurate scores (0-100), extracts covered and missing concepts, and computes
 * the overall interview result strictly according to the correctness of all questions.
 */
@Service
public class AnswerEvaluationService {

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and",
            "any", "are", "as", "at", "be", "because", "been", "before", "being", "below",
            "between", "both", "but", "by", "can", "did", "do", "does", "doing", "don",
            "down", "during", "each", "few", "for", "from", "further", "had", "has", "have",
            "having", "he", "her", "here", "hers", "herself", "him", "himself", "his", "how",
            "i", "if", "in", "into", "is", "it", "its", "itself", "just", "me", "more", "most",
            "my", "myself", "no", "nor", "not", "now", "of", "off", "on", "once", "only", "or",
            "other", "our", "ours", "ourselves", "out", "over", "own", "s", "same", "she",
            "should", "so", "some", "such", "t", "than", "that", "the", "their", "theirs",
            "them", "themselves", "then", "there", "these", "they", "this", "those", "through",
            "to", "too", "under", "until", "up", "very", "was", "we", "were", "what", "when",
            "where", "which", "while", "who", "whom", "why", "will", "with", "would", "you", "your"
    );

    /**
     * Evaluates a single answer against a specific Question.
     */
    public QuestionEvaluationDto evaluateSingleAnswer(Question question, String answerText,
                                                      int questionNumber, Integer durationSecs,
                                                      Integer wordCount) {
        String cleanAnswer = (answerText != null) ? answerText.trim() : "";
        int words = (wordCount != null && wordCount > 0)
                ? wordCount
                : (cleanAnswer.isEmpty() ? 0 : cleanAnswer.split("\\s+").length);

        QuestionEvaluationDto eval = new QuestionEvaluationDto();
        eval.setQuestionId(question != null ? question.getId() : null);
        eval.setQuestionNumber(questionNumber);
        eval.setQuestionText(question != null ? question.getText() : "Question");
        eval.setCategory(question != null ? question.getCategory() : "General");
        eval.setTopic(question != null ? question.getTopic() : "General");
        eval.setDifficulty(question != null ? question.getDifficulty() : "MEDIUM");
        eval.setUserAnswer(cleanAnswer.isEmpty() ? "(No answer submitted)" : cleanAnswer);
        eval.setExpectedAnswer(question != null && question.getExpectedAnswer() != null
                ? question.getExpectedAnswer()
                : "A clear, well-structured explanation covering key technical trade-offs and real-world implementation.");
        eval.setSubtopic(question != null ? question.getSubtopic() : null);
        eval.setCorrectAnswer(question != null && question.getCorrectAnswer() != null
                ? question.getCorrectAnswer()
                : (question != null ? question.getExpectedAnswer() : ""));
        eval.setExplanation(question != null ? question.getExplanation() : null);
        if (question != null && question.getKeyPoints() != null) {
            eval.setKeyPoints(new ArrayList<>(question.getKeyPoints()));
        }
        if (question != null && question.getLearningResources() != null) {
            eval.setLearningResources(new ArrayList<>(question.getLearningResources()));
        }

        // 1. Empty or near-empty response
        if (cleanAnswer.isEmpty() || words < 2) {
            eval.setScore(0);
            eval.setCorrect(false);
            eval.setStatus("INCORRECT");
            eval.setStatusLabel("Incorrect / Not Right ✗");
            eval.setFeedback("No answer was provided. Review the expected answer below to master the core concepts.");
            eval.getMissingConcepts().add("Core explanation was omitted.");
            return eval;
        }

        // 2. Gibberish or random keyboard spam detection
        if (isGibberish(cleanAnswer)) {
            eval.setScore(5);
            eval.setCorrect(false);
            eval.setStatus("INCORRECT");
            eval.setStatusLabel("Incorrect / Not Right ✗");
            eval.setFeedback("The response contains random or unreadable characters. Please provide an intelligible explanation.");
            eval.getMissingConcepts().add("Intelligible response structure.");
            return eval;
        }

        // 3. Very brief answer (< 4 words)
        if (words < 4) {
            // Check if it's a specific short answer (e.g. grammar correction "was")
            boolean isGrammarQ = question != null && question.getText() != null
                    && question.getText().toLowerCase().contains("neither of the project managers were");
            if (isGrammarQ && cleanAnswer.toLowerCase().contains("was")) {
                eval.setScore(95);
                eval.setCorrect(true);
                eval.setStatus("CORRECT");
                eval.setStatusLabel("Right / Correct ✓");
                eval.setFeedback("Correct! 'Neither' is singular and requires the singular verb 'was' rather than 'were'.");
                eval.getCoveredConcepts().add("Corrected 'were' to singular 'was'");
                return eval;
            }

            eval.setScore(15);
            eval.setCorrect(false);
            eval.setStatus("INCORRECT");
            eval.setStatusLabel("Incorrect / Not Right ✗");
            eval.setFeedback("Response is too brief to demonstrate adequate understanding. Elaborate on the mechanism with examples.");
            eval.getMissingConcepts().add("Sufficient explanation and technical detail.");
            return eval;
        }

        // 4. Detailed question-specific evaluation based on known interview concepts
        QuestionEvaluationDto specialized = evaluateSpecializedConcept(question, cleanAnswer, eval);
        if (specialized != null) {
            return specialized;
        }

        // 5. Generalized semantic & concept keyword evaluation
        return evaluateGeneralizedSemantic(question, cleanAnswer, words, eval);
    }

    /**
     * Checks known benchmark interview questions for high-precision grading.
     */
    private QuestionEvaluationDto evaluateSpecializedConcept(Question question, String answer, QuestionEvaluationDto eval) {
        if (question == null || question.getText() == null) return null;
        String qText = question.getText().toLowerCase();
        String ansLower = answer.toLowerCase();

        // Q: WHERE vs HAVING in SQL
        if (qText.contains("where") && qText.contains("having")) {
            boolean hasWhereConcept = (ansLower.contains("before") || ansLower.contains("row") || ansLower.contains("pre-aggregation"))
                    && ansLower.contains("where");
            boolean hasHavingConcept = (ansLower.contains("after") || ansLower.contains("group") || ansLower.contains("aggregate"))
                    && ansLower.contains("having");

            if (hasWhereConcept && hasHavingConcept) {
                eval.setScore(92);
                eval.setCorrect(true);
                eval.setStatus("CORRECT");
                eval.setStatusLabel("Right / Correct ✓");
                eval.setFeedback("Spot-on! You accurately highlighted that WHERE filters rows before aggregation occurs and HAVING filters grouped results after GROUP BY.");
                eval.getCoveredConcepts().add("WHERE filters rows before aggregation");
                eval.getCoveredConcepts().add("HAVING filters groups after GROUP BY");
                return eval;
            } else if (hasWhereConcept || hasHavingConcept) {
                eval.setScore(60);
                eval.setCorrect(false);
                eval.setStatus("PARTIALLY_CORRECT");
                eval.setStatusLabel("Partially Correct ~");
                eval.setFeedback("Partially correct. You explained one clause well, but missed emphasizing both pre-aggregation row filtering (WHERE) and post-GROUP BY aggregate filtering (HAVING).");
                if (hasWhereConcept) eval.getCoveredConcepts().add("WHERE pre-aggregation filtering");
                else eval.getMissingConcepts().add("WHERE pre-aggregation row filtering");
                if (hasHavingConcept) eval.getCoveredConcepts().add("HAVING post-aggregation filtering");
                else eval.getMissingConcepts().add("HAVING post-aggregation filtering");
                return eval;
            } else {
                eval.setScore(25);
                eval.setCorrect(false);
                eval.setStatus("INCORRECT");
                eval.setStatusLabel("Incorrect / Not Right ✗");
                eval.setFeedback("Incorrect. WHERE filters individual rows before aggregation occurs, while HAVING is specifically used to filter groups produced by GROUP BY.");
                eval.getMissingConcepts().add("Pre-aggregation vs post-aggregation filtering distinction");
                return eval;
            }
        }

        // Q: HashMap vs TreeMap in Java
        if (qText.contains("hashmap") && qText.contains("treemap")) {
            boolean hasHashMap = ansLower.contains("hash") || ansLower.contains("bucket") || ansLower.contains("o(1)") || ansLower.contains("constant");
            boolean hasTreeMap = ansLower.contains("tree") || ansLower.contains("red-black") || ansLower.contains("sorted") || ansLower.contains("order") || ansLower.contains("o(log");

            if (hasHashMap && hasTreeMap) {
                eval.setScore(95);
                eval.setCorrect(true);
                eval.setStatus("CORRECT");
                eval.setStatusLabel("Right / Correct ✓");
                eval.setFeedback("Excellent! You clearly identified that HashMap provides O(1) hash bucket lookup while TreeMap maintains sorted keys using a Red-Black Tree in O(log n) time.");
                eval.getCoveredConcepts().add("HashMap O(1) hashing / bucket mechanism");
                eval.getCoveredConcepts().add("TreeMap Red-Black Tree / sorted order");
                return eval;
            } else if (hasHashMap || hasTreeMap) {
                eval.setScore(58);
                eval.setCorrect(false);
                eval.setStatus("PARTIALLY_CORRECT");
                eval.setStatusLabel("Partially Correct ~");
                eval.setFeedback("Partially correct. You addressed one collection's structure, but compare both (O(1) hash table vs O(log n) sorted Red-Black tree).");
                if (hasHashMap) eval.getCoveredConcepts().add("HashMap hash-based structure");
                else eval.getMissingConcepts().add("HashMap hashing mechanism");
                if (hasTreeMap) eval.getCoveredConcepts().add("TreeMap sorted tree ordering");
                else eval.getMissingConcepts().add("TreeMap Red-Black Tree sorting");
                return eval;
            }
        }

        // Q: Second highest salary in SQL
        if (qText.contains("second highest") || (qText.contains("salary") && qText.contains("window"))) {
            boolean hasWindowFn = ansLower.contains("dense_rank") || ansLower.contains("rank()") || ansLower.contains("row_number");
            boolean hasOver = ansLower.contains("over") && ansLower.contains("order by");
            if (hasWindowFn || (hasOver && ansLower.contains("salary"))) {
                eval.setScore(90);
                eval.setCorrect(true);
                eval.setStatus("CORRECT");
                eval.setStatusLabel("Right / Correct ✓");
                eval.setFeedback("Correct! Using DENSE_RANK() OVER (ORDER BY salary DESC) in a CTE or subquery accurately handles ties and identifies the second highest salary.");
                eval.getCoveredConcepts().add("Window ranking function (DENSE_RANK)");
                eval.getCoveredConcepts().add("Descending salary ordering with subquery filter");
                return eval;
            }
        }

        // Q: Grammar - Neither of the project managers were
        if (qText.contains("neither of the project managers were")) {
            if (ansLower.contains("was") && !ansLower.contains("were available")) {
                eval.setScore(96);
                eval.setCorrect(true);
                eval.setStatus("CORRECT");
                eval.setStatusLabel("Right / Correct ✓");
                eval.setFeedback("Right! 'Neither' is an indefinite pronoun that takes a singular verb: 'Neither of the project managers was available'.");
                eval.getCoveredConcepts().add("Subject-verb agreement: singular 'was'");
                return eval;
            } else {
                eval.setScore(20);
                eval.setCorrect(false);
                eval.setStatus("INCORRECT");
                eval.setStatusLabel("Incorrect / Not Right ✗");
                eval.setFeedback("Incorrect. The grammatical error is 'were'. 'Neither' is singular, so it must be 'was available'.");
                eval.getMissingConcepts().add("Identification of singular subject 'Neither' requiring 'was'");
                return eval;
            }
        }

        // Q: Python GIL
        if (qText.contains("gil") || (qText.contains("global interpreter lock") && qText.contains("python"))) {
            boolean hasGilCore = ansLower.contains("thread") && (ansLower.contains("one") || ansLower.contains("single") || ansLower.contains("lock") || ansLower.contains("cpu"));
            boolean hasBypass = ansLower.contains("multiprocess") || ansLower.contains("c extension") || ansLower.contains("process");
            if (hasGilCore) {
                int s = hasBypass ? 95 : 82;
                eval.setScore(s);
                eval.setCorrect(true);
                eval.setStatus("CORRECT");
                eval.setStatusLabel("Right / Correct ✓");
                eval.setFeedback("Strong explanation! You described how the GIL prevents native threads from executing bytecodes concurrently and its bottleneck on CPU-bound workloads.");
                eval.getCoveredConcepts().add("GIL concurrency lock mechanism");
                if (hasBypass) eval.getCoveredConcepts().add("Multiprocessing / native bypass");
                else eval.getMissingConcepts().add("Mentioning multiprocessing or C extensions as bypass");
                return eval;
            }
        }

        // Q: Liskov Substitution Principle (LSP) / SOLID
        if (qText.contains("liskov") || (qText.contains("solid") && qText.contains("substitut"))) {
            boolean hasLsp = ansLower.contains("substitut") || ansLower.contains("subtype") || ansLower.contains("subclass") || ansLower.contains("square") || ansLower.contains("rectangle");
            if (hasLsp) {
                eval.setScore(90);
                eval.setCorrect(true);
                eval.setStatus("CORRECT");
                eval.setStatusLabel("Right / Correct ✓");
                eval.setFeedback("Great answer! You captured the essence of LSP: derived types must be completely substitutable for their base types without altering system correctness.");
                eval.getCoveredConcepts().add("Subtype substitutability invariant");
                return eval;
            }
        }

        // Q: Correlation vs Causation
        if (qText.contains("correlation") && qText.contains("causation")) {
            boolean hasDiff = (ansLower.contains("cause") || ansLower.contains("direct") || ansLower.contains("influence"))
                    && (ansLower.contains("together") || ansLower.contains("relationship") || ansLower.contains("move") || ansLower.contains("associate"));
            if (hasDiff) {
                eval.setScore(90);
                eval.setCorrect(true);
                eval.setStatus("CORRECT");
                eval.setStatusLabel("Right / Correct ✓");
                eval.setFeedback("Very clear distinction! You identified that correlation is an observed statistical relationship, whereas causation requires one variable directly altering another.");
                eval.getCoveredConcepts().add("Statistical relationship vs direct causal link");
                return eval;
            }
        }

        // Q: INDEX-MATCH vs VLOOKUP
        if (qText.contains("index-match") || (qText.contains("vlookup") && qText.contains("excel"))) {
            boolean hasAdv = ansLower.contains("left") || ansLower.contains("insert") || ansLower.contains("faster") || ansLower.contains("performance") || ansLower.contains("column");
            if (hasAdv) {
                eval.setScore(92);
                eval.setCorrect(true);
                eval.setStatus("CORRECT");
                eval.setStatusLabel("Right / Correct ✓");
                eval.setFeedback("Accurate! INDEX-MATCH looks to the left of the lookup column, does not break upon column insertion, and processes large spreadsheets faster.");
                eval.getCoveredConcepts().add("Leftward lookup capability");
                eval.getCoveredConcepts().add("Robustness against column insertion");
                return eval;
            }
        }

        return null;
    }

    /**
     * Generalized concept & semantic keyword evaluator for any interview question.
     */
    private QuestionEvaluationDto evaluateGeneralizedSemantic(Question question, String answer,
                                                               int words, QuestionEvaluationDto eval) {
        String expected = question != null && question.getExpectedAnswer() != null
                ? question.getExpectedAnswer()
                : "";
        String topic = question != null && question.getTopic() != null ? question.getTopic() : "";
        String tags = question != null && question.getTags() != null ? question.getTags() : "";

        // Extract key terms from expected answer, topic, tags
        List<String> expectedKeywords = extractKeywords(expected + " " + topic + " " + tags);
        List<String> answerKeywords = extractKeywords(answer);

        Set<String> ansSet = new HashSet<>(answerKeywords);
        Set<String> ansStemSet = answerKeywords.stream().map(AnswerEvaluationService::stem).collect(Collectors.toSet());
        String ansLower = answer.toLowerCase();

        List<String> covered = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String kw : expectedKeywords) {
            String kwStem = stem(kw);
            boolean matches = ansSet.contains(kw)
                    || ansLower.contains(kw)
                    || ansStemSet.contains(kwStem);

            if (matches) {
                if (!covered.contains(kw)) covered.add(kw);
            } else {
                if (!missing.contains(kw) && missing.size() < 4) missing.add(kw);
            }
        }

        eval.setCoveredConcepts(covered.stream().limit(5).collect(Collectors.toList()));
        eval.setMissingConcepts(missing.stream().limit(4).collect(Collectors.toList()));

        double overlapRatio = expectedKeywords.isEmpty() ? 0.5 : (double) covered.size() / expectedKeywords.size();

        // Technical accuracy & depth calculation
        int baseScore = (int) Math.round(overlapRatio * 70);
        int depthBonus = Math.min(22, words >= 25 ? 20 : (words / 2));

        // Structured articulation bonus
        boolean hasExplanationWords = Pattern.compile("\\b(because|for example|such as|whereas|in contrast|therefore|ensures|improves|specifically|trade-off|furthermore|consequently|as a result)\\b",
                Pattern.CASE_INSENSITIVE).matcher(answer).find();
        int articulateBonus = hasExplanationWords ? 8 : 0;

        // Technical precision bonus (presence of programming / architectural keywords)
        boolean hasTechTerms = Pattern.compile("\\b(complexity|o\\(1\\)|o\\(n\\)|memory|latency|database|query|table|index|cache|api|http|rest|schema|interface|class|thread|async|null|exception)\\b",
                Pattern.CASE_INSENSITIVE).matcher(answer).find();
        int techBonus = hasTechTerms ? 5 : 0;

        // Hesitation penalty
        boolean hasHesitation = Pattern.compile("\\b(i don't know|not sure|i think maybe|i guess|probably perhaps|kind of|sort of)\\b",
                Pattern.CASE_INSENSITIVE).matcher(answer).find();
        int hesitationPenalty = hasHesitation ? 10 : 0;

        int finalScore = Math.min(98, Math.max(10, baseScore + depthBonus + articulateBonus + techBonus - hesitationPenalty));
        eval.setScore(finalScore);

        if (finalScore >= 70 || (overlapRatio >= 0.40 && words >= 18)) {
            eval.setCorrect(true);
            eval.setStatus("CORRECT");
            eval.setStatusLabel("Right / Correct ✓");
            eval.setFeedback("Great answer! You covered the primary concepts and articulated the core technical ideas effectively.");
        } else if (finalScore >= 42 || overlapRatio >= 0.22) {
            eval.setCorrect(false);
            eval.setStatus("PARTIALLY_CORRECT");
            eval.setStatusLabel("Partially Correct ~");
            eval.setFeedback("Partially correct. You touched on relevant terminology, but missed key technical nuances outlined in the model solution.");
        } else {
            eval.setCorrect(false);
            eval.setStatus("INCORRECT");
            eval.setStatusLabel("Incorrect / Not Right ✗");
            eval.setFeedback("Needs improvement. The response lacks the required conceptual depth and key principles for this topic. Review the expected answer below.");
        }

        return eval;
    }

    /**
     * Generates the comprehensive interview result derived from the evaluations of all questions.
     */
    public InterviewResultResponse generateInterviewResult(Long interviewId,
                                                           List<Question> questions,
                                                           List<InterviewResponse> responses,
                                                           String category, String role) {
        InterviewResultResponse result = new InterviewResultResponse();
        result.setInterviewId(interviewId);

        // Map questionId -> Question
        Map<Long, Question> qMap = new HashMap<>();
        if (questions != null) {
            for (Question q : questions) {
                if (q.getId() != null) qMap.put(q.getId(), q);
            }
        }

        List<QuestionEvaluationDto> evaluations = new ArrayList<>();
        int qNum = 1;

        int totalWords = 0;
        int totalArticulateCount = 0;
        int totalHesitationCount = 0;
        int totalStructuredCount = 0;

        // Evaluate all submitted responses
        if (responses != null && !responses.isEmpty()) {
            for (InterviewResponse resp : responses) {
                Question q = (resp.getQuestionId() != null) ? qMap.get(resp.getQuestionId()) : null;
                if (q == null && questions != null && qNum - 1 < questions.size()) {
                    q = questions.get(qNum - 1);
                }

                QuestionEvaluationDto eval = evaluateSingleAnswer(
                        q,
                        resp.getResponseText(),
                        qNum++,
                        resp.getDurationSecs(),
                        resp.getWordCount()
                );
                evaluations.add(eval);

                // Also update the response object with evaluation
                resp.setScore(eval.getScore());
                resp.setIsCorrect(eval.isCorrect());
                resp.setStatus(eval.getStatus());
                resp.setStatusLabel(eval.getStatusLabel());
                resp.setFeedback(eval.getFeedback());
                resp.setExpectedAnswer(eval.getExpectedAnswer());

                if (resp.getResponseText() != null) {
                    String txt = resp.getResponseText();
                    int w = resp.getWordCount() != null && resp.getWordCount() > 0 ? resp.getWordCount() : txt.split("\\s+").length;
                    totalWords += w;
                    if (Pattern.compile("\\b(because|for example|specifically|in contrast|therefore|ensures|furthermore|as a result)\\b", Pattern.CASE_INSENSITIVE).matcher(txt).find()) {
                        totalArticulateCount++;
                    }
                    if (Pattern.compile("\\b(i don't know|not sure|i think maybe|i guess|probably perhaps)\\b", Pattern.CASE_INSENSITIVE).matcher(txt).find()) {
                        totalHesitationCount++;
                    }
                    if (Pattern.compile("(^|\\n)\\s*([0-9]+\\.|-|\\*)|\\b(first|second|finally|situation|task|action|result)\\b", Pattern.CASE_INSENSITIVE).matcher(txt).find()) {
                        totalStructuredCount++;
                    }
                }
            }
        }

        // If there were questions with no responses submitted
        if (questions != null && evaluations.size() < questions.size()) {
            for (int i = evaluations.size(); i < questions.size(); i++) {
                Question q = questions.get(i);
                QuestionEvaluationDto eval = evaluateSingleAnswer(q, "", i + 1, 0, 0);
                evaluations.add(eval);
            }
        }

        result.setQuestionEvaluations(evaluations);
        int totalQ = evaluations.isEmpty() ? 1 : evaluations.size();
        result.setTotalQuestions(totalQ);

        // Count Right vs Partial vs Not Right
        int correctCount = 0;
        int partialCount = 0;
        int incorrectCount = 0;
        int totalScoreSum = 0;

        for (QuestionEvaluationDto e : evaluations) {
            totalScoreSum += e.getScore();
            if ("CORRECT".equals(e.getStatus())) {
                correctCount++;
            } else if ("PARTIALLY_CORRECT".equals(e.getStatus())) {
                partialCount++;
            } else {
                incorrectCount++;
            }
        }

        result.setCorrectCount(correctCount);
        result.setPartiallyCorrectCount(partialCount);
        result.setIncorrectCount(incorrectCount);

        // Accuracy Percentage: Correct gives 100%, Partial gives 50%, Incorrect gives 0%
        int accuracy = Math.round(((correctCount * 100.0f) + (partialCount * 50.0f)) / totalQ);
        result.setAccuracyPercentage(accuracy);

        int avgWordsPerQ = totalQ > 0 ? totalWords / totalQ : 0;
        int avgQuestionScore = Math.round((float) totalScoreSum / totalQ);

        // Calculate 5 core skill pillars according to performance
        int technicalScore = Math.min(98, Math.max(25, avgQuestionScore));
        int commBase = Math.min(95, Math.max(30, (int) Math.round(avgQuestionScore * 0.75 + (avgWordsPerQ >= 18 ? 15 : avgWordsPerQ) + (totalArticulateCount * 3))));
        int communicationScore = Math.min(96, Math.max(35, commBase));
        int confBase = (int) Math.round(accuracy * 0.80 + 15) - (totalHesitationCount * 5);
        int confidenceScore = Math.min(96, Math.max(30, confBase));
        int clarityScore = Math.min(96, Math.max(35, (int) Math.round((technicalScore * 0.5) + (communicationScore * 0.4) + (correctCount * 2))));
        int structBonus = Math.min(20, totalStructuredCount * 5);
        int answerStructureScore = Math.min(96, Math.max(35, (int) Math.round(avgQuestionScore * 0.70 + 10 + structBonus)));

        // Composite overall score
        int overallScore = Math.min(98, Math.max(15, Math.round(
                (technicalScore * 0.40f) +
                (communicationScore * 0.20f) +
                (clarityScore * 0.15f) +
                (answerStructureScore * 0.15f) +
                (confidenceScore * 0.10f)
        )));

        result.setOverallScore(overallScore);
        result.setTechnicalScore(technicalScore);
        result.setClarityScore(clarityScore);
        result.setConfidenceScore(confidenceScore);
        result.setCommunicationScore(communicationScore);
        result.setAnswerStructureScore(answerStructureScore);

        // Dynamic readiness label
        if (overallScore >= 88) {
            result.setReadinessLabel("Interview Ready — High Probability of Offer 🔥");
        } else if (overallScore >= 75) {
            result.setReadinessLabel("Interview Capable — Strong Foundation 🎯");
        } else if (overallScore >= 55) {
            result.setReadinessLabel("Developing — Targeted Practice Recommended 📚");
        } else {
            result.setReadinessLabel("Foundational Review Required ⚠️");
        }

        // Dynamic strengths from correct questions
        List<String> strengths = new ArrayList<>();
        for (QuestionEvaluationDto e : evaluations) {
            if ("CORRECT".equals(e.getStatus()) && strengths.size() < 3) {
                strengths.add("Mastered " + e.getTopic() + ": Strong explanation on '" + truncate(e.getQuestionText(), 45) + "'");
            }
        }
        if (strengths.isEmpty()) {
            strengths.add("Completed all interview questions under timed simulated pressure.");
            strengths.add("Demonstrated baseline familiarity with the interview subject.");
        }
        result.setStrengths(strengths);

        // Dynamic improvements from incorrect / partial questions
        List<String> improvements = new ArrayList<>();
        for (QuestionEvaluationDto e : evaluations) {
            if (!"CORRECT".equals(e.getStatus()) && improvements.size() < 3) {
                String missingStr = (!e.getMissingConcepts().isEmpty()) ? ": " + String.join(", ", e.getMissingConcepts()) : "";
                improvements.add("Review " + e.getTopic() + " on '" + truncate(e.getQuestionText(), 40) + "'" + missingStr);
            }
        }
        if (improvements.isEmpty()) {
            improvements.add("Incorporate quantified metrics and production scale examples to elevate answers from good to exceptional.");
        }
        result.setImprovements(improvements);

        // STAR evaluation summary
        result.setStarEvaluation(String.format(
                "Evaluated %d questions: %d Right / Correct, %d Partially Correct, %d Incorrect. " +
                "Action and technical details were strongest in %s topics.",
                totalQ, correctCount, partialCount, incorrectCount,
                category != null ? category : "assigned"
        ));

        // Overall summary paragraph
        result.setOverallFeedback(String.format(
                "You answered %d out of %d questions correctly with an overall accuracy of %d%%. " +
                (overallScore >= 80
                        ? "Your technical grasp is solid, demonstrating high competency and clear communication. Ready for real company rounds!"
                        : overallScore >= 60
                        ? "You show good working knowledge, but need deeper precision on missed concepts. Review the question analysis below."
                        : "Significant conceptual gaps were identified. Focus on reviewing the expected answers and retry the category."),
                correctCount, totalQ, accuracy
        ));

        // Topic Mastery Analysis & Learning Recommendations
        Map<String, List<Integer>> topicScores = new LinkedHashMap<>();
        Map<String, List<com.nexthire.model.LearningResource>> topicResources = new LinkedHashMap<>();

        for (QuestionEvaluationDto e : evaluations) {
            String topicName = (e.getTopic() != null && !e.getTopic().isBlank()) ? e.getTopic() : "General";
            topicScores.computeIfAbsent(topicName, k -> new ArrayList<>()).add(e.getScore());
            if (e.getLearningResources() != null && !e.getLearningResources().isEmpty()) {
                topicResources.computeIfAbsent(topicName, k -> new ArrayList<>()).addAll(e.getLearningResources());
            }
        }

        List<String> topicsCovered = new ArrayList<>(topicScores.keySet());
        List<String> strongTopics = new ArrayList<>();
        List<String> weakTopics = new ArrayList<>();
        List<com.nexthire.model.LearningResource> recommendedLearning = new ArrayList<>();
        Set<String> seenResourceTitles = new HashSet<>();

        for (Map.Entry<String, List<Integer>> entry : topicScores.entrySet()) {
            double avg = entry.getValue().stream().mapToInt(Integer::intValue).average().orElse(0.0);
            if (avg >= 70.0) {
                strongTopics.add(entry.getKey());
            } else {
                weakTopics.add(entry.getKey());
                // Add learning resources from weak topics first
                List<com.nexthire.model.LearningResource> res = topicResources.get(entry.getKey());
                if (res != null) {
                    for (com.nexthire.model.LearningResource r : res) {
                        if (r.getTitle() != null && seenResourceTitles.add(r.getTitle().toLowerCase())) {
                            recommendedLearning.add(r);
                        }
                    }
                }
            }
        }

        // If recommended learning is still short, also include resources from other topics
        for (Map.Entry<String, List<com.nexthire.model.LearningResource>> entry : topicResources.entrySet()) {
            for (com.nexthire.model.LearningResource r : entry.getValue()) {
                if (r.getTitle() != null && seenResourceTitles.add(r.getTitle().toLowerCase())) {
                    recommendedLearning.add(r);
                }
            }
        }

        result.setTopicsCovered(topicsCovered);
        result.setStrongTopics(strongTopics);
        result.setWeakTopics(weakTopics);
        result.setRecommendedLearning(recommendedLearning);

        return result;
    }

    private List<String> extractKeywords(String text) {
        if (text == null) return List.of();
        String[] tokens = text.toLowerCase().replaceAll("[^a-z0-9_\\-\\s]", " ").split("\\s+");
        List<String> list = new ArrayList<>();
        for (String t : tokens) {
            t = t.trim();
            if (t.length() >= 3 && !STOP_WORDS.contains(t) && !list.contains(t)) {
                list.add(t);
            }
        }
        return list;
    }

    private boolean isGibberish(String s) {
        if (s == null) return false;
        // Repeated single char 5+ times: e.g. "aaaaa", "zzzzz"
        if (Pattern.compile("(.)\\1{4,}").matcher(s).find()) return true;
        // Common keyboard rows spam
        String lower = s.toLowerCase().replaceAll("\\s+", "");
        if (lower.contains("asdfgh") || lower.contains("qwerty") || lower.contains("zxcvbn") || lower.contains("123456")) {
            return true;
        }
        return false;
    }

    public static String stem(String word) {
        if (word == null || word.length() <= 3) return word == null ? "" : word.toLowerCase();
        String w = word.toLowerCase().trim();
        if (w.endsWith("ies") && w.length() > 4) return w.substring(0, w.length() - 3) + "y";
        if (w.endsWith("ing") && w.length() > 5) return w.substring(0, w.length() - 3);
        if (w.endsWith("tion") && w.length() > 5) return w.substring(0, w.length() - 4);
        if (w.endsWith("sion") && w.length() > 5) return w.substring(0, w.length() - 4);
        if (w.endsWith("ment") && w.length() > 5) return w.substring(0, w.length() - 4);
        if (w.endsWith("ness") && w.length() > 5) return w.substring(0, w.length() - 4);
        if (w.endsWith("able") && w.length() > 5) return w.substring(0, w.length() - 4);
        if (w.endsWith("ible") && w.length() > 5) return w.substring(0, w.length() - 4);
        if (w.endsWith("ity") && w.length() > 5) return w.substring(0, w.length() - 3);
        if (w.endsWith("ized") && w.length() > 5) return w.substring(0, w.length() - 4);
        if (w.endsWith("izes") && w.length() > 5) return w.substring(0, w.length() - 4);
        if (w.endsWith("ize") && w.length() > 5) return w.substring(0, w.length() - 3);
        if (w.endsWith("ed") && w.length() > 4) return w.substring(0, w.length() - 2);
        if (w.endsWith("es") && w.length() > 4) return w.substring(0, w.length() - 2);
        if (w.endsWith("s") && !w.endsWith("ss") && w.length() > 3) return w.substring(0, w.length() - 1);
        return w;
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }
}
