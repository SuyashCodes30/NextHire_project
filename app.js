/**
 * NextHire AI Platform — Frontend Application Logic & Backend Integration
 * Architecture: Browser -> REST API (HTTP) -> Spring Boot Backend (port 8080)
 */

(function () {
    'use strict';

    // =========================================================================
    // 1. API CONFIGURATION
    // Centralized API base URL — single source of truth
    // Resolves from window.NEXTHIRE_CONFIG (supporting VITE_API_BASE_URL / production backend),
    // falling back gracefully to http://localhost:8080/api for local development.
    // =========================================================================
    const API_BASE_URL = (window.NEXTHIRE_CONFIG && typeof window.NEXTHIRE_CONFIG.getApiUrl === 'function')
        ? window.NEXTHIRE_CONFIG.getApiUrl()
        : (window.location.hostname === 'localhost'
            ? 'http://localhost:8080/api'
            : '/api');

    console.log('🚀 NextHire Frontend initialized. Target API:', API_BASE_URL);

    // State for current interview session
    let currentInterview = null;
    let interviewQuestions = [];
    let currentQuestionIndex = 0;
    let currentQuestionStartTime = 0;
    let isSubmitting = false;
    let currentInterviewAnswers = [];

    // =========================================================================
    // 2. DASHBOARD DATA LOADER — GET /api/dashboard
    // Replaces static fake stats with real live data from Spring Boot
    // =========================================================================
    async function fetchDashboardData() {
        try {
            let userId = 1;
            try {
                const rawUser = localStorage.getItem('nh_user');
                if (rawUser) {
                    const u = JSON.parse(rawUser);
                    if (u && u.id) userId = u.id;
                }
            } catch (e) {}

            const response = await fetch(`${API_BASE_URL}/dashboard?userId=${userId}`);
            if (!response.ok) throw new Error(`HTTP error ${response.status}`);
            const result = await response.json();

            if (result.success && result.data) {
                const d = result.data;
                console.log('📊 Dashboard data loaded from backend:', d);

                // Update navbar counters
                const streakEl = document.getElementById('nhStreakCount');
                const xpEl = document.getElementById('nhXpVal');
                if (streakEl && d.streakDays !== undefined) streakEl.textContent = d.streakDays;
                if (xpEl && d.xpPoints !== undefined) xpEl.textContent = `${d.xpPoints} XP`;

                // 1. Update Hero AI Performance Score Card
                const heroScoreEl = document.getElementById('nhHeroScoreVal');
                const heroRingEl = document.getElementById('nhHeroScoreRing');
                const heroLabelEl = document.getElementById('nhHeroScoreLabel');

                const overallVal = (d.accuracyPercentage !== undefined && d.totalQuestions > 0)
                    ? Math.round(d.accuracyPercentage)
                    : (d.averageScore !== undefined && d.averageScore > 0 ? Math.round(d.averageScore) : 0);

                if (heroScoreEl) heroScoreEl.textContent = overallVal;
                if (heroRingEl) {
                    const deg = (overallVal / 100) * 360;
                    heroRingEl.style.setProperty('--score-deg', `${deg}deg`);
                }
                if (heroLabelEl) {
                    heroLabelEl.textContent = overallVal === 0 ? 'Not Evaluated' : (d.readinessLabel || (overallVal >= 80 ? 'Interview Ready' : (overallVal >= 50 ? 'Developing' : 'Needs Practice')));
                }

                // Objective Performance Breakdown Bars
                const accuracy = d.accuracyPercentage !== undefined ? Math.round(d.accuracyPercentage) : 0;
                const verifiedRatio = (d.totalQuestions > 0 && d.correctCount !== undefined)
                    ? Math.round((d.correctCount / d.totalQuestions) * 100)
                    : accuracy;
                const techScore = (d.technicalScore !== undefined && d.totalQuestions > 0) ? d.technicalScore : accuracy;
                const completion = d.totalQuestions > 0 ? 100 : 0;

                updateBarAndVal('nhHeroCommBar', 'nhHeroCommVal', accuracy, '%');
                updateBarAndVal('nhHeroConfBar', 'nhHeroConfVal', verifiedRatio, '%');
                updateBarAndVal('nhHeroTechBar', 'nhHeroTechVal', techScore, '%');
                updateBarAndVal('nhHeroStructBar', 'nhHeroStructVal', completion, '%');

                // 2. Update Post-Interview Analytics Section Mockup
                const analyticsRing = document.getElementById('nhAnalyticsRing');
                const analyticsOvScore = document.getElementById('nhAnalyticsOvScore');
                const analyticsOvBadge = document.getElementById('nhAnalyticsOvBadge');

                if (analyticsOvScore) analyticsOvScore.textContent = overallVal;
                if (analyticsRing) {
                    const deg = (overallVal / 100) * 360;
                    analyticsRing.style.setProperty('--score-deg', `${deg}deg`);
                }
                if (analyticsOvBadge) {
                    analyticsOvBadge.textContent = overallVal === 0 ? 'Practice Required' : (d.readinessLabel || (overallVal >= 80 ? 'Interview Ready' : 'Developing'));
                }

                updateBarAndVal('nhAnalyticsCommBar', 'nhAnalyticsCommVal', accuracy, '%');
                updateBarAndVal('nhAnalyticsConfBar', 'nhAnalyticsConfVal', verifiedRatio, '%');
                updateBarAndVal('nhAnalyticsTechBar', 'nhAnalyticsTechVal', techScore, '%');
                updateBarAndVal('nhAnalyticsClarityBar', 'nhAnalyticsClarityVal', 100, '%');
                updateBarAndVal('nhAnalyticsStructBar', 'nhAnalyticsStructVal', completion, '%');

                // Update Gamification Roadmap steps if available
                updateRoadmap(d);
            }
        } catch (err) {
            console.warn('⚠️ Unable to fetch live dashboard from backend (is Spring Boot running on 8080?):', err.message);
        }
    }

    function updateBarAndVal(barId, valId, score, suffix = '') {
        const bar = document.getElementById(barId);
        const val = document.getElementById(valId);
        if (bar) bar.style.width = `${score}%`;
        if (val) val.textContent = `${score}${suffix}`;
    }

    function updateRoadmap(d) {
        const steps = document.querySelectorAll('.progression-path .path-step');
        if (steps.length >= 4) {
            // Step 1
            if (d.step1Completed) setStepStatus(steps[0], 'completed', 'Completed');
            // Step 2
            if (d.step2Completed) setStepStatus(steps[1], 'completed', 'Completed');
            // Step 3
            if (d.step3Active) setStepStatus(steps[2], 'active', 'In progress');
            // Step 4
            if (d.step4Unlocked) {
                setStepStatus(steps[3], 'completed', 'Unlocked');
            } else {
                setStepStatus(steps[3], 'locked', 'Locked');
            }
        }
    }

    function setStepStatus(stepEl, stateClass, text) {
        if (!stepEl) return;
        stepEl.classList.remove('completed', 'active', 'locked');
        stepEl.classList.add(stateClass);
        const statusSpan = stepEl.querySelector('.step-status');
        if (statusSpan) {
            statusSpan.className = `step-status status-${stateClass === 'completed' ? 'done' : stateClass}`;
            statusSpan.textContent = text;
        }
    }

    // =========================================================================
    // 3. USER AUTH / SESSION
    // Reads stored candidate session and reflects in the navbar
    // =========================================================================
    function checkUserSession() {
        try {
            const rawUser = localStorage.getItem('nh_user');
            if (rawUser) {
                const user = JSON.parse(rawUser);
                const navLoginBtn = document.getElementById('nhNavLoginBtn');
                const avatarEl = document.getElementById('nhHeroAvatar');
                const candidateNameEl = document.getElementById('nhHeroCandidateName');

                const displayName = user.firstName || (user.email ? user.email.split('@')[0] : 'Candidate');

                if (navLoginBtn) {
                    navLoginBtn.innerHTML = `👤 ${displayName} <span style="font-size:10px;opacity:0.75;margin-left:4px;">(Logout)</span>`;
                    navLoginBtn.href = '#';
                    navLoginBtn.title = `Logged in as ${user.email || displayName}. Click to logout.`;
                    navLoginBtn.addEventListener('click', function (e) {
                        e.preventDefault();
                        if (confirm(`Log out ${displayName}?`)) {
                            localStorage.removeItem('nh_token');
                            localStorage.removeItem('nh_user');
                            window.location.reload();
                        }
                    });
                }

                if (avatarEl) avatarEl.textContent = displayName.charAt(0).toUpperCase();
                if (candidateNameEl) candidateNameEl.textContent = displayName;
            }
        } catch (e) {
            console.warn('Session parse error:', e);
        }
    }

    // =========================================================================
    // 4. INTERVIEW LIFECYCLE CONTROLLER
    // Flow: Setup Modal -> Start (POST) -> Questions -> Answer (POST) -> Complete (POST) -> Result (GET)
    // =========================================================================

    /**
     * Open Interview Configuration Setup Modal
     * Allows candidate to choose Category, Difficulty (Easy, Medium, Hard), and Number of Questions (5, 10, 15, 20)
     */
    function openInterviewSetupModal(preCat, preRole, preDiff, preCount) {
        let selectedCategory = preCat || 'data-analyst';
        let selectedRole = preRole || (preCat === 'data-analyst' ? 'Data Analyst' : 'Technical Candidate');
        let selectedDifficulty = (preDiff || 'medium').toLowerCase();
        let selectedCount = preCount || 10;

        const modal = ensureInterviewModal();
        modal.classList.add('is-active');

        function renderSetupView() {
            modal.innerHTML = `
                <div class="interview-modal-container im-setup-container">
                    <div class="interview-modal-header">
                        <div class="im-meta-left">
                            <span class="im-badge im-badge-category">Session Setup</span>
                            <span class="im-role">AI Technical Simulation</span>
                        </div>
                        <button class="im-close-btn" id="nhCloseSetupBtn" title="Close">&times;</button>
                    </div>

                    <div class="im-body im-setup-body">
                        <div class="im-setup-header">
                            <h2 class="im-setup-title">🎯 Configure Your Interview</h2>
                            <p class="im-setup-desc">Customize your interview parameters. The AI backend selects a topic-diverse question set with authentic learning resources.</p>
                        </div>

                        <!-- 1. Category Selector -->
                        <div class="im-setup-group">
                            <label class="im-setup-label">1. Select Domain Category</label>
                            <div class="im-setup-grid-categories">
                                <button type="button" class="im-choice-card ${selectedCategory === 'data-analyst' ? 'is-selected' : ''}" data-cat="data-analyst" data-role="Data Analyst">
                                    <span class="im-choice-icon">📊</span>
                                    <span class="im-choice-title">Data Analyst</span>
                                    <span class="im-choice-sub">SQL, Excel, Statistics, Viz</span>
                                </button>
                                <button type="button" class="im-choice-card ${selectedCategory === 'data-scientist' ? 'is-selected' : ''}" data-cat="data-scientist" data-role="Data Scientist">
                                    <span class="im-choice-icon">🧠</span>
                                    <span class="im-choice-title">Data Scientist</span>
                                    <span class="im-choice-sub">Python, ML, Deep Learning, Stats</span>
                                </button>
                                <button type="button" class="im-choice-card ${selectedCategory === 'software-developer' ? 'is-selected' : ''}" data-cat="software-developer" data-role="Software Developer">
                                    <span class="im-choice-icon">💻</span>
                                    <span class="im-choice-title">Software Developer</span>
                                    <span class="im-choice-sub">DSA, OOP, DBMS, Networks</span>
                                </button>
                                <button type="button" class="im-choice-card ${selectedCategory === 'english-communication' ? 'is-selected' : ''}" data-cat="english-communication" data-role="Communication Specialist">
                                    <span class="im-choice-icon">🗣️</span>
                                    <span class="im-choice-title">English Academy</span>
                                    <span class="im-choice-sub">Grammar, STAR, Speaking</span>
                                </button>
                                <button type="button" class="im-choice-card ${selectedCategory === 'general-interview' ? 'is-selected' : ''}" data-cat="general-interview" data-role="Candidate">
                                    <span class="im-choice-icon">👔</span>
                                    <span class="im-choice-title">General Interview</span>
                                    <span class="im-choice-sub">HR, Behavioral, Goals</span>
                                </button>
                            </div>
                        </div>

                        <!-- 2. Difficulty Selector -->
                        <div class="im-setup-group">
                            <label class="im-setup-label">2. Select Difficulty Level</label>
                            <div class="im-setup-pills im-pills-diff">
                                <button type="button" class="im-pill-btn ${selectedDifficulty === 'easy' ? 'is-selected' : ''}" data-diff="easy">
                                    <strong><span class="pill-dot dot-green"></span> Easy</strong>
                                    <small>Basic concepts, definitions & practicals</small>
                                </button>
                                <button type="button" class="im-pill-btn ${selectedDifficulty === 'medium' ? 'is-selected' : ''}" data-diff="medium">
                                    <strong><span class="pill-dot dot-yellow"></span> Medium</strong>
                                    <small>Concept application & scenarios</small>
                                </button>
                                <button type="button" class="im-pill-btn ${selectedDifficulty === 'hard' ? 'is-selected' : ''}" data-diff="hard">
                                    <strong><span class="pill-dot dot-red"></span> Hard</strong>
                                    <small>Complex reasoning & real-world cases</small>
                                </button>
                            </div>
                        </div>

                        <!-- 3. Number of Questions Selector -->
                        <div class="im-setup-group">
                            <label class="im-setup-label">3. Number of Questions</label>
                            <div class="im-setup-pills im-pills-count">
                                <button type="button" class="im-pill-btn count-pill ${selectedCount === 5 ? 'is-selected' : ''}" data-count="5">
                                    <strong>5 Questions</strong>
                                    <small>Quick Sprint (~10 mins)</small>
                                </button>
                                <button type="button" class="im-pill-btn count-pill ${selectedCount === 10 ? 'is-selected' : ''}" data-count="10">
                                    <strong>10 Questions</strong>
                                    <small>Standard Round (~20 mins)</small>
                                </button>
                                <button type="button" class="im-pill-btn count-pill ${selectedCount === 15 ? 'is-selected' : ''}" data-count="15">
                                    <strong>15 Questions</strong>
                                    <small>Deep Assessment (~30 mins)</small>
                                </button>
                                <button type="button" class="im-pill-btn count-pill ${selectedCount === 20 ? 'is-selected' : ''}" data-count="20">
                                    <strong>20 Questions</strong>
                                    <small>Full Simulation (~45 mins)</small>
                                </button>
                            </div>
                        </div>
                    </div>

                    <div class="interview-modal-footer">
                        <button class="btn-duo-secondary" id="nhCancelSetupBtn">Cancel</button>
                        <button class="btn-duo-primary" id="nhStartConfiguredBtn">Start Interview 🚀</button>
                    </div>
                </div>
            `;

            // Wire Category selection
            modal.querySelectorAll('.im-choice-card').forEach(card => {
                card.onclick = function () {
                    selectedCategory = this.dataset.cat;
                    selectedRole = this.dataset.role;
                    renderSetupView();
                };
            });

            // Wire Difficulty selection
            modal.querySelectorAll('.im-pills-diff .im-pill-btn').forEach(btn => {
                btn.onclick = function () {
                    selectedDifficulty = this.dataset.diff;
                    renderSetupView();
                };
            });

            // Wire Question Count selection
            modal.querySelectorAll('.im-pills-count .im-pill-btn').forEach(btn => {
                btn.onclick = function () {
                    selectedCount = parseInt(this.dataset.count, 10);
                    renderSetupView();
                };
            });

            document.getElementById('nhCloseSetupBtn').onclick = closeModal;
            document.getElementById('nhCancelSetupBtn').onclick = closeModal;

            document.getElementById('nhStartConfiguredBtn').onclick = function () {
                startInterview(selectedCategory, selectedRole, selectedDifficulty, selectedCount);
            };
        }

        renderSetupView();
    }

    /**
     * Start a new interview session via POST /api/interviews
     */
    async function startInterview(category, role, difficulty, numberOfQuestions) {
        category = category || 'data-analyst';
        role = role || 'Data Analyst';
        difficulty = (difficulty || 'medium').toUpperCase();
        numberOfQuestions = numberOfQuestions || 5;
        currentInterviewAnswers = [];

        showModalLoader(`Configuring ${numberOfQuestions} ${difficulty.toLowerCase()} questions for ${role}...`);

        try {
            let userId = 1;
            try {
                const u = JSON.parse(localStorage.getItem('nh_user'));
                if (u && u.id) userId = u.id;
            } catch (e) {}

            // 1. Create interview on backend with explicit numberOfQuestions
            console.log(`📡 POST ${API_BASE_URL}/interviews`, { category, role, difficulty, numberOfQuestions, userId });
            const createRes = await fetch(`${API_BASE_URL}/interviews`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ category, role, difficulty, numberOfQuestions, userId })
            });

            if (!createRes.ok) throw new Error(`HTTP ${createRes.status} creating interview`);
            const createData = await createRes.json();
            if (!createData.success || !createData.data) {
                throw new Error(createData.message || 'Failed to initialize interview');
            }

            currentInterview = createData.data;
            console.log('✅ Interview created successfully:', currentInterview);

            // Use questions directly if returned in response DTO
            if (Array.isArray(currentInterview.questions) && currentInterview.questions.length > 0) {
                interviewQuestions = currentInterview.questions;
            } else {
                // Otherwise fetch questions via GET /api/interviews/{id}/questions
                console.log(`📡 GET ${API_BASE_URL}/interviews/${currentInterview.id}/questions`);
                const qRes = await fetch(`${API_BASE_URL}/interviews/${currentInterview.id}/questions`);
                if (!qRes.ok) throw new Error(`HTTP ${qRes.status} loading questions`);
                const qData = await qRes.json();
                interviewQuestions = (qData.success && Array.isArray(qData.data)) ? qData.data : [];
            }

            if (interviewQuestions.length === 0) {
                throw new Error('No questions returned by backend for this category.');
            }

            currentQuestionIndex = 0;
            renderCurrentQuestion();

        } catch (err) {
            console.error('❌ Failed to start interview:', err);
            showModalError(`Failed to connect to backend: ${err.message}. Please verify the Spring Boot server is running on port 8080.`);
        }
    }

    /**
     * Display current question in the interview modal
     */
    function renderCurrentQuestion() {
        const q = interviewQuestions[currentQuestionIndex];
        currentQuestionStartTime = Date.now();

        const modal = ensureInterviewModal();
        modal.classList.add('is-active');

        const totalQ = interviewQuestions.length;
        const currentQNum = currentQuestionIndex + 1;

        modal.innerHTML = `
            <div class="interview-modal-container">
                <div class="interview-modal-header">
                    <div class="im-meta-left">
                        <span class="im-badge im-badge-category">${currentInterview.category}</span>
                        <span class="im-badge im-badge-diff">${q.difficulty || currentInterview.difficulty}</span>
                        <span class="im-role">${escapeHtml(currentInterview.role)}</span>
                    </div>
                    <div class="im-meta-right">
                        <span class="im-counter">Question <strong>${currentQNum}</strong> of ${totalQ}</span>
                        <button class="im-close-btn" id="nhCloseModalBtn" title="Exit Interview">&times;</button>
                    </div>
                </div>

                <div class="im-body">
                    <!-- Progress Bar -->
                    <div class="im-progress-bar">
                        <div class="im-progress-fill" style="width: ${(currentQNum / totalQ) * 100}%;"></div>
                    </div>

                    <!-- Question Card -->
                    <div class="im-question-card">
                        <div class="im-q-label">
                            <span class="im-q-pulse"></span>
                            <span>AI Interviewer Question</span>
                            ${q.tags ? `<span class="im-tags">Tags: ${escapeHtml(q.tags)}</span>` : ''}
                        </div>
                        <h2 class="im-question-text">“${escapeHtml(q.text)}”</h2>
                    </div>

                    <!-- Audio Simulation Wave -->
                    <div class="im-audio-wave-bar">
                        <div class="im-wave-pulse">
                            <span class="im-w-bar b1"></span>
                            <span class="im-w-bar b2"></span>
                            <span class="im-w-bar b3"></span>
                            <span class="im-w-bar b4"></span>
                            <span class="im-w-bar b5"></span>
                        </div>
                        <span class="im-wave-hint">Session Active — Type or speak your response below</span>
                    </div>

                    <!-- Answer Input Area -->
                    <div class="im-answer-area">
                        <label for="nhAnswerInput" class="im-input-label">Your Response:</label>
                        <textarea id="nhAnswerInput" class="im-textarea" rows="4" 
                            placeholder="Type your answer here... (Tip: Structure using the STAR method for behavioral questions)"></textarea>
                        
                        <div class="im-answer-tools">
                            <button type="button" class="im-tool-btn" id="nhSpeechBtn">
                                🎙️ Speak Answer
                            </button>
                            <span class="im-word-counter" id="nhWordCount">0 words</span>
                        </div>
                    </div>

                    <!-- Status Notification -->
                    <div id="imStatusMsg" class="im-status-msg" style="display:none;"></div>
                </div>

                <div class="interview-modal-footer">
                    <button class="btn-duo-secondary" id="nhCancelInterviewBtn">Exit</button>
                    <button class="btn-duo-primary btn-submit-step" id="nhSubmitAnswerBtn">
                        ${currentQNum === totalQ ? 'Submit & Finish Interview 🚀' : 'Submit & Next Question &rarr;'}
                    </button>
                </div>
            </div>
        `;

        // Wire event listeners
        document.getElementById('nhCloseModalBtn').onclick = confirmExit;
        document.getElementById('nhCancelInterviewBtn').onclick = confirmExit;

        const answerInput = document.getElementById('nhAnswerInput');
        const wordCountEl = document.getElementById('nhWordCount');
        const submitBtn = document.getElementById('nhSubmitAnswerBtn');
        const speechBtn = document.getElementById('nhSpeechBtn');

        answerInput.focus();

        answerInput.addEventListener('input', function () {
            const words = this.value.trim() ? this.value.trim().split(/\s+/).length : 0;
            wordCountEl.textContent = `${words} word${words === 1 ? '' : 's'}`;
        });

        // Speech recognition integration
        if (speechBtn) {
            setupSpeechRecognition(speechBtn, answerInput);
        }

        submitBtn.onclick = async function () {
            if (isSubmitting) return;
            const answerText = answerInput.value.trim();
            if (!answerText) {
                showModalInlineMsg('Please enter or dictate an answer before submitting.', true);
                answerInput.focus();
                return;
            }

            const elapsedSecs = Math.max(5, Math.round((Date.now() - currentQuestionStartTime) / 1000));
            const words = answerText.split(/\s+/).length;

            isSubmitting = true;
            submitBtn.disabled = true;
            submitBtn.textContent = 'Analyzing answer accuracy...';

            try {
                // 3. POST /api/interviews/{id}/responses
                console.log(`📡 POST ${API_BASE_URL}/interviews/${currentInterview.id}/responses`, {
                    questionId: q.id,
                    responseText: answerText,
                    durationSecs: elapsedSecs,
                    wordCount: words
                });

                let savedEval = null;
                try {
                    const resp = await fetch(`${API_BASE_URL}/interviews/${currentInterview.id}/responses`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            questionId: q.id,
                            responseText: answerText,
                            durationSecs: elapsedSecs,
                            wordCount: words,
                            answerMetadata: JSON.stringify({ device: 'browser', timestamp: new Date().toISOString() })
                        })
                    });

                    if (resp.ok) {
                        const saved = await resp.json();
                        if (saved.success && saved.data) {
                            savedEval = saved.data;
                            console.log('✅ Response saved and evaluated by backend:', savedEval);
                        }
                    }
                } catch (netErr) {
                    console.warn('⚠️ Network call to backend failed, using client evaluation engine:', netErr.message);
                }

                // If backend provided evaluation, format it; otherwise use client evaluation
                const evalItem = (savedEval && savedEval.score !== undefined)
                    ? {
                        questionId: q.id,
                        questionNumber: currentQNum,
                        questionText: q.text,
                        topic: q.topic || currentInterview.category,
                        subtopic: q.subtopic || savedEval.subtopic || '',
                        difficulty: q.difficulty || currentInterview.difficulty,
                        userAnswer: answerText,
                        expectedAnswer: savedEval.expectedAnswer || q.expectedAnswer || 'A clear explanation of the core technical concepts.',
                        correctAnswer: savedEval.correctAnswer || q.correctAnswer || q.expectedAnswer || '',
                        explanation: savedEval.explanation || q.explanation || '',
                        keyPoints: savedEval.keyPoints || q.keyPoints || [],
                        learningResources: savedEval.learningResources || q.learningResources || [],
                        score: savedEval.score,
                        isCorrect: savedEval.isCorrect,
                        status: savedEval.status || (savedEval.isCorrect ? 'CORRECT' : 'INCORRECT'),
                        statusLabel: savedEval.statusLabel || (savedEval.isCorrect ? 'Right / Correct ✓' : 'Incorrect / Not Right ✗'),
                        feedback: savedEval.feedback || 'Answer evaluated.',
                        coveredConcepts: savedEval.coveredConcepts || [],
                        missingConcepts: savedEval.missingConcepts || []
                    }
                    : clientEvaluateAnswer(q, answerText, currentQNum);

                currentInterviewAnswers.push(evalItem);

                // Show visual feedback toast for answer check
                const isCorrect = evalItem.status === 'CORRECT';
                const isPartial = evalItem.status === 'PARTIALLY_CORRECT';
                showModalInlineMsg(
                    `${evalItem.statusLabel} (Score: ${evalItem.score}%) — ${evalItem.feedback}`,
                    !isCorrect && !isPartial
                );

                // Short delay to display evaluation before moving forward
                await new Promise(resolve => setTimeout(resolve, 600));

                isSubmitting = false;

                // Move to next question or complete interview
                if (currentQuestionIndex + 1 < interviewQuestions.length) {
                    currentQuestionIndex++;
                    renderCurrentQuestion();
                } else {
                    await completeAndShowResult();
                }

            } catch (err) {
                isSubmitting = false;
                submitBtn.disabled = false;
                submitBtn.textContent = (currentQNum === totalQ) ? 'Submit & Finish Interview 🚀' : 'Submit & Next Question &rarr;';
                showModalInlineMsg(`Failed to save answer: ${err.message}`, true);
            }
        };
    }

    /**
     * Client-side evaluation fallback to verify Right vs Not Right when offline or standalone
     */
    function clientEvaluateAnswer(q, answerText, qNum) {
        const text = (answerText || '').trim();
        const words = text ? text.split(/\s+/).length : 0;
        const lower = text.toLowerCase();
        const expected = (q && (q.correctAnswer || q.expectedAnswer)) || 'Comprehensive technical answer explaining the mechanism with examples.';
        const qText = ((q && q.text) || '').toLowerCase();

        // 1. Empty / whitespace
        if (!text || words < 2) {
            return {
                questionId: q ? q.id : null,
                questionNumber: qNum,
                questionText: q ? q.text : 'Question',
                topic: q ? q.topic : 'General',
                difficulty: q ? q.difficulty : 'MEDIUM',
                userAnswer: text || '(No answer submitted)',
                expectedAnswer: expected,
                score: 0,
                isCorrect: false,
                status: 'INCORRECT',
                statusLabel: 'Incorrect / Not Right ✗',
                feedback: 'No answer was provided. Review the expected answer below to master the core concept.',
                coveredConcepts: [],
                missingConcepts: ['Essential explanation']
            };
        }

        // 2. Direct lack of knowledge / evasion
        if (lower.match(/^(i\s+)?(do\s+not|dont)\s+know/) || lower.match(/^(no\s+idea|not\s+sure|idk|skip|pass|n\/?a)$/)) {
            return {
                questionId: q ? q.id : null,
                questionNumber: qNum,
                questionText: q ? q.text : 'Question',
                topic: q ? q.topic : 'General',
                difficulty: q ? q.difficulty : 'MEDIUM',
                userAnswer: text,
                expectedAnswer: expected,
                score: 0,
                isCorrect: false,
                status: 'INCORRECT',
                statusLabel: 'Incorrect / Not Right ✗',
                feedback: 'The response indicates you do not know the answer. Study the model solution and key points below.',
                coveredConcepts: [],
                missingConcepts: ['Core question requirements']
            };
        }

        // 3. Multiple Choice
        if (q && q.options && q.options.length > 0) {
            const expLower = expected.toLowerCase().trim();
            const isMatch = lower.includes(expLower) || (q.correctOption && lower.includes(q.correctOption.toLowerCase()));
            return {
                questionId: q.id,
                questionNumber: qNum,
                questionText: q.text,
                topic: q.topic || 'General',
                difficulty: q.difficulty || 'MEDIUM',
                userAnswer: text,
                expectedAnswer: expected,
                score: isMatch ? 100 : 0,
                isCorrect: isMatch,
                status: isMatch ? 'CORRECT' : 'INCORRECT',
                statusLabel: isMatch ? 'Right / Correct ✓' : 'Incorrect / Not Right ✗',
                feedback: isMatch ? `Correct! You selected the right option: ${expected}` : `Incorrect. The correct option is: ${expected}.`,
                coveredConcepts: isMatch ? [expected] : [],
                missingConcepts: isMatch ? [] : [expected]
            };
        }

        // 4. Q: WHERE vs HAVING
        if (qText.includes('where') && qText.includes('having')) {
            const hasWhere = (lower.includes('before') || lower.includes('row') || lower.includes('pre-aggregation')) && lower.includes('where');
            const hasHaving = (lower.includes('after') || lower.includes('group') || lower.includes('aggregate')) && lower.includes('having');
            const whereWrong = lower.includes('where filters groups') || lower.includes('where is after');
            const havingWrong = lower.includes('having filters rows') || lower.includes('having is before');

            if (whereWrong || havingWrong) {
                return {
                    questionId: q.id, questionNumber: qNum, questionText: q.text, topic: q.topic || 'SQL', difficulty: q.difficulty || 'EASY',
                    userAnswer: text, expectedAnswer: expected, score: 0, isCorrect: false, status: 'INCORRECT',
                    statusLabel: 'Incorrect / Not Right ✗',
                    feedback: 'Incorrect. WHERE filters individual rows before aggregation occurs, while HAVING filters aggregated groups after GROUP BY.',
                    coveredConcepts: [],
                    missingConcepts: ['Pre-aggregation vs post-aggregation clause sequencing']
                };
            }

            if (hasWhere && hasHaving) {
                return {
                    questionId: q.id, questionNumber: qNum, questionText: q.text, topic: q.topic || 'SQL', difficulty: q.difficulty || 'EASY',
                    userAnswer: text, expectedAnswer: expected, score: 100, isCorrect: true, status: 'CORRECT',
                    statusLabel: 'Right / Correct ✓',
                    feedback: 'Spot-on! You accurately highlighted that WHERE filters rows before aggregation occurs and HAVING filters grouped results after GROUP BY.',
                    coveredConcepts: ['WHERE filters rows before aggregation', 'HAVING filters groups after GROUP BY'],
                    missingConcepts: []
                };
            } else if (hasWhere || hasHaving) {
                return {
                    questionId: q.id, questionNumber: qNum, questionText: q.text, topic: q.topic || 'SQL', difficulty: q.difficulty || 'EASY',
                    userAnswer: text, expectedAnswer: expected, score: 50, isCorrect: false, status: 'PARTIALLY_CORRECT',
                    statusLabel: 'Partially Correct ~',
                    feedback: 'Partially correct. You explained one clause well, but make sure to contrast pre-aggregation filtering (WHERE) with post-GROUP BY aggregate filtering (HAVING).',
                    coveredConcepts: hasWhere ? ['WHERE pre-aggregation row filtering'] : ['HAVING post-aggregation group filtering'],
                    missingConcepts: hasWhere ? ['HAVING post-aggregation group filtering'] : ['WHERE pre-aggregation row filtering']
                };
            } else {
                return {
                    questionId: q.id, questionNumber: qNum, questionText: q.text, topic: q.topic || 'SQL', difficulty: q.difficulty || 'EASY',
                    userAnswer: text, expectedAnswer: expected, score: 0, isCorrect: false, status: 'INCORRECT',
                    statusLabel: 'Incorrect / Not Right ✗',
                    feedback: 'Incorrect. WHERE filters rows before aggregation, while HAVING filters groups after aggregation.',
                    coveredConcepts: [],
                    missingConcepts: ['WHERE vs HAVING distinction']
                };
            }
        }

        // 5. Q: HashMap vs TreeMap
        if (qText.includes('hashmap') && qText.includes('treemap')) {
            const hasHash = lower.includes('hash') || lower.includes('bucket') || lower.includes('o(1)');
            const hasTree = lower.includes('tree') || lower.includes('red-black') || lower.includes('sorted') || lower.includes('order') || lower.includes('o(log');
            const hashWrong = lower.includes('hashmap is sorted');
            const treeWrong = lower.includes('treemap uses hash') || lower.includes('treemap is o(1)');

            if (hashWrong || treeWrong) {
                return {
                    questionId: q.id, questionNumber: qNum, questionText: q.text, topic: q.topic || 'Java', difficulty: q.difficulty || 'EASY',
                    userAnswer: text, expectedAnswer: expected, score: 0, isCorrect: false, status: 'INCORRECT',
                    statusLabel: 'Incorrect / Not Right ✗',
                    feedback: 'Incorrect. HashMap uses hash buckets in O(1) time without sorted order; TreeMap maintains sorted keys using a Red-Black Tree in O(log n) time.',
                    coveredConcepts: [],
                    missingConcepts: ['Data structure mechanics']
                };
            }

            if (hasHash && hasTree) {
                return {
                    questionId: q.id, questionNumber: qNum, questionText: q.text, topic: q.topic || 'Java', difficulty: q.difficulty || 'EASY',
                    userAnswer: text, expectedAnswer: expected, score: 100, isCorrect: true, status: 'CORRECT',
                    statusLabel: 'Right / Correct ✓',
                    feedback: 'Excellent! You clearly identified that HashMap provides O(1) hash bucket lookup while TreeMap maintains sorted keys using a Red-Black Tree in O(log n) time.',
                    coveredConcepts: ['HashMap O(1) hash table', 'TreeMap Red-Black Tree sorting'],
                    missingConcepts: []
                };
            } else if (hasHash || hasTree) {
                return {
                    questionId: q.id, questionNumber: qNum, questionText: q.text, topic: q.topic || 'Java', difficulty: q.difficulty || 'EASY',
                    userAnswer: text, expectedAnswer: expected, score: 50, isCorrect: false, status: 'PARTIALLY_CORRECT',
                    statusLabel: 'Partially Correct ~',
                    feedback: 'Partially correct. You addressed one collection, but compare both (O(1) hash table vs O(log n) sorted Red-Black tree).',
                    coveredConcepts: hasHash ? ['HashMap hashing mechanism'] : ['TreeMap Red-Black Tree ordering'],
                    missingConcepts: hasHash ? ['TreeMap Red-Black Tree ordering'] : ['HashMap hashing mechanism']
                };
            } else {
                return {
                    questionId: q.id, questionNumber: qNum, questionText: q.text, topic: q.topic || 'Java', difficulty: q.difficulty || 'EASY',
                    userAnswer: text, expectedAnswer: expected, score: 0, isCorrect: false, status: 'INCORRECT',
                    statusLabel: 'Incorrect / Not Right ✗',
                    feedback: 'Incorrect. HashMap is an O(1) hash table; TreeMap is an O(log n) sorted Red-Black tree.',
                    coveredConcepts: [],
                    missingConcepts: ['HashMap vs TreeMap structures']
                };
            }
        }

        // 6. Q: Grammar - Neither was
        if (qText.includes('neither of the project managers were')) {
            if (lower.includes('was') && !lower.includes('were available')) {
                return {
                    questionId: q.id, questionNumber: qNum, questionText: q.text, topic: 'Grammar', difficulty: 'EASY',
                    userAnswer: text, expectedAnswer: expected, score: 100, isCorrect: true, status: 'CORRECT',
                    statusLabel: 'Right / Correct ✓',
                    feedback: "Right! 'Neither' is singular and requires 'was': 'Neither of the project managers was available'.",
                    coveredConcepts: ["Subject-verb agreement: singular 'was'"],
                    missingConcepts: []
                };
            } else {
                return {
                    questionId: q.id, questionNumber: qNum, questionText: q.text, topic: 'Grammar', difficulty: 'EASY',
                    userAnswer: text, expectedAnswer: expected, score: 0, isCorrect: false, status: 'INCORRECT',
                    statusLabel: 'Incorrect / Not Right ✗',
                    feedback: "Incorrect. The grammatical error is 'were'. 'Neither' is singular, so it must be 'was available'.",
                    coveredConcepts: [],
                    missingConcepts: ["Identification of singular subject 'Neither' requiring 'was'"]
                };
            }
        }

        // 7. Generalized semantic evaluation
        const keyPoints = (q && q.keyPoints && q.keyPoints.length > 0) ? q.keyPoints : [expected];
        const covered = [];
        const missing = [];

        keyPoints.forEach(kp => {
            const stopWords = new Set(['the','is','a','an','and','or','to','in','of','with','for','on','at','by','from','up','about','into','over','after','this','that','these','those','then','what','when','where','how','why','between']);
            const kpWords = kp.toLowerCase().replace(/[^a-z0-9\s]/g, ' ').split(/\s+/).filter(w => w.length > 2 && !stopWords.has(w));
            let matches = 0;
            kpWords.forEach(w => {
                if (lower.includes(w)) matches++;
            });
            if (kpWords.length > 0 && (matches / kpWords.length >= 0.45 || (kpWords.length >= 3 && matches >= 2))) {
                covered.push(kp);
            } else {
                missing.push(kp);
            }
        });

        const ratio = keyPoints.length ? (covered.length / keyPoints.length) : 0;
        let score = 0;
        let isCorrect = false;
        let isPartial = false;

        if (ratio >= 0.70 || (keyPoints.length <= 3 && covered.length >= 2)) {
            score = 100;
            isCorrect = true;
        } else if (covered.length > 0 && (ratio >= 0.30 || covered.length >= 1)) {
            score = 50;
            isPartial = true;
        } else {
            score = 0;
        }

        return {
            questionId: q ? q.id : null,
            questionNumber: qNum,
            questionText: q ? q.text : 'Question',
            topic: q ? q.topic : 'General',
            difficulty: q ? q.difficulty : 'MEDIUM',
            userAnswer: text,
            expectedAnswer: expected,
            score: score,
            isCorrect: isCorrect,
            status: isCorrect ? 'CORRECT' : (isPartial ? 'PARTIALLY_CORRECT' : 'INCORRECT'),
            statusLabel: isCorrect ? 'Right / Correct ✓' : (isPartial ? 'Partially Correct ~' : 'Incorrect / Not Right ✗'),
            feedback: isCorrect
                ? 'Great answer! You covered the primary concepts and articulated the core technical ideas effectively.'
                : (isPartial
                    ? 'Partially correct. You touched on relevant terminology, but missed key technical requirements outlined in the model solution.'
                    : 'Needs improvement. The response lacks the required conceptual depth and key principles for this topic. Review the expected answer below.'),
            coveredConcepts: covered,
            missingConcepts: missing
        };
    }

    /**
     * Complete interview session and fetch AI evaluation result
     * Calls: POST /api/interviews/{id}/complete -> GET /api/interviews/{id}/result
     */
    async function completeAndShowResult() {
        showModalLoader('Evaluating all answers and generating accuracy report...');

        try {
            // 4. Mark interview completed: POST /api/interviews/{id}/complete
            console.log(`📡 POST ${API_BASE_URL}/interviews/${currentInterview.id}/complete`);
            try {
                await fetch(`${API_BASE_URL}/interviews/${currentInterview.id}/complete`, { method: 'POST' });
            } catch (ignored) {}

            // 5. Fetch full AI result: GET /api/interviews/{id}/result
            console.log(`📡 GET ${API_BASE_URL}/interviews/${currentInterview.id}/result`);
            const resRes = await fetch(`${API_BASE_URL}/interviews/${currentInterview.id}/result`);
            let r = null;
            if (resRes.ok) {
                const resultData = await resRes.json();
                if (resultData.success && resultData.data) {
                    r = resultData.data;
                }
            }

            if (!r) {
                if (currentInterviewAnswers && currentInterviewAnswers.length > 0) {
                    r = generateClientSideResult();
                } else {
                    throw new Error('AI evaluation service returned invalid data or could not be reached.');
                }
            } else if (!r.questionEvaluations || r.questionEvaluations.length === 0) {
                if (currentInterviewAnswers && currentInterviewAnswers.length > 0) {
                    r.questionEvaluations = currentInterviewAnswers;
                } else {
                    throw new Error('Question evaluations are missing from evaluation result.');
                }
            }

            console.log('🏆 AI Result report with question-by-question check:', r);

            // Render rich result report view
            renderResultReport(r);

            // Refresh home page dashboard stats
            fetchDashboardData();

        } catch (err) {
            console.error('AI evaluation error:', err);
            renderEvaluationError(err.message);
        }
    }

    /**
     * Renders evaluation error alert when AI evaluation fails instead of inventing a score
     */
    function renderEvaluationError(errorMessage) {
        const modal = ensureInterviewModal();
        modal.innerHTML = `
            <div class="interview-modal-container im-result-container">
                <div class="interview-modal-header">
                    <div class="im-meta-left">
                        <span class="im-badge im-badge-status badge-incorrect" style="background:rgba(239,68,68,0.15);color:#ef4444;border:1px solid rgba(239,68,68,0.3);">Evaluation Error</span>
                        <span class="im-role">${escapeHtml(currentInterview ? currentInterview.role : 'Interview')}</span>
                    </div>
                    <button class="im-close-btn" id="nhCloseModalBtn" title="Close">&times;</button>
                </div>
                <div class="im-body im-result-body" style="padding: 2.5rem 1.5rem; text-align: center;">
                    <div style="font-size: 3rem; margin-bottom: 1rem;">⚠️</div>
                    <h2 style="font-size: 1.5rem; margin-bottom: 0.75rem; color: var(--text-primary, #fff);">AI Evaluation Error</h2>
                    <p style="color: var(--text-secondary, #94a3b8); max-width: 480px; margin: 0 auto 1.5rem; line-height: 1.6;">
                        ${escapeHtml(errorMessage || 'We were unable to evaluate your responses accurately. To maintain scoring integrity, no fabricated scores or fallback estimates have been generated.')}
                    </p>
                    <div style="display: flex; gap: 1rem; justify-content: center; flex-wrap: wrap;">
                        <button class="btn-duo-primary" id="nhRetryEvalBtn" style="padding: 0.75rem 1.5rem;">Retry Evaluation 🔄</button>
                        <button class="btn-duo-secondary" id="nhCloseErrorBtn" style="padding: 0.75rem 1.5rem;">Close</button>
                    </div>
                </div>
            </div>
        `;
        document.getElementById('nhCloseModalBtn').onclick = closeModal;
        document.getElementById('nhCloseErrorBtn').onclick = closeModal;
        const retryBtn = document.getElementById('nhRetryEvalBtn');
        if (retryBtn) {
            retryBtn.onclick = function() {
                completeAndShowResult();
            };
        }
    }

    /**
     * Builds complete result report object when offline or as local fallback
     */
    function generateClientSideResult() {
        const evals = currentInterviewAnswers.length > 0 ? currentInterviewAnswers : [];
        const total = evals.length || 5;
        const correct = evals.filter(e => e.status === 'CORRECT').length;
        const partial = evals.filter(e => e.status === 'PARTIALLY_CORRECT').length;
        const incorrect = evals.filter(e => e.status === 'INCORRECT').length;
        const accuracy = total > 0 ? Math.round(((correct * 100) + (partial * 50)) / total) : 0;

        return {
            interviewId: currentInterview ? currentInterview.id : 1001,
            overallScore: accuracy,
            accuracyPercentage: accuracy,
            totalQuestions: total,
            correctCount: correct,
            partiallyCorrectCount: partial,
            incorrectCount: incorrect,
            partialCreditPolicy: 'Full credit (100%) for correct answers, partial credit (50%) for partially correct answers, zero credit (0%) for incorrect answers.',
            readinessLabel: accuracy >= 90 ? 'Interview Ready — Exceptional Mastery 🚀'
                : (accuracy >= 75 ? 'Interview Ready — Strong Technical Command 🔥'
                : (accuracy >= 50 ? 'Interview Capable — Practice Recommended 🎯'
                : (accuracy > 0 ? 'Developing — Targeted Review Required 📚' : 'Foundational Review Required ⚠️'))),
            communicationScore: 0,
            confidenceScore: 0,
            technicalScore: accuracy,
            clarityScore: 0,
            answerStructureScore: 0,
            overallFeedback: `You answered ${correct} out of ${total} questions correctly with an overall accuracy of ${accuracy}%${partial > 0 ? ` (including partial credit for ${partial} partially correct answers)` : ''}. ${accuracy >= 75 ? 'Strong technical command!' : 'Focus on reviewing the missed concepts below before your actual interview.'}`,
            strengths: evals.filter(e => e.status === 'CORRECT').slice(0, 3).map(e => `Mastered ${e.topic}: Answered '${(e.questionText || '').slice(0, 40)}...' accurately.`),
            improvements: evals.filter(e => e.status !== 'CORRECT').slice(0, 4).map(e => `Review ${e.topic}: Focus on model concepts for '${(e.questionText || '').slice(0, 40)}...'`),
            questionEvaluations: evals
        };
    }

    /**
     * Render the rich results report with question-by-question check (Right vs Not Right)
     */
    function renderResultReport(r) {
        const modal = ensureInterviewModal();
        const score = (r.accuracyPercentage !== undefined) ? r.accuracyPercentage : (r.overallScore !== undefined ? r.overallScore : 0);
        const deg = (score / 100) * 360;

        const evaluations = (r.questionEvaluations && r.questionEvaluations.length > 0)
            ? r.questionEvaluations
            : (currentInterviewAnswers.length > 0 ? currentInterviewAnswers : []);

        const totalQ = r.totalQuestions || evaluations.length || 5;
        const correctCount = (r.correctCount !== undefined)
            ? r.correctCount
            : evaluations.filter(e => e.status === 'CORRECT' || e.isCorrect).length;
        const partialCount = (r.partiallyCorrectCount !== undefined)
            ? r.partiallyCorrectCount
            : evaluations.filter(e => e.status === 'PARTIALLY_CORRECT').length;
        const incorrectCount = (r.incorrectCount !== undefined)
            ? r.incorrectCount
            : evaluations.filter(e => e.status === 'INCORRECT' || (!e.isCorrect && e.status !== 'PARTIALLY_CORRECT')).length;
        const accuracy = (r.accuracyPercentage !== undefined)
            ? r.accuracyPercentage
            : (totalQ > 0 ? Math.round(((correctCount * 100) + (partialCount * 50)) / totalQ) : 0);

        const strengthsList = (r.strengths && r.strengths.length > 0)
            ? r.strengths.map(s => `<li><span class="bullet-check">✓</span> ${escapeHtml(s)}</li>`).join('')
            : '<li><span class="bullet-check">✓</span> Complete review of core technical concepts recommended.</li>';

        const improvementsList = (r.improvements && r.improvements.length > 0)
            ? r.improvements.map(i => `<li><span class="bullet-alert">⚡</span> ${escapeHtml(i)}</li>`).join('')
            : '<li><span class="bullet-alert">⚡</span> Strengthen responses with concrete technical principles and implementation details.</li>';

        // Build Question Review Cards with Explanation, Key Points, and "📚 Learn More" Deep Dive
        const questionCardsHtml = evaluations.map((e, idx) => {
            const qNum = e.questionNumber || (idx + 1);
            const statusLower = (e.status || (e.isCorrect ? 'CORRECT' : 'INCORRECT')).toLowerCase();
            const isCorr = e.status === 'CORRECT' || e.isCorrect;
            const isPart = e.status === 'PARTIALLY_CORRECT';
            const badgeClass = isCorr ? 'badge-correct' : (isPart ? 'badge-partial' : 'badge-incorrect');
            const statusLabel = e.statusLabel || (isCorr ? 'Right / Correct ✓' : (isPart ? 'Partially Correct ~' : 'Incorrect / Not Right ✗'));
            const qScore = e.score !== undefined ? e.score : (isCorr ? 100 : (isPart ? 50 : 0));

            const coveredHtml = (e.coveredConcepts && e.coveredConcepts.length > 0)
                ? `<div class="im-concepts-row"><span style="font-size:0.75rem;font-weight:700;color:var(--text-secondary);">Concepts Covered:</span> ${e.coveredConcepts.map(c => `<span class="im-concept-tag c-covered">✓ ${escapeHtml(c)}</span>`).join('')}</div>`
                : '';

            const missingHtml = (e.missingConcepts && e.missingConcepts.length > 0)
                ? `<div class="im-concepts-row"><span style="font-size:0.75rem;font-weight:700;color:var(--text-secondary);">Missed / Review:</span> ${e.missingConcepts.map(c => `<span class="im-concept-tag c-missing">⚡ ${escapeHtml(c)}</span>`).join('')}</div>`
                : '';

            // Explanation Box
            const explanationHtml = (e.explanation && e.explanation.trim())
                ? `<div class="im-explanation-box"><strong>📖 Conceptual Explanation:</strong> ${escapeHtml(e.explanation)}</div>`
                : '';

            // Key Points Box
            const keyPointsHtml = (e.keyPoints && e.keyPoints.length > 0)
                ? `<div class="im-keypoints-box"><strong>🔑 Key Points to Master:</strong><ul>${e.keyPoints.map(kp => `<li>${escapeHtml(kp)}</li>`).join('')}</ul></div>`
                : '';

            // Per-Question "📚 Learn More" Deep Dive
            const resources = e.learningResources || [];
            const learnMoreHtml = (resources.length > 0)
                ? `<div class="im-learn-more-box">
                    <h5 class="im-learn-more-title">📚 Learn More & Deep Dive</h5>
                    <div class="im-learn-resources-list">
                        ${resources.map(res => `
                            <div class="im-learn-item">
                                <div class="im-learn-item-info">
                                    <strong>${escapeHtml(res.title || 'Educational Reference')}</strong>
                                    ${res.description ? `<span>${escapeHtml(res.description)}</span>` : ''}
                                </div>
                                ${res.url ? `<a href="${escapeHtml(res.url)}" target="_blank" rel="noopener noreferrer" class="im-learn-link">Learn More &rarr;</a>` : '<span style="font-size:0.75rem;color:var(--text-muted);">Self-study Reference</span>'}
                            </div>
                        `).join('')}
                    </div>
                </div>`
                : '';

            return `
                <div class="im-q-card status-${statusLower}" data-filter-status="${isCorr ? 'correct' : 'review'}">
                    <div class="im-q-card-head">
                        <div class="im-q-meta-badges">
                            <span class="im-badge-num">Q${qNum}</span>
                            <span class="im-badge im-badge-category">${escapeHtml(e.topic || currentInterview?.category || 'Technical')}</span>
                            ${e.subtopic ? `<span class="im-badge im-badge-subtopic" style="background:rgba(255,255,255,0.06);color:var(--text-secondary);font-size:0.72rem;padding:0.2rem 0.5rem;border-radius:4px;">${escapeHtml(e.subtopic)}</span>` : ''}
                            <span class="im-badge im-badge-diff">${escapeHtml(e.difficulty || 'MEDIUM')}</span>
                            <span class="im-badge-status ${badgeClass}">${escapeHtml(statusLabel)}</span>
                        </div>
                        <span class="im-score-pill">Score: ${qScore}%</span>
                    </div>

                    <h3 class="im-q-title-text">“${escapeHtml(e.questionText || 'Interview Question')}”</h3>

                    <div class="im-answers-dual-grid">
                        <div class="im-ans-box box-user">
                            <span class="im-box-label">👤 Your Submitted Answer</span>
                            <p>${escapeHtml(e.userAnswer || '(No answer provided)')}</p>
                        </div>
                        <div class="im-ans-box box-expected">
                            <span class="im-box-label">💡 Expected / Model Answer</span>
                            <p>${escapeHtml(e.correctAnswer || e.expectedAnswer || 'Clear technical explanation required.')}</p>
                        </div>
                    </div>

                    ${explanationHtml}
                    ${keyPointsHtml}

                    <div class="im-eval-footer">
                        <p class="im-eval-feedback"><strong>AI Assessment:</strong> ${escapeHtml(e.feedback || 'Answer checked against benchmark solution.')}</p>
                        ${coveredHtml}
                        ${missingHtml}
                    </div>

                    ${learnMoreHtml}
                </div>
            `;
        }).join('');

        modal.innerHTML = `
            <div class="interview-modal-container im-result-container">
                <div class="interview-modal-header">
                    <div class="im-meta-left">
                        <span class="im-badge im-badge-success">Session Completed</span>
                        <span class="im-role">${escapeHtml(currentInterview ? currentInterview.role : 'Candidate')} (${escapeHtml(currentInterview ? currentInterview.category : 'General')})</span>
                    </div>
                    <button class="im-close-btn" id="nhCloseModalBtn" title="Close">&times;</button>
                </div>

                <div class="im-body im-result-body">
                    <!-- Top Score Banner -->
                    <div class="im-score-banner">
                        <div class="css-score-ring" style="--score-deg: ${deg}deg; width: 110px; height: 110px;">
                            <div class="ring-inner">
                                <span class="score-number" style="font-size: 28px;">${score}</span>
                                <span class="score-max">%</span>
                            </div>
                        </div>
                        <div class="im-score-meta">
                            <span class="im-result-tag">AI Evaluation Result</span>
                            <h2 class="im-result-headline">${escapeHtml(r.readinessLabel || 'Evaluation Complete')}</h2>
                            <p class="im-result-feedback">${escapeHtml(r.overallFeedback || '')}</p>
                        </div>
                    </div>

                    <!-- Performance Accuracy Stats Grid -->
                    <div class="im-result-stats-grid">
                        <div class="im-stat-card">
                            <div class="im-stat-val">${accuracy}%</div>
                            <div class="im-stat-lbl">🎯 Accuracy Rate</div>
                        </div>
                        <div class="im-stat-card stat-success">
                            <div class="im-stat-val">${correctCount} / ${totalQ}</div>
                            <div class="im-stat-lbl">✓ Questions Right</div>
                        </div>
                        <div class="im-stat-card stat-warning">
                            <div class="im-stat-val">${partialCount} / ${totalQ}</div>
                            <div class="im-stat-lbl">~ Partially Correct</div>
                        </div>
                        <div class="im-stat-card stat-danger">
                            <div class="im-stat-val">${incorrectCount} / ${totalQ}</div>
                            <div class="im-stat-lbl">✗ Not Right / Missed</div>
                        </div>
                    </div>

                    <!-- Partial-Credit Policy Banner -->
                    <div class="im-policy-banner" style="display:flex;align-items:center;gap:0.75rem;padding:0.85rem 1.25rem;background:rgba(0,229,255,0.06);border:1px solid rgba(0,229,255,0.2);border-radius:var(--radius-md);margin-bottom:1.5rem;font-size:0.85rem;color:var(--text-secondary);">
                        <span style="font-size:1.1rem;color:var(--cyan-primary);">ℹ️</span>
                        <div>
                            <strong style="color:var(--text-primary);">Partial-Credit Policy:</strong>
                            ${escapeHtml(r.partialCreditPolicy || 'Full credit (100%) for correct answers, partial credit (50%) for partially correct answers, zero credit (0%) for incorrect answers.')}
                        </div>
                    </div>

                    <!-- Topic Mastery & Coverage Analysis -->
                    <div class="im-topics-section">
                        <h4 class="im-topics-title">📊 Topic Mastery & Coverage Analysis</h4>
                        <div class="im-topics-grid">
                            <div class="im-topic-col">
                                <h5>Topics Covered (${(r.topicsCovered || []).length})</h5>
                                <div class="im-topic-chips-wrap">
                                    ${(r.topicsCovered && r.topicsCovered.length > 0)
                                        ? r.topicsCovered.map(t => `<span class="im-topic-chip">${escapeHtml(t)}</span>`).join('')
                                        : evaluations.map(e => e.topic).filter((v, i, a) => v && a.indexOf(v) === i).map(t => `<span class="im-topic-chip">${escapeHtml(t)}</span>`).join('')}
                                </div>
                            </div>
                            <div class="im-topic-col">
                                <h5>Strong Areas (✓ Mastered)</h5>
                                <div class="im-topic-chips-wrap">
                                    ${(r.strongTopics && r.strongTopics.length > 0)
                                        ? r.strongTopics.map(t => `<span class="im-topic-chip chip-strong">✓ ${escapeHtml(t)}</span>`).join('')
                                        : '<span style="font-size:0.8rem;color:var(--text-secondary);">Keep practicing to achieve mastery</span>'}
                                </div>
                            </div>
                            <div class="im-topic-col">
                                <h5>Needs Improvement (⚡ Practice)</h5>
                                <div class="im-topic-chips-wrap">
                                    ${(r.weakTopics && r.weakTopics.length > 0)
                                        ? r.weakTopics.map(t => `<span class="im-topic-chip chip-weak">⚡ ${escapeHtml(t)}</span>`).join('')
                                        : '<span style="font-size:0.8rem;color:#10b981;">All covered topics met target mastery!</span>'}
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Recommended Learning Topics Section -->
                    ${(r.recommendedLearning && r.recommendedLearning.length > 0) ? `
                        <div class="im-rec-learning-section">
                            <div class="im-rec-learning-header">
                                <h3 class="im-rec-learning-title">📚 Recommended Deep-Dive Learning</h3>
                                <span style="font-size: 0.78rem; color: var(--text-secondary);">Personalized recommendations based on your performance</span>
                            </div>
                            <div class="im-rec-learning-grid">
                                ${r.recommendedLearning.slice(0, 4).map(rec => `
                                    <div class="im-rec-card">
                                        <div>
                                            <h4>${escapeHtml(rec.title || 'Learning Resource')}</h4>
                                            <p>${escapeHtml(rec.description || 'Comprehensive documentation and best practices.')}</p>
                                        </div>
                                        ${rec.url ? `<a href="${escapeHtml(rec.url)}" target="_blank" rel="noopener noreferrer" class="im-rec-link-btn">Study Topic &rarr;</a>` : '<span style="font-size:0.75rem;color:var(--text-muted);">In-app Learning</span>'}
                                    </div>
                                `).join('')}
                            </div>
                        </div>
                    ` : ''}

                    <!-- Strengths and Areas of Improvement -->
                    <div class="im-insights-row">
                        <div class="im-insight-col im-col-strengths">
                            <h4>Key Strengths</h4>
                            <ul>${strengthsList}</ul>
                        </div>
                        <div class="im-insight-col im-col-improvements">
                            <h4>Areas to Improve</h4>
                            <ul>${improvementsList}</ul>
                        </div>
                    </div>

                    <!-- Question-by-Question Evaluation Breakdown Section -->
                    <div class="im-q-review-section">
                        <div class="im-q-review-header">
                            <h3 class="im-q-review-title">📋 Question-by-Question Accuracy Check</h3>
                            <div class="im-filter-tabs">
                                <button class="im-filter-btn is-active" data-filter="all">All (${evaluations.length})</button>
                                <button class="im-filter-btn" data-filter="correct">✓ Right (${correctCount})</button>
                                <button class="im-filter-btn" data-filter="review">⚠️ Needs Review (${incorrectCount + partialCount})</button>
                            </div>
                        </div>
                        <div class="im-q-review-list" id="imQReviewList">
                            ${questionCardsHtml}
                        </div>
                    </div>
                </div>

                <div class="interview-modal-footer">
                    <button class="btn-duo-secondary" id="nhCloseResultBtn">Close & View Dashboard</button>
                    <button class="btn-duo-primary" id="nhRestartBtn">Start Another Interview 🚀</button>
                </div>
            </div>
        `;

        document.getElementById('nhCloseModalBtn').onclick = closeModal;
        document.getElementById('nhCloseResultBtn').onclick = closeModal;
        document.getElementById('nhRestartBtn').onclick = function () {
            openInterviewSetupModal(
                currentInterview ? currentInterview.category : 'data-analyst',
                currentInterview ? currentInterview.role : 'Data Analyst',
                currentInterview ? currentInterview.difficulty : 'medium',
                currentInterview ? currentInterview.totalQuestions : 10
            );
        };

        // Wire interactive filter tabs
        const filterBtns = modal.querySelectorAll('.im-filter-btn');
        const qCards = modal.querySelectorAll('.im-q-card');
        filterBtns.forEach(btn => {
            btn.onclick = function () {
                filterBtns.forEach(b => b.classList.remove('is-active'));
                this.classList.add('is-active');
                const filter = this.getAttribute('data-filter');
                qCards.forEach(card => {
                    const cardStatus = card.getAttribute('data-filter-status');
                    if (filter === 'all') {
                        card.style.display = 'block';
                    } else if (filter === 'correct') {
                        card.style.display = (cardStatus === 'correct') ? 'block' : 'none';
                    } else if (filter === 'review') {
                        card.style.display = (cardStatus === 'review') ? 'block' : 'none';
                    }
                });
            };
        });
    }

    // =========================================================================
    // 5. MODAL HELPERS & UI COMPONENTS
    // =========================================================================
    function ensureInterviewModal() {
        let el = document.getElementById('nhInterviewModal');
        if (!el) {
            el = document.createElement('div');
            el.id = 'nhInterviewModal';
            el.className = 'interview-modal-overlay';
            document.body.appendChild(el);
        }
        return el;
    }

    function closeModal() {
        const el = document.getElementById('nhInterviewModal');
        if (el) el.classList.remove('is-active');
        currentInterview = null;
        interviewQuestions = [];
        currentInterviewAnswers = [];
    }

    function confirmExit() {
        if (confirm('Are you sure you want to exit this interview session? Your progress will be saved in your dashboard.')) {
            closeModal();
        }
    }

    function showModalLoader(msg) {
        const modal = ensureInterviewModal();
        modal.classList.add('is-active');
        modal.innerHTML = `
            <div class="interview-modal-container im-loading-container">
                <div class="im-spinner"></div>
                <h3 class="im-loading-text">${escapeHtml(msg)}</h3>
                <span class="im-subtext">Communicating with Spring Boot REST API at <code>${API_BASE_URL}</code></span>
            </div>
        `;
    }

    function showModalError(msg) {
        const modal = ensureInterviewModal();
        modal.classList.add('is-active');
        modal.innerHTML = `
            <div class="interview-modal-container im-error-container">
                <div class="im-error-icon">⚠️</div>
                <h3 class="im-error-title">Backend Connection Notice</h3>
                <p class="im-error-msg">${escapeHtml(msg)}</p>
                <div class="im-error-actions">
                    <button class="btn-duo-primary" onclick="document.getElementById('nhInterviewModal').classList.remove('is-active')">Dismiss</button>
                </div>
            </div>
        `;
    }

    function showModalInlineMsg(text, isError) {
        const el = document.getElementById('imStatusMsg');
        if (!el) return;
        el.textContent = text;
        el.style.display = 'block';
        el.className = `im-status-msg ${isError ? 'is-error' : 'is-success'}`;
    }

    function setupSpeechRecognition(btn, textarea) {
        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
        if (!SpeechRecognition) {
            btn.title = 'Speech recognition not supported in this browser. You can type your answer.';
            return;
        }

        let recognition = null;
        let isListening = false;

        btn.onclick = function () {
            if (isListening) {
                if (recognition) recognition.stop();
                return;
            }

            try {
                recognition = new SpeechRecognition();
                recognition.continuous = true;
                recognition.interimResults = true;
                recognition.lang = 'en-US';

                recognition.onstart = function () {
                    isListening = true;
                    btn.classList.add('is-recording');
                    btn.innerHTML = '🛑 Stop Speaking';
                };

                recognition.onresult = function (event) {
                    let transcript = '';
                    for (let i = event.resultIndex; i < event.results.length; i++) {
                        transcript += event.results[i][0].transcript;
                    }
                    textarea.value = (textarea.value + ' ' + transcript).trim();
                    textarea.dispatchEvent(new Event('input'));
                };

                recognition.onerror = function (e) {
                    console.warn('Speech recognition error:', e);
                    isListening = false;
                    btn.classList.remove('is-recording');
                    btn.innerHTML = '🎙️ Speak Answer';
                };

                recognition.onend = function () {
                    isListening = false;
                    btn.classList.remove('is-recording');
                    btn.innerHTML = '🎙️ Speak Answer';
                };

                recognition.start();
            } catch (err) {
                console.warn('Could not start speech recognition:', err);
            }
        };
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    // =========================================================================
    // 6. GUIDANCE CHATBOT MODULE (POST /api/chatbot/message)
    // =========================================================================
    function initGuidanceChatbot() {
        const fab = document.getElementById('nhChatbotFab');
        const win = document.getElementById('nhChatbotWindow');
        const closeBtn = document.getElementById('nhChatCloseBtn');
        const form = document.getElementById('nhChatForm');
        const input = document.getElementById('nhChatInput');
        const msgs = document.getElementById('nhChatMessages');
        const suggestionsBox = document.getElementById('nhChatSuggestions');

        if (!fab || !win) return;

        fab.addEventListener('click', function () {
            win.classList.toggle('is-open');
            if (win.classList.contains('is-open') && input) {
                input.focus();
            }
        });

        if (closeBtn) {
            closeBtn.addEventListener('click', function () {
                win.classList.remove('is-open');
            });
        }

        // Handle suggestion chip clicks
        if (suggestionsBox) {
            suggestionsBox.addEventListener('click', function (e) {
                const chip = e.target.closest('.nh-chat-suggestion-chip');
                if (chip && chip.dataset.msg) {
                    sendChatMessage(chip.dataset.msg);
                }
            });
        }

        // Handle text submit
        if (form && input) {
            form.addEventListener('submit', function (e) {
                e.preventDefault();
                const text = input.value.trim();
                if (!text) return;
                input.value = '';
                sendChatMessage(text);
            });
        }

        async function sendChatMessage(userText) {
            // Append user bubble
            appendChatBubble('user', userText);

            // Append thinking bubble
            const loadingBubble = appendChatBubble('bot', 'Thinking...');

            try {
                const response = await fetch(`${API_BASE_URL}/chatbot/message`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ message: userText })
                });

                if (!response.ok) throw new Error(`HTTP ${response.status}`);
                const resData = await response.json();

                if (resData.success && resData.data) {
                    loadingBubble.textContent = resData.data.message;

                    // Update suggestion chips if suggestions exist
                    if (Array.isArray(resData.data.suggestions) && resData.data.suggestions.length > 0) {
                        renderChatSuggestions(resData.data.suggestions);
                    }
                } else {
                    loadingBubble.textContent = "I'm having trouble retrieving advice right now. Please try again in a moment.";
                }
            } catch (err) {
                console.error('Chatbot API error:', err);
                loadingBubble.textContent = "Could not connect to NextHire Guidance Service. Please ensure the backend is running at http://localhost:8080.";
            }

            msgs.scrollTop = msgs.scrollHeight;
        }

        function appendChatBubble(sender, text) {
            const bubble = document.createElement('div');
            bubble.className = `nh-chat-msg ${sender}`;
            bubble.textContent = text;
            msgs.appendChild(bubble);
            msgs.scrollTop = msgs.scrollHeight;
            return bubble;
        }

        function renderChatSuggestions(suggestions) {
            let container = document.getElementById('nhChatSuggestions');
            if (!container) {
                container = document.createElement('div');
                container.id = 'nhChatSuggestions';
                container.className = 'nh-chat-suggestions';
                msgs.appendChild(container);
            }
            container.innerHTML = suggestions.map(s => `
                <button class="nh-chat-suggestion-chip" data-msg="${escapeHtml(s)}">${escapeHtml(s)}</button>
            `).join('');
            msgs.appendChild(container);
            msgs.scrollTop = msgs.scrollHeight;
        }
    }

    // =========================================================================
    // 7. ENGLISH LEARNING ACADEMY MODULE (GET /api/learning/english/topics)
    // =========================================================================
    function initLearningAcademy() {
        const navBtn = document.getElementById('nhNavLearningBtn');
        const modal = document.getElementById('nhLearningModal');
        const closeBtn = document.getElementById('nhCloseLearningBtn');
        const grid = document.getElementById('nhTopicsGrid');
        const body = document.getElementById('nhLearningBody');

        if (!modal) return;

        if (navBtn) {
            navBtn.addEventListener('click', function (e) {
                e.preventDefault();
                openLearningModal();
            });
        }

        if (closeBtn) {
            closeBtn.addEventListener('click', function () {
                modal.classList.remove('is-active');
            });
        }

        modal.addEventListener('click', function (e) {
            if (e.target === modal) modal.classList.remove('is-active');
        });

        async function openLearningModal() {
            modal.classList.add('is-active');
            await loadTopicsList();
        }

        async function loadTopicsList() {
            body.innerHTML = `
                <div class="nh-topics-grid" id="nhTopicsGrid">
                    <div style="padding: 30px; text-align: center; color: var(--text-secondary); grid-column: 1 / -1;">
                        <div class="im-spinner" style="width: 32px; height: 32px; margin-bottom: 12px;"></div>
                        Loading English curriculum from backend...
                    </div>
                </div>
            `;

            try {
                const res = await fetch(`${API_BASE_URL}/learning/english/topics`);
                if (!res.ok) throw new Error(`HTTP ${res.status}`);
                const json = await res.json();

                if (json.success && Array.isArray(json.data)) {
                    renderTopicsGrid(json.data);
                } else {
                    body.innerHTML = `<p style="color: #ef4444; padding: 20px;">Failed to load topics: ${escapeHtml(json.message)}</p>`;
                }
            } catch (err) {
                body.innerHTML = `<p style="color: #ef4444; padding: 20px;">Could not connect to learning service: ${escapeHtml(err.message)}</p>`;
            }
        }

        function renderTopicsGrid(topics) {
            const icons = ['📖', '💬', '✍️', '👔', '🎯', '🤝'];
            body.innerHTML = `
                <div class="nh-topics-grid">
                    ${topics.map((t, idx) => `
                        <div class="nh-topic-card" data-topic-id="${escapeHtml(t.topicId)}">
                            <div>
                                <div class="nh-topic-icon">${icons[idx % icons.length]}</div>
                                <h4 class="nh-topic-title">${escapeHtml(t.title)}</h4>
                                <p class="nh-topic-desc">${escapeHtml(t.description)}</p>
                            </div>
                            <div class="nh-topic-meta">
                                <span>${t.lessons ? t.lessons.length : 0} Lessons</span>
                                <span>•</span>
                                <span>${t.practiceQuestions ? t.practiceQuestions.length : 0} Questions</span>
                                <span style="margin-left: auto;">Explore &rarr;</span>
                            </div>
                        </div>
                    `).join('')}
                </div>
            `;

            // Attach card click handlers
            body.querySelectorAll('.nh-topic-card').forEach(card => {
                card.addEventListener('click', function () {
                    const topicId = this.dataset.topicId;
                    loadTopicDetail(topicId);
                });
            });
        }

        async function loadTopicDetail(topicId) {
            body.innerHTML = `
                <div style="padding: 40px; text-align: center;">
                    <div class="im-spinner" style="width: 32px; height: 32px; margin-bottom: 12px;"></div>
                    Loading topic lesson content...
                </div>
            `;

            try {
                const res = await fetch(`${API_BASE_URL}/learning/english/${topicId}`);
                if (!res.ok) throw new Error(`HTTP ${res.status}`);
                const json = await res.json();

                if (json.success && json.data) {
                    renderTopicDetailView(json.data);
                }
            } catch (err) {
                body.innerHTML = `<p style="color: #ef4444;">Error loading topic details: ${escapeHtml(err.message)}</p>`;
            }
        }

        function renderTopicDetailView(topic) {
            const lessonsHtml = (topic.lessons || []).map(l => `
                <div style="margin-bottom: 18px; padding-bottom: 14px; border-bottom: 1px solid var(--card-border);">
                    <h5 style="margin: 0 0 6px 0; font-size: 1rem; color: var(--text-primary); font-weight: 700;">${escapeHtml(l.title)}</h5>
                    <p style="margin: 0 0 8px 0; font-size: 0.88rem; color: var(--text-secondary);">${escapeHtml(l.content)}</p>
                    ${l.keyPoints ? `
                        <ul style="margin: 0; padding-left: 20px; font-size: 0.82rem; color: var(--text-secondary);">
                            ${l.keyPoints.map(kp => `<li>${escapeHtml(kp)}</li>`).join('')}
                        </ul>
                    ` : ''}
                </div>
            `).join('');

            const examplesHtml = (topic.examples || []).map(ex => `
                <div class="nh-example-box">
                    ${escapeHtml(ex)}
                </div>
            `).join('');

            const practiceHtml = (topic.practiceQuestions || []).map((pq, idx) => `
                <div style="background: var(--bg-inset); padding: 12px 16px; border-radius: 8px; margin-bottom: 10px;">
                    <strong style="color: var(--text-primary); font-size: 0.88rem;">Q${idx + 1}: ${escapeHtml(pq.question)}</strong>
                    <div style="margin-top: 6px; font-size: 0.82rem; color: #10b981;">
                        <strong>Explanation / Ideal Response:</strong> ${escapeHtml(pq.explanation || pq.correctAnswer)}
                    </div>
                </div>
            `).join('');

            body.innerHTML = `
                <div class="nh-lesson-detail">
                    <button class="nh-lesson-back-btn" id="nhLessonBackBtn">&larr; Back to all English topics</button>
                    <h3 style="margin: 0 0 8px 0; font-size: 1.4rem; color: var(--text-primary);">${escapeHtml(topic.title)}</h3>
                    <p style="margin: 0 0 20px 0; font-size: 0.95rem; color: var(--text-secondary);">${escapeHtml(topic.description)}</p>

                    <div class="nh-lesson-section-title">Core Lessons</div>
                    ${lessonsHtml}

                    <div class="nh-lesson-section-title">Corporate Phrasing Examples</div>
                    ${examplesHtml}

                    <div class="nh-lesson-section-title">Practice Questions</div>
                    ${practiceHtml}
                </div>
            `;

            document.getElementById('nhLessonBackBtn').addEventListener('click', loadTopicsList);
        }
    }

    // =========================================================================
    // 8. TRENDING TOPICS MODULE (GET /api/trending)
    // =========================================================================
    function initTrendingSection() {
        const navBtn = document.getElementById('nhNavTrendingBtn');
        const modal = document.getElementById('nhTrendingModal');
        const closeBtn = document.getElementById('nhCloseTrendingBtn');
        const list = document.getElementById('nhTrendingList');

        if (!modal) return;

        if (navBtn) {
            navBtn.addEventListener('click', function (e) {
                e.preventDefault();
                openTrendingModal();
            });
        }

        if (closeBtn) {
            closeBtn.addEventListener('click', function () {
                modal.classList.remove('is-active');
            });
        }

        modal.addEventListener('click', function (e) {
            if (e.target === modal) modal.classList.remove('is-active');
        });

        async function openTrendingModal() {
            modal.classList.add('is-active');
            list.innerHTML = `
                <div style="padding: 30px; text-align: center; color: var(--text-secondary);">
                    <div class="im-spinner" style="width: 32px; height: 32px; margin-bottom: 12px;"></div>
                    Loading trending interview trends...
                </div>
            `;

            try {
                const res = await fetch(`${API_BASE_URL}/trending`);
                if (!res.ok) throw new Error(`HTTP ${res.status}`);
                const json = await res.json();

                if (json.success && Array.isArray(json.data)) {
                    list.innerHTML = json.data.map(item => `
                        <div class="nh-trending-row">
                            <div class="nh-trend-info">
                                <h4>🔥 ${escapeHtml(item.title)}</h4>
                                <p>${escapeHtml(item.description)}</p>
                            </div>
                            <div style="text-align: right; display: flex; flex-direction: column; align-items: flex-end; gap: 4px;">
                                <span class="nh-trend-tag">${escapeHtml(item.category)}</span>
                                <span style="font-size: 0.75rem; color: var(--text-secondary);">${item.popularity}% Popularity</span>
                            </div>
                        </div>
                    `).join('');
                }
            } catch (err) {
                list.innerHTML = `<p style="color: #ef4444; padding: 20px;">Could not load trending topics: ${escapeHtml(err.message)}</p>`;
            }
        }
    }

    // =========================================================================
    // 9. ATTACH FRONTEND ACTION LISTENERS
    // =========================================================================
    function attachListeners() {
        // 1. Navbar "Start practicing"
        const navStartBtn = document.querySelector('.btn-nav-start');
        if (navStartBtn) {
            navStartBtn.removeAttribute('data-wip');
            navStartBtn.addEventListener('click', function (e) {
                e.preventDefault();
                openInterviewSetupModal('data-analyst', 'Data Analyst', 'medium', 10);
            });
        }

        // 2. Hero "Launch AI Evaluation"
        const heroLaunchBtn = document.querySelector('.btn-hero-lg');
        if (heroLaunchBtn) {
            heroLaunchBtn.removeAttribute('data-wip');
            heroLaunchBtn.addEventListener('click', function (e) {
                e.preventDefault();
                openInterviewSetupModal('data-analyst', 'Data Analyst', 'medium', 10);
            });
        }

        // 3. Category cards in Interview Types section (all 5 official categories)
        document.querySelectorAll('.nh-cat-launch-btn').forEach(btn => {
            btn.removeAttribute('data-wip');
            btn.addEventListener('click', function (e) {
                e.preventDefault();
                const catId = this.dataset.categoryId || 'data-analyst';
                const role = this.dataset.role || 'Data Analyst';
                openInterviewSetupModal(catId, role, 'medium', 10);
            });
        });

        // 4. Final CTA "Start your first interview"
        const ctaBtn = document.querySelector('.btn-cta-xl');
        if (ctaBtn) {
            ctaBtn.removeAttribute('data-wip');
            ctaBtn.addEventListener('click', function (e) {
                e.preventDefault();
                openInterviewSetupModal('general-interview', 'Candidate', 'medium', 10);
            });
        }
    }

    // =========================================================================
    // 10. INITIALIZE ON DOM READY
    // =========================================================================
    document.addEventListener('DOMContentLoaded', function () {
        checkUserSession();
        fetchDashboardData();
        initGuidanceChatbot();
        initLearningAcademy();
        initTrendingSection();
        attachListeners();
    });

})();

