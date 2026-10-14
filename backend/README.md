# NextHire Backend — Spring Boot REST API

> Backend REST API for the NextHire AI Interview Platform.  
> Built with Java + Spring Boot + Maven. Runs **without a database** (uses in-memory mock data).

---

## How to Run

### Prerequisites
- Java 17+ installed (Java 26 detected on this machine ✓)
- Maven installed *(or use IntelliJ IDEA's built-in Maven)*

### Option 1: Run in IntelliJ IDEA (Recommended — Maven already installed in IntelliJ)
1. Open IntelliJ IDEA
2. Click **File → Open** → Select the `backend/` folder
3. IntelliJ will auto-detect the `pom.xml` — click **Trust Project**
4. Wait for Maven to download dependencies (~1-2 minutes first time)
5. Open `src/main/java/com/nexthire/NextHireApplication.java`
6. Click the **▶ green Run button** next to `main()`
7. Server starts at: **http://localhost:8080**

### Option 2: Run via Maven command line (if Maven is installed)
```bash
cd backend
mvn spring-boot:run
```

### Option 3: Install Maven first, then run
Download Maven from: https://maven.apache.org/download.cgi  
Extract to `C:\Program Files\Apache Maven\`  
Add `bin/` folder to your System PATH  
Then: `mvn spring-boot:run`

---

## URLs After Starting

| URL | Description |
|-----|-------------|
| `http://localhost:8080/swagger-ui.html` | **Swagger UI** — Interactive API docs |
| `http://localhost:8080/v3/api-docs` | Raw OpenAPI JSON |
| `http://localhost:8080/api/questions` | Test: Get all questions |
| `http://localhost:8080/api/dashboard` | Test: Get dashboard |

---

## All API Endpoints

### 🔐 Authentication — `/api/auth`
| Method | URL | Description |
|--------|-----|-------------|
| `POST` | `/api/auth/register` | Register new user account |
| `POST` | `/api/auth/login` | Login with email + password |

### 🎙️ Interviews — `/api/interviews`
| Method | URL | Description |
|--------|-----|-------------|
| `POST` | `/api/interviews` | Create/start a new interview |
| `GET`  | `/api/interviews/{id}` | Get interview details |
| `GET`  | `/api/interviews?userId={userId}` | Get all interviews for a user |
| `GET`  | `/api/interviews/{id}/questions` | Get questions for an interview |
| `POST` | `/api/interviews/{id}/responses` | Submit an answer |
| `POST` | `/api/interviews/{id}/complete` | Complete the interview |
| `GET`  | `/api/interviews/{id}/result` | Get AI-evaluated result |

### ❓ Questions — `/api/questions`
| Method | URL | Description |
|--------|-----|-------------|
| `GET` | `/api/questions` | Get all questions |
| `GET` | `/api/questions?category=TECHNICAL` | Filter by category |
| `GET` | `/api/questions?difficulty=HARD` | Filter by difficulty |
| `GET` | `/api/questions?category=HR&difficulty=EASY` | Filter by both |
| `GET` | `/api/questions/{id}` | Get specific question |

### 📊 Dashboard — `/api/dashboard`
| Method | URL | Description |
|--------|-----|-------------|
| `GET` | `/api/dashboard` | Full dashboard (stats + recent interviews) |
| `GET` | `/api/dashboard/statistics` | Stats only (lighter) |

---

## Frontend → API Mapping

| Frontend Feature | API Endpoint |
|-----------------|--------------|
| Login page form | `POST /api/auth/login` |
| Sign up form | `POST /api/auth/register` |
| "Start practicing" button | `POST /api/interviews` |
| Interview type selection (Technical, HR, etc.) | `POST /api/interviews` (category field) |
| Question display during interview | `GET /api/interviews/{id}/questions` |
| Answer submission (voice/text) | `POST /api/interviews/{id}/responses` |
| End interview | `POST /api/interviews/{id}/complete` |
| Analytics/Result page | `GET /api/interviews/{id}/result` |
| Navbar streak count (🔥) | `GET /api/dashboard` → `streakDays` |
| Navbar XP counter (⚡) | `GET /api/dashboard` → `xpPoints` |
| Progression path (Steps 1-4) | `GET /api/dashboard` → `step1-4` fields |
| Achievement banner | `GET /api/dashboard` → `streakDays`, `xpPoints` |
| Recent interviews | `GET /api/dashboard` → `recentInterviews` |

---

## Quick API Tests (using curl)

```bash
# Test login with demo user
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@nexthire.ai","password":"anypassword"}'

# Get all questions
curl http://localhost:8080/api/questions

# Get only Technical questions
curl "http://localhost:8080/api/questions?category=TECHNICAL"

# Create an interview
curl -X POST http://localhost:8080/api/interviews \
  -H "Content-Type: application/json" \
  -d '{"category":"TECHNICAL","role":"Software Engineer","difficulty":"MEDIUM","userId":1}'

# Get dashboard (mock data)
curl http://localhost:8080/api/dashboard
```

---

## Project Structure

```
backend/
├── pom.xml                          ← Maven build config
└── src/
    ├── main/
    │   ├── java/com/nexthire/
    │   │   ├── NextHireApplication.java        ← Main entry point
    │   │   ├── config/
    │   │   │   ├── CorsConfig.java             ← CORS setup (frontend ↔ backend)
    │   │   │   └── OpenApiConfig.java          ← Swagger/OpenAPI docs
    │   │   ├── controller/
    │   │   │   ├── AuthController.java         ← /api/auth
    │   │   │   ├── InterviewController.java    ← /api/interviews
    │   │   │   ├── QuestionController.java     ← /api/questions
    │   │   │   └── DashboardController.java    ← /api/dashboard
    │   │   ├── service/
    │   │   │   ├── AuthService.java            ← Auth interface
    │   │   │   ├── InterviewService.java       ← Interview interface
    │   │   │   ├── QuestionService.java        ← Questions interface
    │   │   │   ├── DashboardService.java       ← Dashboard interface
    │   │   │   ├── AIInterviewService.java     ← AI service interface
    │   │   │   └── impl/
    │   │   │       ├── AuthServiceImpl.java
    │   │   │       ├── InterviewServiceImpl.java
    │   │   │       ├── QuestionServiceImpl.java
    │   │   │       ├── DashboardServiceImpl.java
    │   │   │       └── AIInterviewServiceMockImpl.java
    │   │   ├── model/
    │   │   │   ├── User.java                   ← User entity
    │   │   │   ├── Interview.java              ← Interview entity
    │   │   │   ├── Question.java               ← Question entity
    │   │   │   ├── InterviewResponse.java      ← Answer entity
    │   │   │   └── InterviewResult.java        ← Result entity
    │   │   ├── dto/
    │   │   │   ├── request/
    │   │   │   │   ├── LoginRequest.java
    │   │   │   │   ├── RegisterRequest.java
    │   │   │   │   ├── CreateInterviewRequest.java
    │   │   │   │   └── SubmitResponseRequest.java
    │   │   │   └── response/
    │   │   │       ├── ApiResponse.java        ← Standard response wrapper
    │   │   │       ├── UserResponse.java
    │   │   │       ├── LoginResponse.java
    │   │   │       ├── InterviewResponseDto.java
    │   │   │       ├── InterviewResultResponse.java
    │   │   │       └── DashboardResponse.java
    │   │   ├── exception/
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   ├── BadRequestException.java
    │   │   │   └── GlobalExceptionHandler.java
    │   │   └── repository/
    │   │       ├── UserRepository.java         ← Placeholder (no DB yet)
    │   │       ├── InterviewRepository.java    ← Placeholder (no DB yet)
    │   │       └── QuestionRepository.java     ← Placeholder (no DB yet)
    │   └── resources/
    │       └── application.properties         ← App configuration
    └── test/
        └── java/com/nexthire/
            └── NextHireApiTests.java           ← API integration tests
```

---

## What's Left for Phase 2

### MySQL Database Integration
1. Uncomment MySQL dependencies in `pom.xml`
2. Uncomment MySQL config in `application.properties`
3. Create the `nexthire_db` MySQL database
4. Add `@Entity` / `@Table` annotations to model classes
5. Replace placeholder repositories with `JpaRepository` extensions
6. Remove in-memory storage from service implementations

### Authentication (JWT + BCrypt)
1. Add Spring Security + JWT dependencies to `pom.xml`
2. Create `JwtTokenProvider` utility class
3. Create Spring Security filter chain configuration
4. Replace mock login in `AuthServiceImpl` with:
   - `BCryptPasswordEncoder.matches()` for password verification
   - Real JWT token generation
5. Add `@PreAuthorize` to protected endpoints
6. Extract userId from JWT token in controllers (remove `?userId=` query params)
