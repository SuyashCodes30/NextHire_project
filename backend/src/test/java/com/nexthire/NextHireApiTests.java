package com.nexthire;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexthire.dto.request.CreateInterviewRequest;
import com.nexthire.dto.request.LoginRequest;
import com.nexthire.dto.request.RegisterRequest;
import com.nexthire.dto.request.SubmitResponseRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * NextHireApiTests — Integration tests for all REST API endpoints.
 *
 * These tests use MockMvc to test the full Spring MVC stack.
 * No database or external services required — uses mock in-memory data.
 *
 * Run all tests with: mvn test
 * Or run this class in IntelliJ IDEA.
 */
@SpringBootTest
@AutoConfigureMockMvc
class NextHireApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ================================================================
    // AUTH ENDPOINT TESTS
    // ================================================================

    @Test
    @DisplayName("POST /api/auth/register — Should register a new user")
    void testRegisterUser() throws Exception {
        RegisterRequest request = new RegisterRequest("Test", "User", "testuser@nexthire.ai", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("testuser@nexthire.ai"))
                .andExpect(jsonPath("$.data.firstName").value("Test"))
                .andExpect(jsonPath("$.status").value(201));
    }

    @Test
    @DisplayName("POST /api/auth/register — Should reject invalid email")
    void testRegisterWithInvalidEmail() throws Exception {
        RegisterRequest request = new RegisterRequest("Test", "User", "not-an-email", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/register — Should reject short password")
    void testRegisterWithShortPassword() throws Exception {
        RegisterRequest request = new RegisterRequest("Test", "User", "test@test.com", "123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.password").exists());
    }

    @Test
    @DisplayName("POST /api/auth/login — Should login with demo user credentials")
    void testLoginWithDemoUser() throws Exception {
        LoginRequest request = new LoginRequest("demo@nexthire.ai", "anypassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user.email").value("demo@nexthire.ai"));
    }

    @Test
    @DisplayName("POST /api/auth/login — Should reject wrong email")
    void testLoginWithWrongEmail() throws Exception {
        LoginRequest request = new LoginRequest("nonexistent@test.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ================================================================
    // QUESTION ENDPOINT TESTS
    // ================================================================

    @Test
    @DisplayName("GET /api/questions — Should return all questions")
    void testGetAllQuestions() throws Exception {
        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("GET /api/questions?category=TECHNICAL — Should filter by category")
    void testGetQuestionsByCategory() throws Exception {
        mockMvc.perform(get("/api/questions").param("category", "TECHNICAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].category").value("TECHNICAL"));
    }

    @Test
    @DisplayName("GET /api/questions/{id} — Should return question by ID")
    void testGetQuestionById() throws Exception {
        mockMvc.perform(get("/api/questions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("GET /api/questions/9999 — Should return 404 for unknown ID")
    void testGetQuestionByInvalidId() throws Exception {
        mockMvc.perform(get("/api/questions/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404));
    }

    // ================================================================
    // INTERVIEW ENDPOINT TESTS
    // ================================================================

    @Test
    @DisplayName("POST /api/interviews — Should create a new interview")
    void testCreateInterview() throws Exception {
        CreateInterviewRequest request = new CreateInterviewRequest(
                "TECHNICAL", "Software Engineer", "MEDIUM", 1L
        );

        mockMvc.perform(post("/api/interviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.category").value("TECHNICAL"))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test
    @DisplayName("POST /api/interviews — Should reject invalid category")
    void testCreateInterviewWithInvalidCategory() throws Exception {
        CreateInterviewRequest request = new CreateInterviewRequest(
                "INVALID_CATEGORY", "Developer", "MEDIUM", 1L
        );

        mockMvc.perform(post("/api/interviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Full interview flow — Create, get questions, submit answer, complete, get result")
    void testFullInterviewFlow() throws Exception {
        ObjectMapper mapper = objectMapper;

        // Step 1: Create interview
        CreateInterviewRequest createRequest = new CreateInterviewRequest(
                "HR", "Product Manager", "EASY", 1L
        );
        MvcResult createResult = mockMvc.perform(post("/api/interviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String createBody = createResult.getResponse().getContentAsString();
        Long interviewId = mapper.readTree(createBody).path("data").path("id").asLong();

        // Step 2: Get questions
        mockMvc.perform(get("/api/interviews/" + interviewId + "/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // Step 3: Submit a response
        SubmitResponseRequest responseRequest = new SubmitResponseRequest(
                1L, "I am a passionate developer with 3 years of experience in Java and Spring Boot.",
                60, 20, null
        );
        mockMvc.perform(post("/api/interviews/" + interviewId + "/responses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(responseRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        // Step 4: Complete the interview
        mockMvc.perform(post("/api/interviews/" + interviewId + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        // Step 5: Get result
        mockMvc.perform(get("/api/interviews/" + interviewId + "/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overallScore").isNumber())
                .andExpect(jsonPath("$.data.communicationScore").isNumber())
                .andExpect(jsonPath("$.data.strengths").isArray())
                .andExpect(jsonPath("$.data.improvements").isArray())
                .andExpect(jsonPath("$.data.questionEvaluations").isArray())
                .andExpect(jsonPath("$.data.totalQuestions").isNumber())
                .andExpect(jsonPath("$.data.accuracyPercentage").isNumber());
    }

    @Test
    @DisplayName("Answer Evaluation — Right vs Not Right answer accuracy and result generation")
    void testAnswerCorrectnessAndResultCalculation() throws Exception {
        // Step 1: Create software developer interview
        CreateInterviewRequest createRequest = new CreateInterviewRequest(
                "software-developer", "Backend Engineer", "EASY", 1L
        );
        MvcResult createResult = mockMvc.perform(post("/api/interviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long interviewId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // Step 2: Fetch questions
        MvcResult qResult = mockMvc.perform(get("/api/interviews/" + interviewId + "/questions"))
                .andExpect(status().isOk())
                .andReturn();

        var qNode = objectMapper.readTree(qResult.getResponse().getContentAsString()).path("data");
        Long q1Id = qNode.get(0).path("id").asLong();
        String q1Text = qNode.get(0).path("text").asText();

        // Step 3: Submit a comprehensive, correct answer
        String rightAnswer = "HashMap uses an array of buckets with linked lists and red-black trees providing O(1) average lookup time. TreeMap is built using a Red-Black Tree and maintains sorted key order in O(log n) time.";
        mockMvc.perform(post("/api/interviews/" + interviewId + "/responses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SubmitResponseRequest(
                                q1Id, rightAnswer, 45, 30, null
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.score").isNumber())
                .andExpect(jsonPath("$.data.isCorrect").exists());

        // Step 4: Complete interview and check question-by-question result
        mockMvc.perform(post("/api/interviews/" + interviewId + "/complete"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/interviews/" + interviewId + "/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.questionEvaluations").isArray())
                .andExpect(jsonPath("$.data.questionEvaluations[0].userAnswer").isNotEmpty())
                .andExpect(jsonPath("$.data.questionEvaluations[0].expectedAnswer").isNotEmpty())
                .andExpect(jsonPath("$.data.questionEvaluations[0].status").isNotEmpty())
                .andExpect(jsonPath("$.data.correctCount").isNumber())
                .andExpect(jsonPath("$.data.readinessLabel").isNotEmpty());
    }

    // ================================================================
    // DASHBOARD ENDPOINT TESTS
    // ================================================================

    @Test
    @DisplayName("GET /api/dashboard — Should return dashboard data")
    void testGetDashboard() throws Exception {
        mockMvc.perform(get("/api/dashboard").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalInterviews").isNumber())
                .andExpect(jsonPath("$.data.streakDays").isNumber())
                .andExpect(jsonPath("$.data.xpPoints").isNumber())
                .andExpect(jsonPath("$.data.recentInterviews").isArray());
    }

    @Test
    @DisplayName("GET /api/dashboard/statistics — Should return stats only")
    void testGetStatistics() throws Exception {
        mockMvc.perform(get("/api/dashboard/statistics").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.averageScore").isNumber());
    }
}
