/**
 * NEXTHIRE GAME ARENA — Frontend Engine & Backend Integration
 * Architecture: Vanilla JavaScript + Web Audio API + REST API Client + Offline Fallback
 */

(function () {
    'use strict';

    // =========================================================================
    // 1. API CONFIGURATION
    // =========================================================================
    const API_BASE_URL = (window.NEXTHIRE_CONFIG && typeof window.NEXTHIRE_CONFIG.getApiUrl === 'function')
        ? window.NEXTHIRE_CONFIG.getApiUrl()
        : ((window.location.hostname === 'localhost' && window.location.port === '8080')
            ? '/api'
            : 'http://localhost:8080/api');

    console.log('🎮 [Game Arena] Initialized. Target API:', API_BASE_URL);

    // =========================================================================
    // 2. STATE MANAGEMENT
    // =========================================================================
    const state = {
        userId: 1,
        user: {
            id: 1,
            firstName: 'Candidate',
            lastName: '',
            email: 'candidate@nexthire.ai',
            streakDays: 7,
            xpPoints: 250
        },
        soundEnabled: localStorage.getItem('nh_arena_sfx') !== 'false',
        highContrast: localStorage.getItem('nh_arena_contrast') === 'true',
        domains: [],
        currentDomain: null,
        currentLevels: [],
        currentLevel: null,
        gameplay: {
            questions: [],
            currentIndex: 0,
            score: 0,
            correctCount: 0,
            comboStreak: 0,
            maxStreak: 0,
            timeRemaining: 30,
            timerInterval: null,
            answers: [], // { question, selectedIndex, correctIndex, isCorrect, timeSpent }
            used5050: false,
            usedHint: false,
            usedTime: false,
            questionStartTime: 0,
            isAnswered: false,
            domainId: '',
            levelId: 1
        },
        stats: {
            totalXp: 250,
            gamesCompleted: 0,
            currentStreak: 7,
            overallAccuracy: 0,
            domainsExplored: 0,
            totalStars: 0,
            maxStars: 75,
            lastPlayedDomain: null,
            lastPlayedLevel: null
        },
        achievements: [],
        leaderboard: [],
        activeView: 'overview'
    };

    // Load active session from localStorage
    try {
        const rawUser = localStorage.getItem('nh_user');
        if (rawUser) {
            const parsed = JSON.parse(rawUser);
            if (parsed && parsed.id) {
                state.userId = parsed.id;
                state.user = Object.assign({}, state.user, parsed);
            }
        }
    } catch (e) {
        console.warn('Could not read user from localStorage:', e);
    }

    // Load saved local arena progress cache
    function loadLocalProgress() {
        try {
            const raw = localStorage.getItem('nh_arena_progress_' + state.userId);
            if (raw) return JSON.parse(raw);
        } catch (e) {}
        return { levels: {}, achievements: [], stats: { gamesCompleted: 0, correct: 0, total: 0 } };
    }

    function saveLocalProgress(prog) {
        try {
            localStorage.setItem('nh_arena_progress_' + state.userId, JSON.stringify(prog));
        } catch (e) {}
    }

    // =========================================================================
    // 3. SYNTHESIZED WEB AUDIO SOUND EFFECTS (Zero External Asset Dependency)
    // =========================================================================
    let audioCtx = null;

    function getAudioContext() {
        if (!audioCtx && typeof (window.AudioContext || window.webkitAudioContext) !== 'undefined') {
            const AudioClass = window.AudioContext || window.webkitAudioContext;
            audioCtx = new AudioClass();
        }
        if (audioCtx && audioCtx.state === 'suspended') {
            audioCtx.resume();
        }
        return audioCtx;
    }

    const sound = {
        playTone: function (freq, type, duration, delay = 0, vol = 0.15) {
            if (!state.soundEnabled) return;
            try {
                const ctx = getAudioContext();
                if (!ctx) return;
                const osc = ctx.createOscillator();
                const gain = ctx.createGain();

                osc.type = type;
                osc.frequency.setValueAtTime(freq, ctx.currentTime + delay);

                gain.gain.setValueAtTime(vol, ctx.currentTime + delay);
                gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + delay + duration);

                osc.connect(gain);
                gain.connect(ctx.destination);

                osc.start(ctx.currentTime + delay);
                osc.stop(ctx.currentTime + delay + duration);
            } catch (e) {}
        },

        correct: function () {
            // Ascending major chord fanfare: E5 -> G#5 -> B5
            sound.playTone(659.25, 'triangle', 0.15, 0, 0.2);
            sound.playTone(830.61, 'triangle', 0.18, 0.08, 0.2);
            sound.playTone(987.77, 'sine', 0.35, 0.16, 0.25);
        },

        wrong: function () {
            // Gentle low downward dissonance
            sound.playTone(220, 'sawtooth', 0.25, 0, 0.15);
            sound.playTone(174.61, 'sawtooth', 0.35, 0.1, 0.18);
        },

        combo: function () {
            // High sparkling chime
            sound.playTone(1046.5, 'sine', 0.1, 0, 0.2);
            sound.playTone(1318.5, 'sine', 0.12, 0.07, 0.2);
            sound.playTone(1567.98, 'sine', 0.25, 0.14, 0.25);
        },

        victory: function () {
            // Majestic fanfare
            const notes = [523.25, 659.25, 783.99, 1046.5];
            notes.forEach((f, idx) => {
                sound.playTone(f, 'triangle', 0.3, idx * 0.1, 0.25);
            });
            sound.playTone(1318.5, 'sine', 0.6, 0.45, 0.3);
        },

        click: function () {
            sound.playTone(600, 'sine', 0.04, 0, 0.06);
        }
    };

    // =========================================================================
    // 4. CANVAS CONFETTI CELEBRATION ENGINE
    // =========================================================================
    function launchVictoryConfetti() {
        const canvas = document.getElementById('confettiCanvas');
        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;

        const particles = [];
        const colors = ['#00e5ff', '#a855f7', '#6366f1', '#10b981', '#f59e0b', '#ec4899', '#ffffff'];

        for (let i = 0; i < 110; i++) {
            particles.push({
                x: canvas.width / 2,
                y: canvas.height / 2,
                w: Math.random() * 9 + 4,
                h: Math.random() * 7 + 4,
                color: colors[Math.floor(Math.random() * colors.length)],
                vx: (Math.random() - 0.5) * 18,
                vy: (Math.random() - 0.7) * 16 - 3,
                gravity: 0.32,
                rotation: Math.random() * 360,
                rotSpeed: (Math.random() - 0.5) * 12,
                opacity: 1
            });
        }

        let animationFrame;
        function update() {
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            let alive = false;

            particles.forEach(p => {
                p.x += p.vx;
                p.y += p.vy;
                p.vy += p.gravity;
                p.rotation += p.rotSpeed;
                p.opacity -= 0.009;

                if (p.opacity > 0) {
                    alive = true;
                    ctx.save();
                    ctx.globalAlpha = Math.max(0, p.opacity);
                    ctx.translate(p.x, p.y);
                    ctx.rotate((p.rotation * Math.PI) / 180);
                    ctx.fillStyle = p.color;
                    ctx.fillRect(-p.w / 2, -p.h / 2, p.w, p.h);
                    ctx.restore();
                }
            });

            if (alive) {
                animationFrame = requestAnimationFrame(update);
            } else {
                ctx.clearRect(0, 0, canvas.width, canvas.height);
            }
        }
        update();
    }

    // =========================================================================
    // 5. VIEW NAVIGATION & ROUTING
    // =========================================================================
    function switchView(viewName, param) {
        sound.click();
        state.activeView = viewName;

        // Hide all screens
        const views = document.querySelectorAll('.arena-view');
        views.forEach(v => v.classList.add('hidden'));

        // Target view
        const targetView = document.getElementById('view-' + viewName);
        if (targetView) {
            targetView.classList.remove('hidden');
        }

        // Update active class on sidebar links
        const navItems = document.querySelectorAll('.sidebar-nav .nav-item');
        navItems.forEach(item => {
            if (item.dataset.view === viewName) {
                item.classList.add('active');
            } else {
                item.classList.remove('active');
            }
        });

        // Update topbar breadcrumbs
        const titleMap = {
            overview: 'Overview',
            careers: 'Explore Careers',
            progress: 'My Progress',
            daily: 'Daily Challenge',
            achievements: 'Achievements',
            leaderboard: 'Leaderboard',
            levels: state.currentDomain ? state.currentDomain.title : 'Level Map',
            gameplay: 'Live Arena',
            results: 'Victory Evaluation'
        };
        const titleEl = document.getElementById('topbarCurrentViewTitle');
        if (titleEl) titleEl.textContent = titleMap[viewName] || 'Arena';

        // Load specific view data
        if (viewName === 'overview') {
            loadOverview();
        } else if (viewName === 'careers') {
            renderCareersExploreView();
        } else if (viewName === 'progress') {
            // Render progress by switching to careers view with progress highlight
            renderCareersExploreView();
        } else if (viewName === 'daily') {
            initDailyChallengeView();
        } else if (viewName === 'achievements') {
            loadAchievements();
        } else if (viewName === 'leaderboard') {
            loadLeaderboard();
        } else if (viewName === 'levels') {
            if (param) loadDomainLevels(param);
        }

        // Close mobile drawer if open
        const sidebar = document.getElementById('arenaSidebar');
        if (sidebar) sidebar.classList.remove('mobile-open');

        // Scroll to top
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    // =========================================================================
    // 6. BACKEND DATA LOADERS & DOM RENDERING
    // =========================================================================
    async function loadOverview() {
        try {
            const res = await fetch(`${API_BASE_URL}/arena/overview?userId=${state.userId}`);
            if (res.ok) {
                const json = await res.json();
                if (json.success && json.data) {
                    state.stats = json.data;
                    state.domains = json.data.domains || [];
                    updateStatsUI();
                    renderDomainsOverview(state.domains);
                    return;
                }
            }
        } catch (e) {
            console.warn('[Arena] Backend offline or slow, loading local cached data:', e.message);
        }
        // Fallback local data
        loadLocalFallbackOverview();
    }

    function updateStatsUI() {
        const xp = state.stats.totalXp !== undefined ? state.stats.totalXp : state.user.xpPoints;
        const streak = state.stats.currentStreak !== undefined ? state.stats.currentStreak : state.user.streakDays;

        setText('topbarXpVal', `${xp} XP`);
        setText('topbarStreakVal', `${streak} Days`);
        setText('sidebarUserXp', xp);
        setText('statsTotalXp', xp);
        setText('statsGamesCompleted', state.stats.gamesCompleted || 0);
        setText('statsCurrentStreak', `${streak} Days`);
        setText('statsAccuracy', `${state.stats.overallAccuracy || 0}%`);
        setText('statsTotalStars', `${state.stats.totalStars || 0} / ${state.stats.maxStars || 75}`);

        // Update player tier
        const tier = Math.max(1, Math.floor(xp / 150) + 1);
        setText('sidebarUserTier', tier);

        // Continue playing button state in Hero
        const ctaBtn = document.getElementById('btnHeroPrimaryCTA');
        const ctaText = document.getElementById('heroPrimaryCtaText');
        if (state.stats.lastPlayedDomain) {
            if (ctaText) ctaText.textContent = 'Continue Playing';
            if (ctaBtn) {
                ctaBtn.onclick = function () {
                    switchView('levels', state.stats.lastPlayedDomain);
                };
            }
        } else {
            if (ctaText) ctaText.textContent = 'Start Exploring';
            if (ctaBtn) {
                ctaBtn.onclick = function () {
                    switchView('careers');
                };
            }
        }
    }

    function renderDomainsOverview(domainsList) {
        const container = document.getElementById('domainsOverviewGrid');
        if (!container) return;
        container.innerHTML = '';

        domainsList.forEach(domain => {
            const card = createDomainCardElement(domain);
            container.appendChild(card);
        });
    }

    function renderCareersExploreView() {
        const container = document.getElementById('domainsExploreGrid');
        if (!container) return;
        container.innerHTML = '';

        state.domains.forEach(domain => {
            const card = createDomainCardElement(domain);
            container.appendChild(card);
        });
    }

    function createDomainCardElement(domain) {
        const card = document.createElement('div');
        card.className = 'domain-card';
        card.style.setProperty('--card-accent', domain.accentColor || '#a855f7');
        card.style.setProperty('--card-glow', domain.glowColor || 'rgba(168, 85, 247, 0.35)');

        const iconEmojiMap = {
            'data-analytics': '📊',
            'data-science': '🧠',
            'software-dev': '💻',
            'cybersecurity': '🛡️',
            'business-aptitude': '💼'
        };
        const emoji = iconEmojiMap[domain.id] || '🎯';

        card.innerHTML = `
            <div class="domain-card-header">
                <div class="domain-icon-badge" style="background: ${domain.accentGradient || 'rgba(255,255,255,0.06)'}; color: ${domain.accentColor}">
                    ${emoji}
                </div>
                <div class="domain-level-counter">
                    <span>⚡ Level ${domain.completedLevels || 0} / ${domain.totalLevels || 5}</span>
                </div>
            </div>
            <h3 class="domain-title">${escapeHTML(domain.title)}</h3>
            <div class="domain-tagline" style="color: ${domain.accentColor}">${escapeHTML(domain.tagline || '')}</div>
            <p class="domain-desc">${escapeHTML(domain.description || '')}</p>
            <div class="domain-topics-list">
                ${(domain.topics || []).slice(0, 4).map(t => `<span class="topic-chip">${escapeHTML(t)}</span>`).join('')}
            </div>
            <div class="domain-card-footer">
                <div class="domain-stars-indicator">
                    <span>★</span>
                    <span>${domain.earnedStars || 0} / ${domain.totalStars || 15} Stars</span>
                </div>
                <button class="btn-play-domain">
                    <span>Play Level</span>
                    <span>➔</span>
                </button>
            </div>
        `;

        card.addEventListener('click', () => {
            state.currentDomain = domain;
            switchView('levels', domain.id);
        });

        return card;
    }

    // =========================================================================
    // 7. LEVEL PROGRESSION MAP VIEW
    // =========================================================================
    async function loadDomainLevels(domainId) {
        try {
            const res = await fetch(`${API_BASE_URL}/arena/domains/${domainId}/levels?userId=${state.userId}`);
            if (res.ok) {
                const json = await res.json();
                if (json.success && json.data) {
                    state.currentLevels = json.data;
                    renderLevelsMap(domainId, state.currentLevels);
                    return;
                }
            }
        } catch (e) {
            console.warn('[Arena] Offline levels fetch, falling back to local levels generator:', e.message);
        }
        loadLocalLevels(domainId);
    }

    function renderLevelsMap(domainId, levels) {
        // Update hero banner
        const domain = state.domains.find(d => d.id === domainId) || {
            title: domainId.replace('-', ' ').toUpperCase(),
            tagline: 'MASTER CAREER TRACK',
            description: 'Conquer progressive levels from syntax rookie to boss champion.'
        };

        setText('mapHeroTitle', domain.title);
        setText('mapHeroTagline', domain.tagline || 'CAREER ROADMAP');
        setText('mapHeroDesc', domain.description || '');

        const container = document.getElementById('levelsPathList');
        if (!container) return;
        container.innerHTML = '';

        levels.forEach(lvl => {
            const card = document.createElement('div');
            card.className = `level-node-card ${lvl.unlocked ? '' : 'locked'} ${lvl.difficulty === 'BOSS' ? 'boss-level' : ''}`;

            const starSymbols = '★'.repeat(lvl.starsEarned || 0) + '☆'.repeat(3 - (lvl.starsEarned || 0));

            card.innerHTML = `
                <div class="level-number-badge">
                    ${lvl.unlocked ? (lvl.difficulty === 'BOSS' ? '👑' : lvl.levelId) : '🔒'}
                </div>
                <div class="level-node-content">
                    <div class="level-header-meta">
                        <span class="difficulty-pill diff-${lvl.difficulty.toLowerCase()}">${lvl.difficulty}</span>
                        <span style="font-size: 0.75rem; color: var(--text-muted); font-weight: 700;">${lvl.badge}</span>
                        <span style="font-size: 0.75rem; color: var(--text-muted); font-weight: 700;">• ${lvl.questionsCount} Questions</span>
                    </div>
                    <div class="level-title-text">${escapeHTML(lvl.title)}</div>
                    <div class="level-subtitle-text">${escapeHTML(lvl.subtitle)}</div>
                </div>
                <div class="level-node-rewards">
                    <span class="reward-stars" title="${lvl.starsEarned} of 3 Stars">${starSymbols}</span>
                    <span class="reward-xp">+${lvl.xpReward} XP</span>
                </div>
            `;

            if (lvl.unlocked) {
                card.addEventListener('click', () => {
                    launchGameplay(domainId, lvl.levelId);
                });
            }

            container.appendChild(card);
        });
    }

    // =========================================================================
    // 8. GAMEPLAY QUIZ ENGINE
    // =========================================================================
    async function launchGameplay(domainId, levelId) {
        sound.click();
        state.gameplay.domainId = domainId;
        state.gameplay.levelId = levelId;
        state.gameplay.currentIndex = 0;
        state.gameplay.score = 0;
        state.gameplay.correctCount = 0;
        state.gameplay.comboStreak = 0;
        state.gameplay.maxStreak = 0;
        state.gameplay.answers = [];
        state.gameplay.used5050 = false;
        state.gameplay.usedHint = false;
        state.gameplay.usedTime = false;

        // Reset lifeline button states
        resetLifelineButtons();

        // Fetch questions from backend
        try {
            const res = await fetch(`${API_BASE_URL}/arena/levels/${domainId}/${levelId}/questions`);
            if (res.ok) {
                const json = await res.json();
                if (json.success && json.data && json.data.length > 0) {
                    state.gameplay.questions = json.data;
                    switchView('gameplay');
                    presentCurrentQuestion();
                    return;
                }
            }
        } catch (e) {
            console.warn('[Arena] Offline question fetch, using client fallback question bank:', e.message);
        }

        // Fallback local questions
        state.gameplay.questions = getLocalQuestions(domainId, levelId);
        switchView('gameplay');
        presentCurrentQuestion();
    }

    function presentCurrentQuestion() {
        const q = state.gameplay.questions[state.gameplay.currentIndex];
        if (!q) {
            finishLevelQuiz();
            return;
        }

        state.gameplay.isAnswered = false;
        state.gameplay.questionStartTime = Date.now();

        // Update HUD
        const total = state.gameplay.questions.length;
        setText('gameplayQuestionStep', `Question ${state.gameplay.currentIndex + 1} of ${total}`);
        
        const domainObj = state.domains.find(d => d.id === state.gameplay.domainId);
        const domainTitle = domainObj ? domainObj.title : 'Arena';
        setText('gameplayDomainTitle', `${domainTitle} • Level ${state.gameplay.levelId}`);

        // Update live score and combo
        updateGameplayHud();

        // Render Question Text & Code Snippet
        setText('gameplayQuestionText', q.text);
        const codeEl = document.getElementById('gameplayCodeSnippet');
        if (codeEl) {
            if (q.codeSnippet && q.codeSnippet.trim()) {
                codeEl.textContent = q.codeSnippet;
                codeEl.style.display = 'block';
            } else {
                codeEl.style.display = 'none';
            }
        }

        // Render 4 Options
        const optionsContainer = document.getElementById('gameplayOptionsList');
        if (optionsContainer) {
            optionsContainer.innerHTML = '';
            const letters = ['A', 'B', 'C', 'D'];

            (q.options || []).forEach((optText, idx) => {
                const btn = document.createElement('button');
                btn.className = 'option-btn';
                btn.dataset.index = idx;
                btn.innerHTML = `
                    <div class="option-letter-badge">${letters[idx]}</div>
                    <div class="option-text">${escapeHTML(optText)}</div>
                `;

                btn.addEventListener('click', () => {
                    handleOptionSelected(idx, btn);
                });

                optionsContainer.appendChild(btn);
            });
        }

        // Hide explanation and next button
        const expPanel = document.getElementById('gameplayExplanationPanel');
        if (expPanel) expPanel.style.display = 'none';

        const nextBtn = document.getElementById('btnNextQuestion');
        if (nextBtn) nextBtn.style.display = 'none';

        // Start Question Timer (30s per question)
        startQuestionTimer(30);
    }

    function startQuestionTimer(seconds) {
        clearInterval(state.gameplay.timerInterval);
        state.gameplay.timeRemaining = seconds;

        const timerEl = document.getElementById('gameplayTimerCircle');
        if (timerEl) {
            timerEl.textContent = seconds;
            timerEl.classList.remove('warning');
        }

        state.gameplay.timerInterval = setInterval(() => {
            state.gameplay.timeRemaining--;
            if (timerEl) {
                timerEl.textContent = state.gameplay.timeRemaining;
                if (state.gameplay.timeRemaining <= 7) {
                    timerEl.classList.add('warning');
                }
            }

            if (state.gameplay.timeRemaining <= 0) {
                clearInterval(state.gameplay.timerInterval);
                handleTimeOut();
            }
        }, 1000);
    }

    function handleOptionSelected(selectedIndex, selectedBtn) {
        if (state.gameplay.isAnswered) return;
        state.gameplay.isAnswered = true;
        clearInterval(state.gameplay.timerInterval);

        const q = state.gameplay.questions[state.gameplay.currentIndex];
        const isCorrect = (selectedIndex === q.correctIndex);
        const timeSpent = Math.round((Date.now() - state.gameplay.questionStartTime) / 1000);

        // Record Answer
        state.gameplay.answers.push({
            question: q,
            selectedIndex: selectedIndex,
            correctIndex: q.correctIndex,
            isCorrect: isCorrect,
            timeSpent: timeSpent
        });

        // Highlight options
        const allOptionBtns = document.querySelectorAll('.option-btn');
        allOptionBtns.forEach((btn, idx) => {
            btn.disabled = true;
            if (idx === q.correctIndex) {
                btn.classList.add('correct');
            } else if (idx === selectedIndex && !isCorrect) {
                btn.classList.add('wrong');
            }
        });

        // Scoring & Sound
        if (isCorrect) {
            state.gameplay.correctCount++;
            state.gameplay.comboStreak++;
            if (state.gameplay.comboStreak > state.gameplay.maxStreak) {
                state.gameplay.maxStreak = state.gameplay.comboStreak;
            }

            // Streak multiplier: 1x, 1.5x, 2x, 3x
            let mult = 1.0;
            if (state.gameplay.comboStreak >= 4) mult = 3.0;
            else if (state.gameplay.comboStreak >= 3) mult = 2.0;
            else if (state.gameplay.comboStreak >= 2) mult = 1.5;

            const basePts = 20;
            const earnedPts = Math.round(basePts * mult);
            state.gameplay.score += earnedPts;

            if (state.gameplay.comboStreak >= 2) {
                sound.combo();
            } else {
                sound.correct();
            }
        } else {
            state.gameplay.comboStreak = 0;
            sound.wrong();
        }

        updateGameplayHud();

        // Reveal Explanation
        showExplanationPanel(q, isCorrect);

        // Show Next Button
        const nextBtn = document.getElementById('btnNextQuestion');
        const isLast = (state.gameplay.currentIndex >= state.gameplay.questions.length - 1);
        setText('nextQuestionBtnText', isLast ? 'Complete Evaluation' : 'Next Question');
        if (nextBtn) {
            nextBtn.style.display = 'inline-flex';
            nextBtn.onclick = function () {
                state.gameplay.currentIndex++;
                presentCurrentQuestion();
            };
        }
    }

    function handleTimeOut() {
        if (state.gameplay.isAnswered) return;
        state.gameplay.isAnswered = true;

        const q = state.gameplay.questions[state.gameplay.currentIndex];
        state.gameplay.comboStreak = 0;
        sound.wrong();

        state.gameplay.answers.push({
            question: q,
            selectedIndex: -1,
            correctIndex: q.correctIndex,
            isCorrect: false,
            timeSpent: 30
        });

        // Highlight correct
        const allOptionBtns = document.querySelectorAll('.option-btn');
        allOptionBtns.forEach((btn, idx) => {
            btn.disabled = true;
            if (idx === q.correctIndex) {
                btn.classList.add('correct');
            }
        });

        updateGameplayHud();
        showExplanationPanel(q, false, 'Time expired on this challenge.');

        const nextBtn = document.getElementById('btnNextQuestion');
        const isLast = (state.gameplay.currentIndex >= state.gameplay.questions.length - 1);
        setText('nextQuestionBtnText', isLast ? 'Complete Evaluation' : 'Next Question');
        if (nextBtn) {
            nextBtn.style.display = 'inline-flex';
            nextBtn.onclick = function () {
                state.gameplay.currentIndex++;
                presentCurrentQuestion();
            };
        }
    }

    function showExplanationPanel(q, isCorrect, customHeader) {
        const expPanel = document.getElementById('gameplayExplanationPanel');
        if (!expPanel) return;

        setText('explanationIcon', isCorrect ? '✅' : '💡');
        setText('explanationTitle', customHeader || (isCorrect ? 'Correct! Concept Breakdown:' : 'Insight & Correct Breakdown:'));
        setText('explanationBody', q.explanation || 'Review the core question principle.');
        setText('conceptText', q.keyConcept || q.topic || 'Interview Concept');

        expPanel.style.display = 'block';
    }

    function updateGameplayHud() {
        setText('gameplayCurrentXpVal', `+${state.gameplay.score} XP`);

        const comboPill = document.getElementById('gameplayComboPill');
        const comboText = document.getElementById('comboMultiplierText');
        if (comboPill && comboText) {
            if (state.gameplay.comboStreak >= 2) {
                comboPill.style.display = 'inline-flex';
                let mult = (state.gameplay.comboStreak >= 4) ? '3x' : (state.gameplay.comboStreak >= 3 ? '2x' : '1.5x');
                comboText.textContent = `${mult} Combo (${state.gameplay.comboStreak} In A Row)`;
            } else {
                comboPill.style.display = 'none';
            }
        }
    }

    // Lifeline Handlers
    function resetLifelineButtons() {
        const b5050 = document.getElementById('btnLifeline5050');
        const bHint = document.getElementById('btnLifelineHint');
        const bTime = document.getElementById('btnLifelineTime');

        if (b5050) {
            b5050.disabled = false;
            b5050.onclick = use5050Lifeline;
        }
        if (bHint) {
            bHint.disabled = false;
            bHint.onclick = useHintLifeline;
        }
        if (bTime) {
            bTime.disabled = false;
            bTime.onclick = useTimeLifeline;
        }
    }

    function use5050Lifeline() {
        if (state.gameplay.used5050 || state.gameplay.isAnswered) return;
        state.gameplay.used5050 = true;
        sound.click();

        const q = state.gameplay.questions[state.gameplay.currentIndex];
        const correct = q.correctIndex;
        const wrongs = [0, 1, 2, 3].filter(i => i !== correct);

        // Pick two wrong options to disable
        wrongs.sort(() => Math.random() - 0.5);
        const toDisable = wrongs.slice(0, 2);

        const btns = document.querySelectorAll('.option-btn');
        toDisable.forEach(idx => {
            if (btns[idx]) btns[idx].classList.add('disabled-5050');
        });

        const b5050 = document.getElementById('btnLifeline5050');
        if (b5050) b5050.disabled = true;
    }

    function useHintLifeline() {
        if (state.gameplay.usedHint || state.gameplay.isAnswered) return;
        state.gameplay.usedHint = true;
        sound.click();

        const q = state.gameplay.questions[state.gameplay.currentIndex];
        alert(`💡 ARENA HINT:\n\n${q.hint || q.keyConcept || 'Focus on the fundamental rules of the domain.'}`);

        const bHint = document.getElementById('btnLifelineHint');
        if (bHint) bHint.disabled = true;
    }

    function useTimeLifeline() {
        if (state.gameplay.usedTime || state.gameplay.isAnswered) return;
        state.gameplay.usedTime = true;
        sound.click();

        state.gameplay.timeRemaining += 15;
        const timerEl = document.getElementById('gameplayTimerCircle');
        if (timerEl) timerEl.textContent = state.gameplay.timeRemaining;

        const bTime = document.getElementById('btnLifelineTime');
        if (bTime) bTime.disabled = true;
    }

    // =========================================================================
    // 9. RESULTS & VICTORY EVALUATION
    // =========================================================================
    async function finishLevelQuiz() {
        clearInterval(state.gameplay.timerInterval);

        const total = state.gameplay.questions.length;
        const correct = state.gameplay.correctCount;
        const accuracy = Math.round((correct / total) * 1000) / 10.0;
        const passed = (accuracy >= 60.0);

        let totalTimeTaken = 0;
        state.gameplay.answers.forEach(a => totalTimeTaken += a.timeSpent);

        // Submit to backend
        const payload = {
            userId: state.userId,
            domainId: state.gameplay.domainId,
            levelId: state.gameplay.levelId,
            score: state.gameplay.score,
            totalQuestions: total,
            correctAnswers: correct,
            timeTakenSeconds: totalTimeTaken,
            streakMax: state.gameplay.maxStreak,
            userAnswers: state.gameplay.answers.map(a => a.selectedIndex)
        };

        let resultData = null;
        try {
            const res = await fetch(`${API_BASE_URL}/arena/submit`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (res.ok) {
                const json = await res.json();
                if (json.success) {
                    resultData = json.data;
                }
            }
        } catch (e) {
            console.warn('[Arena] Offline submit, evaluating score locally:', e.message);
        }

        if (!resultData) {
            resultData = evaluateLocalSubmit(payload);
        }

        // Play sound & confetti
        if (passed) {
            sound.victory();
            launchVictoryConfetti();
        } else {
            sound.wrong();
        }

        // Render Results Screen
        renderResultsView(resultData, totalTimeTaken);
        switchView('results');

        // Refresh overview cache in background
        loadOverview();
    }

    function evaluateLocalSubmit(req) {
        const accuracy = Math.round((req.correctAnswers / req.totalQuestions) * 100);
        const passed = (accuracy >= 60);
        let stars = 0;
        if (accuracy >= 90) stars = 3;
        else if (accuracy >= 70) stars = 2;
        else if (accuracy >= 50) stars = 1;

        const xp = Math.round(50 * (accuracy / 100)) + (req.streakMax * 10);
        const prog = loadLocalProgress();
        const key = req.domainId + ':' + req.levelId;

        prog.levels[key] = {
            completed: passed,
            stars: Math.max(stars, (prog.levels[key] ? prog.levels[key].stars : 0)),
            highScore: Math.max(req.score, (prog.levels[key] ? prog.levels[key].highScore : 0))
        };
        prog.stats.gamesCompleted++;
        prog.stats.correct += req.correctAnswers;
        prog.stats.total += req.totalQuestions;

        saveLocalProgress(prog);

        // Update active player XP immediately
        state.stats.totalXp = (state.stats.totalXp || 0) + xp;
        state.user.xpPoints = state.stats.totalXp;
        try {
            const rawUser = localStorage.getItem('nh_user');
            const parsed = rawUser ? JSON.parse(rawUser) : {};
            parsed.xpPoints = state.stats.totalXp;
            localStorage.setItem('nh_user', JSON.stringify(parsed));
        } catch (e) {}
        updateStatsUI();

        return {
            passed: passed,
            starsEarned: stars,
            xpAwarded: xp,
            accuracy: accuracy,
            nextLevelUnlocked: passed && req.levelId < 5,
            nextLevelId: req.levelId + 1,
            feedbackMessage: passed ? 'Level cleared! Outstanding career knowledge demonstrated.' : 'Keep practicing to master these concepts.'
        };
    }


    function renderResultsView(resultData, timeTakenSeconds) {
        setText('resultsTitle', resultData.passed ? 'Level Conquered!' : 'Level Incomplete');
        setText('resultsFeedbackMsg', resultData.feedbackMessage || '');
        setText('resultsXpEarned', `+${resultData.xpAwarded} XP`);
        setText('resultsAccuracyVal', `${resultData.accuracy}%`);
        setText('resultsTimeTaken', `${timeTakenSeconds}s`);

        const iconEl = document.getElementById('resultsVictoryIcon');
        if (iconEl) iconEl.textContent = resultData.passed ? '🏆' : '⚡';

        // Stars Row
        const starsRow = document.getElementById('resultsStarsRow');
        if (starsRow) {
            const stars = resultData.starsEarned || 0;
            starsRow.innerHTML = `
                <span class="result-star-icon ${stars >= 1 ? 'filled' : ''}">★</span>
                <span class="result-star-icon ${stars >= 2 ? 'filled' : ''}">★</span>
                <span class="result-star-icon ${stars >= 3 ? 'filled' : ''}">★</span>
            `;
        }

        // Action Buttons
        const btnNext = document.getElementById('btnResultNextLevel');
        if (btnNext) {
            if (resultData.nextLevelUnlocked) {
                btnNext.style.display = 'inline-flex';
                btnNext.onclick = function () {
                    launchGameplay(state.gameplay.domainId, resultData.nextLevelId);
                };
            } else {
                btnNext.style.display = 'none';
            }
        }

        const btnReplay = document.getElementById('btnResultReplay');
        if (btnReplay) {
            btnReplay.onclick = function () {
                launchGameplay(state.gameplay.domainId, state.gameplay.levelId);
            };
        }

        const btnBack = document.getElementById('btnResultBackDomains');
        if (btnBack) {
            btnBack.onclick = function () {
                switchView('levels', state.gameplay.domainId);
            };
        }

        // Review Accordion
        const reviewContainer = document.getElementById('resultsReviewList');
        if (reviewContainer) {
            reviewContainer.innerHTML = '';
            state.gameplay.answers.forEach((ans, idx) => {
                const item = document.createElement('div');
                item.className = 'review-item';

                const letters = ['A', 'B', 'C', 'D'];
                const userChoice = ans.selectedIndex >= 0 ? `${letters[ans.selectedIndex]}. ${ans.question.options[ans.selectedIndex]}` : 'Timed Out';
                const correctChoice = `${letters[ans.correctIndex]}. ${ans.question.options[ans.correctIndex]}`;

                item.innerHTML = `
                    <div class="review-item-header">
                        <span>${ans.isCorrect ? '✅' : '❌'} Q${idx + 1}: ${escapeHTML(ans.question.text.substring(0, 70))}...</span>
                        <span style="font-size: 0.75rem; color: ${ans.isCorrect ? '#10b981' : '#ef4444'}; font-weight: 800;">
                            ${ans.isCorrect ? 'CORRECT' : 'INCORRECT'}
                        </span>
                    </div>
                    <div class="review-item-body">
                        <div style="margin-bottom: 0.5rem;"><strong>Your Answer:</strong> <span style="color: ${ans.isCorrect ? '#10b981' : '#ef4444'}">${escapeHTML(userChoice)}</span></div>
                        <div style="margin-bottom: 0.75rem;"><strong>Correct Answer:</strong> <span style="color: #10b981;">${escapeHTML(correctChoice)}</span></div>
                        <div style="margin-top: 0.5rem; line-height: 1.5; color: var(--text-secondary);">
                            <strong>Explanation:</strong> ${escapeHTML(ans.question.explanation || '')}
                        </div>
                    </div>
                `;
                reviewContainer.appendChild(item);
            });
        }
    }

    // =========================================================================
    // 10. DAILY CHALLENGE & COUNTDOWN
    // =========================================================================
    function initDailyChallengeView() {
        startDailyCountdown();

        const btn = document.getElementById('btnStartDailyChallenge');
        if (btn) {
            btn.onclick = function () {
                // Launch mixed 5-question sprint
                launchDailySprint();
            };
        }
    }

    function startDailyCountdown() {
        function updateClock() {
            const now = new Date();
            const tomorrow = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1);
            const diff = tomorrow - now;

            const h = Math.floor((diff / (1000 * 60 * 60)) % 24);
            const m = Math.floor((diff / (1000 * 60)) % 60);
            const s = Math.floor((diff / 1000) % 60);

            setText('dailyHours', String(h).padStart(2, '0'));
            setText('dailyMinutes', String(m).padStart(2, '0'));
            setText('dailySeconds', String(s).padStart(2, '0'));
        }
        updateClock();
        setInterval(updateClock, 1000);
    }

    async function launchDailySprint() {
        sound.click();
        try {
            const res = await fetch(`${API_BASE_URL}/arena/daily-challenge?userId=${state.userId}`);
            if (res.ok) {
                const json = await res.json();
                if (json.success && json.data && json.data.questions) {
                    state.gameplay.domainId = 'daily-cross-domain';
                    state.gameplay.levelId = 1;
                    state.gameplay.questions = json.data.questions;
                    state.gameplay.currentIndex = 0;
                    state.gameplay.score = 0;
                    state.gameplay.correctCount = 0;
                    state.gameplay.comboStreak = 0;
                    state.gameplay.maxStreak = 0;
                    state.gameplay.answers = [];
                    resetLifelineButtons();
                    switchView('gameplay');
                    presentCurrentQuestion();
                    return;
                }
            }
        } catch (e) {}

        // Fallback: build a real cross-domain sprint with 1 question from each domain!
        const domains = ['data-analytics', 'data-science', 'software-dev', 'cybersecurity', 'business-aptitude'];
        const sprintQuestions = [];
        domains.forEach(d => {
            const list = getLocalQuestions(d, 1);
            if (list && list.length > 0) {
                sprintQuestions.push(list[0]);
            }
        });

        state.gameplay.domainId = 'daily-cross-domain';
        state.gameplay.levelId = 1;
        state.gameplay.questions = sprintQuestions;
        state.gameplay.currentIndex = 0;
        state.gameplay.score = 0;
        state.gameplay.correctCount = 0;
        state.gameplay.comboStreak = 0;
        state.gameplay.maxStreak = 0;
        state.gameplay.answers = [];
        resetLifelineButtons();
        switchView('gameplay');
        presentCurrentQuestion();
    }


    // =========================================================================
    // 11. ACHIEVEMENTS SYSTEM
    // =========================================================================
    async function loadAchievements() {
        try {
            const res = await fetch(`${API_BASE_URL}/arena/achievements?userId=${state.userId}`);
            if (res.ok) {
                const json = await res.json();
                if (json.success && json.data) {
                    renderAchievements(json.data);
                    return;
                }
            }
        } catch (e) {}
        loadLocalAchievements();
    }

    function renderAchievements(achs) {
        const container = document.getElementById('achievementsGrid');
        if (!container) return;
        container.innerHTML = '';

        let unlockedCount = 0;
        const iconMap = {
            crosshair: '🎯',
            'bar-chart-2': '📊',
            cpu: '🧠',
            'code-2': '💻',
            'shield-check': '🛡️',
            briefcase: '💼',
            star: '⭐',
            zap: '⚡',
            award: '👑',
            trophy: '🏆'
        };

        achs.forEach(ach => {
            if (ach.unlocked) unlockedCount++;
            const card = document.createElement('div');
            card.className = `achievement-card ${ach.unlocked ? 'unlocked' : ''}`;
            const icon = iconMap[ach.icon] || '🏆';

            card.innerHTML = `
                <div class="achievement-icon-wrap">${icon}</div>
                <div style="flex: 1;">
                    <h4 class="achievement-title">${escapeHTML(ach.title)}</h4>
                    <p class="achievement-desc">${escapeHTML(ach.description)}</p>
                    <div style="display: flex; align-items: center; justify-content: space-between;">
                        <span class="achievement-reward">+${ach.xpBonus} XP Bonus</span>
                        <span style="font-size: 0.72rem; font-weight: 800; color: ${ach.unlocked ? '#10b981' : 'var(--text-muted)'};">
                            ${ach.unlocked ? '✓ UNLOCKED' : 'LOCKED'}
                        </span>
                    </div>
                </div>
            `;
            container.appendChild(card);
        });

        setText('sidebarAchCount', `${unlockedCount}/${achs.length}`);
    }

    // =========================================================================
    // 12. LEADERBOARD SYSTEM
    // =========================================================================
    async function loadLeaderboard() {
        try {
            const res = await fetch(`${API_BASE_URL}/arena/leaderboard?userId=${state.userId}`);
            if (res.ok) {
                const json = await res.json();
                if (json.success && json.data) {
                    renderLeaderboard(json.data);
                    return;
                }
            }
        } catch (e) {}
        loadLocalLeaderboard();
    }

    function renderLeaderboard(entries) {
        // Render Podium (Ranks 2, 1, 3)
        const podiumContainer = document.getElementById('leaderboardPodium');
        if (podiumContainer && entries.length >= 3) {
            const r1 = entries.find(e => e.rank === 1) || entries[0];
            const r2 = entries.find(e => e.rank === 2) || entries[1];
            const r3 = entries.find(e => e.rank === 3) || entries[2];

            podiumContainer.innerHTML = `
                <!-- Rank 2 (Left) -->
                <div class="podium-pillar podium-rank-2">
                    <div class="podium-crown">🥈</div>
                    <div class="podium-avatar">${r2.avatar || '👨‍💻'}</div>
                    <div class="podium-name">${escapeHTML(r2.name)}</div>
                    <div style="font-size: 0.75rem; color: var(--text-muted); margin-bottom: 0.5rem;">${escapeHTML(r2.domainTitle)}</div>
                    <div class="podium-score">${r2.xp} XP</div>
                </div>

                <!-- Rank 1 (Center) -->
                <div class="podium-pillar podium-rank-1">
                    <div class="podium-crown">👑</div>
                    <div class="podium-avatar">${r1.avatar || '👩‍💻'}</div>
                    <div class="podium-name">${escapeHTML(r1.name)}</div>
                    <div style="font-size: 0.75rem; color: #fbbf24; margin-bottom: 0.5rem; font-weight: 700;">${escapeHTML(r1.domainTitle)}</div>
                    <div class="podium-score" style="color: #fbbf24; font-size: 1.1rem;">${r1.xp} XP</div>
                </div>

                <!-- Rank 3 (Right) -->
                <div class="podium-pillar podium-rank-3">
                    <div class="podium-crown">🥉</div>
                    <div class="podium-avatar">${r3.avatar || '🛡️'}</div>
                    <div class="podium-name">${escapeHTML(r3.name)}</div>
                    <div style="font-size: 0.75rem; color: var(--text-muted); margin-bottom: 0.5rem;">${escapeHTML(r3.domainTitle)}</div>
                    <div class="podium-score">${r3.xp} XP</div>
                </div>
            `;
        }

        // Render Table
        const tbody = document.getElementById('leaderboardTableBody');
        if (tbody) {
            tbody.innerHTML = '';
            entries.forEach(item => {
                const tr = document.createElement('tr');
                if (item.isCurrentUser) tr.className = 'lb-row-current-user';

                tr.innerHTML = `
                    <td style="font-weight: 800; font-family: 'JetBrains Mono', monospace;">#${item.rank}</td>
                    <td>
                        <div style="display: flex; align-items: center; gap: 0.75rem;">
                            <span style="font-size: 1.25rem;">${item.avatar || '🎮'}</span>
                            <span style="font-weight: 700;">${escapeHTML(item.name)}</span>
                        </div>
                    </td>
                    <td style="color: var(--text-secondary);">${escapeHTML(item.domainTitle)}</td>
                    <td style="color: #f97316; font-weight: 700;">🔥 ${item.streak}d</td>
                    <td style="color: #fbbf24; font-weight: 700;">⭐ ${item.stars}</td>
                    <td style="font-weight: 800; color: var(--accent-cyan); font-family: 'JetBrains Mono', monospace;">${item.xp} XP</td>
                `;
                tbody.appendChild(tr);
            });
        }
    }

    // =========================================================================
    // 13. LOCAL FALLBACKS & SEEDS
    // =========================================================================
    function loadLocalFallbackOverview() {
        state.domains = [
            {
                id: 'data-analytics',
                title: 'Data Analytics',
                tagline: 'Master SQL, Data Visualization & KPI Insights',
                description: 'Excel modeling, SQL window queries, Power BI DAX, and executive reporting.',
                accentColor: '#00e5ff',
                accentGradient: 'linear-gradient(135deg, rgba(0, 229, 255, 0.2) 0%, rgba(14, 165, 233, 0.05) 100%)',
                glowColor: 'rgba(0, 229, 255, 0.35)',
                topics: ['SQL Queries', 'Excel & Spreadsheets', 'Statistics', 'Power BI & DAX'],
                totalLevels: 5,
                completedLevels: 0,
                totalStars: 15,
                earnedStars: 0
            },
            {
                id: 'data-science',
                title: 'Data Science',
                tagline: 'Predictive Modeling, ML & AI Architectures',
                description: 'Python data wrangling, feature engineering, classification, neural nets, and LLM guardrails.',
                accentColor: '#a855f7',
                accentGradient: 'linear-gradient(135deg, rgba(168, 85, 247, 0.2) 0%, rgba(139, 92, 246, 0.05) 100%)',
                glowColor: 'rgba(168, 85, 247, 0.35)',
                topics: ['Python & Pandas', 'Machine Learning', 'Deep Learning', 'LLM Guardrails'],
                totalLevels: 5,
                completedLevels: 0,
                totalStars: 15,
                earnedStars: 0
            },
            {
                id: 'software-dev',
                title: 'Software Development',
                tagline: 'Algorithms, OOP Design & Scalable Systems',
                description: 'Data structures, Java/Python internals, complexity tuning, concurrency, and distributed systems.',
                accentColor: '#10b981',
                accentGradient: 'linear-gradient(135deg, rgba(16, 185, 129, 0.2) 0%, rgba(5, 150, 105, 0.05) 100%)',
                glowColor: 'rgba(16, 185, 129, 0.35)',
                topics: ['OOP Principles', 'Data Structures', 'Concurrency', 'System Design'],
                totalLevels: 5,
                completedLevels: 0,
                totalStars: 15,
                earnedStars: 0
            },
            {
                id: 'cybersecurity',
                title: 'Cybersecurity & Networks',
                tagline: 'Defensive Security, Protocols & Threat Analysis',
                description: 'TCP/IP stack, firewalls, cryptographic hashing, vulnerability detection (SQLi/XSS), and Zero Trust.',
                accentColor: '#f97316',
                accentGradient: 'linear-gradient(135deg, rgba(249, 115, 22, 0.2) 0%, rgba(234, 88, 12, 0.05) 100%)',
                glowColor: 'rgba(249, 115, 22, 0.35)',
                topics: ['Networking & OSI', 'Authentication', 'Threat Defense', 'Incident Response'],
                totalLevels: 5,
                completedLevels: 0,
                totalStars: 15,
                earnedStars: 0
            },
            {
                id: 'business-aptitude',
                title: 'Business & Aptitude',
                tagline: 'Quantitative Reasoning, Logic & Case Breakdown',
                description: 'Numerical problem solving, logical deduction, data interpretation, and unit economics.',
                accentColor: '#f59e0b',
                accentGradient: 'linear-gradient(135deg, rgba(245, 158, 11, 0.2) 0%, rgba(217, 119, 6, 0.05) 100%)',
                glowColor: 'rgba(245, 158, 11, 0.35)',
                topics: ['Quantitative Aptitude', 'Logical Reasoning', 'Unit Economics', 'Case Studies'],
                totalLevels: 5,
                completedLevels: 0,
                totalStars: 15,
                earnedStars: 0
            }
        ];

        const prog = loadLocalProgress();
        let totalStarsCollected = 0;
        state.domains.forEach(d => {
            let comp = 0;
            let stars = 0;
            for (let i = 1; i <= 5; i++) {
                const lp = prog.levels[d.id + ':' + i];
                if (lp && lp.completed) {
                    comp++;
                    stars += lp.stars;
                }
            }
            d.completedLevels = comp;
            d.earnedStars = stars;
            totalStarsCollected += stars;
        });

        state.stats.totalStars = totalStarsCollected;
        state.stats.gamesCompleted = (prog.stats && prog.stats.gamesCompleted) || 0;
        state.stats.overallAccuracy = (prog.stats && prog.stats.total > 0)
            ? Math.round((prog.stats.correct / prog.stats.total) * 100)
            : 0;
        state.stats.totalXp = state.user.xpPoints || 250;

        updateStatsUI();
        renderDomainsOverview(state.domains);

    }

    function loadLocalLevels(domainId) {
        const prog = loadLocalProgress();
        const titles = ["Foundations & Syntax", "Core Problem Solving", "Advanced Architecture", "Mastery & Optimization", "Boss Battle: Arena Champion"];
        const subtitles = ["Master basic terms, syntax, and foundational principles", "Tackle real-world scenarios, algorithms, and practical queries", "Deep-dive into performance tuning and system patterns", "Edge-cases, complex tradeoffs, and rigorous analysis", "High-stakes timed battle against elite professional scenarios"];
        const diffs = ["EASY", "MEDIUM", "HARD", "EXPERT", "BOSS"];
        const xps = [50, 80, 120, 160, 250];

        state.currentLevels = [];
        for (let i = 1; i <= 5; i++) {
            const unlocked = (i === 1) || (prog.levels[domainId + ':' + (i - 1)] && prog.levels[domainId + ':' + (i - 1)].completed);
            const lp = prog.levels[domainId + ':' + i] || { stars: 0, highScore: 0 };
            state.currentLevels.push({
                levelId: i,
                domainId: domainId,
                title: titles[i - 1],
                subtitle: subtitles[i - 1],
                badge: ["Rookie", "Specialist", "Architect", "Elite", "Grandmaster"][i - 1],
                questionsCount: 5,
                xpReward: xps[i - 1],
                difficulty: diffs[i - 1],
                unlocked: !!unlocked,
                starsEarned: lp.stars,
                highScore: lp.highScore
            });
        }
        renderLevelsMap(domainId, state.currentLevels);
    }

    function getLocalQuestions(domainId, levelId) {
        if (domainId === 'data-science') {
            return [
                {
                    text: "In classification problems with severe class imbalance (e.g. 99% negative, 1% positive), which metric is LEAST informative?",
                    codeSnippet: null,
                    options: ["Precision", "Recall", "Accuracy", "F1-Score"],
                    correctIndex: 2,
                    explanation: "Accuracy is deceptive under severe imbalance: a naive model predicting 100% negative achieves 99% accuracy while detecting zero positives.",
                    keyConcept: "Raw accuracy hides minority class detection failures.",
                    hint: "Think about a model that always predicts negative regardless of input.",
                    topic: "Model Evaluation"
                },
                {
                    text: "Which regularization technique can drive model feature coefficients strictly to zero for automatic feature selection?",
                    codeSnippet: "Loss = MSE + lambda * ||w||",
                    options: ["L2 Regularization (Ridge)", "L1 Regularization (Lasso)", "Dropout", "Batch Normalization"],
                    correctIndex: 1,
                    explanation: "L1 Lasso applies an absolute value penalty, producing sharp corners on the constraint contour that force weights strictly to zero.",
                    keyConcept: "L1 regularization performs sparse weight selection.",
                    hint: "L1 vs L2: one uses absolute sum penalty, the other uses squared sum penalty.",
                    topic: "Regularization"
                },
                {
                    text: "What is the primary function of the Self-Attention mechanism in modern Transformer architectures?",
                    codeSnippet: null,
                    options: [
                        "Compress token strings into integer hashes",
                        "Compute contextual relevance scores between all token pairs regardless of sequence distance",
                        "Remove punctuation from token representations",
                        "Enforce chronological Left-to-Right token recurrence"
                    ],
                    correctIndex: 1,
                    explanation: "Self-attention dynamically scores relationships between all tokens in parallel across arbitrary sequence distances.",
                    keyConcept: "Transformers use pairwise attention to capture context without recurrent bottlenecks.",
                    hint: "Think about pairwise token-to-token relationship scoring.",
                    topic: "Deep Learning & Transformers"
                },
                {
                    text: "What happens when learning rate is set excessively high during gradient descent?",
                    codeSnippet: null,
                    options: [
                        "Loss plateaus immediately without changing",
                        "Loss oscillates and diverges away from the local minimum",
                        "Gradient norm drops instantly to zero",
                        "Model weights freeze permanently"
                    ],
                    correctIndex: 1,
                    explanation: "An excessively large learning rate causes steps to overshoot the gradient valley, causing catastrophic divergence.",
                    keyConcept: "High learning rates cause gradient oscillation and divergence.",
                    hint: "Think of a ball bouncing violently over the valley walls.",
                    topic: "Optimization"
                },
                {
                    text: "In Retrieval-Augmented Generation (RAG), what is the role of Vector Embeddings?",
                    codeSnippet: null,
                    options: [
                        "Encrypt confidential corporate documents",
                        "Map text semantics into high-dimensional vector space for cosine similarity search",
                        "Translate text into SQL queries automatically",
                        "Compress audio signals into text chunks"
                    ],
                    correctIndex: 1,
                    explanation: "Vector embeddings convert document chunks into coordinate vectors where geometric proximity signifies semantic similarity.",
                    keyConcept: "Embeddings enable semantic nearest-neighbor search.",
                    hint: "Think of geometry in semantic concept space.",
                    topic: "LLMs & Vector Search"
                }
            ];
        } else if (domainId === 'software-dev') {
            return [
                {
                    text: "What is the average time complexity of lookups in a well-balanced HashMap / HashTable?",
                    codeSnippet: null,
                    options: ["O(1)", "O(log n)", "O(n)", "O(n log n)"],
                    correctIndex: 0,
                    explanation: "Hash table bucket computation via key hash code provides constant O(1) average lookup and insertion time.",
                    keyConcept: "Hash functions map keys directly to array buckets in constant time.",
                    hint: "Think of how array direct indexing works.",
                    topic: "Data Structures"
                },
                {
                    text: "Which SOLID principle states that software entities should be open for extension, but closed for modification?",
                    codeSnippet: null,
                    options: ["Single Responsibility Principle", "Open/Closed Principle", "Liskov Substitution Principle", "Interface Segregation"],
                    correctIndex: 1,
                    explanation: "The Open/Closed Principle (OCP) ensures existing validated classes remain untouched while extending behavior via polymorphism.",
                    keyConcept: "OCP: Extend functionality through interfaces rather than altering existing code.",
                    hint: "Look for the letters O and C.",
                    topic: "OOP Principles"
                },
                {
                    text: "What condition causes a Deadlock in multithreaded concurrent systems?",
                    codeSnippet: null,
                    options: [
                        "A thread is garbage collected while running",
                        "Two or more threads each hold a lock while waiting for a lock held by the other",
                        "A thread executes an infinite loop",
                        "Thread priority is set to maximum"
                    ],
                    correctIndex: 1,
                    explanation: "Deadlock occurs when mutual exclusion, hold-and-wait, no preemption, and circular lock waiting dependencies coincide.",
                    keyConcept: "Circular dependency on exclusive locks causes permanent stall.",
                    hint: "Think of two cars blocking each other at a single-lane bridge.",
                    topic: "Concurrency"
                },
                {
                    text: "What does the CAP theorem state regarding distributed data stores?",
                    codeSnippet: null,
                    options: [
                        "A system can guarantee all three: Consistency, Availability, and Partition Tolerance",
                        "Under network partitions, a distributed system must choose between Consistency or Availability",
                        "Cache Always Precedes Persistence",
                        "Concurrency And Parallelism are mathematically identical"
                    ],
                    correctIndex: 1,
                    explanation: "The CAP theorem proves that in the presence of network partitions (P), a distributed system must trade off between Consistency (C) and Availability (A).",
                    keyConcept: "Under network splits, choose between data consistency or continuous availability.",
                    hint: "Network partitions are unavoidable across distributed nodes.",
                    topic: "Distributed Systems"
                },
                {
                    text: "Which HTTP status code should a REST API return when a client is authenticated but lacks required permissions?",
                    codeSnippet: null,
                    options: ["401 Unauthorized", "403 Forbidden", "404 Not Found", "400 Bad Request"],
                    correctIndex: 1,
                    explanation: "401 means unauthenticated (identity missing). 403 means authenticated but forbidden (insufficient role privileges).",
                    keyConcept: "401 = Missing identity; 403 = Missing permission.",
                    hint: "The caller is logged in, but not authorized to touch this specific resource.",
                    topic: "REST API Design"
                }
            ];
        } else if (domainId === 'cybersecurity') {
            return [
                {
                    text: "At which OSI layer does the TCP protocol operate?",
                    codeSnippet: null,
                    options: ["Layer 2 (Data Link)", "Layer 3 (Network)", "Layer 4 (Transport)", "Layer 7 (Application)"],
                    correctIndex: 2,
                    explanation: "TCP provides reliable, ordered end-to-end transport and flow control at Layer 4 (Transport Layer).",
                    keyConcept: "TCP/UDP belong to OSI Layer 4.",
                    hint: "IP operates at Layer 3; TCP operates directly above it.",
                    topic: "Networking & OSI"
                },
                {
                    text: "What is the primary defense against SQL Injection (SQLi) vulnerabilities in web applications?",
                    codeSnippet: "String query = \"SELECT * FROM users WHERE user='\" + input + \"'\";",
                    options: [
                        "Storing passwords in client cookies",
                        "Parameterized Prepared Statements with typed bindings",
                        "Increasing database connection timeout to 60 seconds",
                        "Using base64 encoding on form inputs"
                    ],
                    correctIndex: 1,
                    explanation: "Prepared Statements pre-compile the SQL statement structure, ensuring user inputs are treated strictly as data literals.",
                    keyConcept: "Pre-compilation and parameterized binding eliminate SQL code injection.",
                    hint: "The database engine must separate query syntax from user inputs.",
                    topic: "Application Security"
                },
                {
                    text: "Why is password salting essential prior to cryptographic hashing (e.g. bcrypt / argon2)?",
                    codeSnippet: null,
                    options: [
                        "It speeds up the hashing algorithm",
                        "It prevents rainbow table attacks and ensures identical passwords yield unique hashes",
                        "It encrypts the password with the user's private key",
                        "It allows administrators to reverse the hash"
                    ],
                    correctIndex: 1,
                    explanation: "A unique random salt ensures identical passwords produce distinct hash digests, neutralizing precomputed rainbow tables.",
                    keyConcept: "Salts ensure unique hashes even for identical passwords.",
                    hint: "Think about how attackers precalculate hashes for top passwords.",
                    topic: "Cryptography"
                },
                {
                    text: "What core tenet defines the Zero Trust security architecture?",
                    codeSnippet: null,
                    options: [
                        "Trust all devices located inside the corporate intranet",
                        "'Never trust, always verify' — authenticate and authorize every request regardless of origin",
                        "Disable encryption on internal networks for speed",
                        "Use a single master password across all cloud nodes"
                    ],
                    correctIndex: 1,
                    explanation: "Zero Trust assumes threats exist both inside and outside the perimeter; every access attempt is strictly authenticated and authorized.",
                    keyConcept: "Zero Trust eliminates implicit trust based on network perimeter.",
                    hint: "Never trust, always verify.",
                    topic: "Security Architecture"
                },
                {
                    text: "What is the primary difference between Symmetric and Asymmetric encryption?",
                    codeSnippet: null,
                    options: [
                        "Symmetric is for text; Asymmetric is for binary files",
                        "Symmetric uses one shared secret key; Asymmetric uses a mathematically linked public/private key pair",
                        "Symmetric cannot be decrypted",
                        "Asymmetric requires quantum computing hardware"
                    ],
                    correctIndex: 1,
                    explanation: "Symmetric (e.g. AES) uses a single secret key. Asymmetric (e.g. RSA, ECC) uses a public key to encrypt and a private key to decrypt.",
                    keyConcept: "Symmetric: 1 shared key; Asymmetric: public/private key pair.",
                    hint: "Think of how TLS/HTTPS uses public keys to negotiate a symmetric session key.",
                    topic: "Cryptography"
                }
            ];
        } else if (domainId === 'business-aptitude') {
            return [
                {
                    text: "If a retailer buys an item for $80 and marks it up to $120, what is the gross profit margin percentage?",
                    codeSnippet: "Cost = $80, Selling Price = $120",
                    options: ["50%", "33.3%", "40%", "25%"],
                    correctIndex: 1,
                    explanation: "Profit = $120 - $80 = $40. Margin = Profit / Selling Price = 40 / 120 = 33.3%. (Markup on cost would be 40 / 80 = 50%).",
                    keyConcept: "Margin is Profit / Revenue; Markup is Profit / Cost.",
                    hint: "Margin divides profit by selling price, not by cost.",
                    topic: "Profit & Margins"
                },
                {
                    text: "A company has a Customer Acquisition Cost (CAC) of $200 and an average Customer Lifetime Value (LTV) of $700. What is the LTV:CAC ratio and health?",
                    codeSnippet: null,
                    options: [
                        "0.28:1 (Unsustainable)",
                        "3.5:1 (Strong and healthy unit economics)",
                        "1:1 (Break-even)",
                        "7:1 (Under-invested in growth)"
                    ],
                    correctIndex: 1,
                    explanation: "LTV / CAC = 700 / 200 = 3.5:1. An LTV:CAC ratio around 3:1 is widely considered the sweet spot for sustainable growth.",
                    keyConcept: "A healthy SaaS benchmark is LTV:CAC of 3:1 or higher.",
                    hint: "Divide 700 by 200.",
                    topic: "Unit Economics"
                },
                {
                    text: "If all Bloops are Razzies, and all Razzies are Lurgs, which deduction is logically certain?",
                    codeSnippet: null,
                    options: ["All Lurgs are Bloops", "All Bloops are Lurgs", "No Lurgs are Razzies", "Some Bloops are not Razzies"],
                    correctIndex: 1,
                    explanation: "Transitive inclusion: Since Bloops is a subset of Razzies and Razzies is a subset of Lurgs, all Bloops must be Lurgs.",
                    keyConcept: "Transitive categorical syllogisms preserve nested subset relationships.",
                    hint: "Think of nested circles: Bloops inside Razzies inside Lurgs.",
                    topic: "Logical Reasoning"
                },
                {
                    text: "If an initial investment of $10,000 grows at 10% compound annual interest, what is its value after 2 years?",
                    codeSnippet: "Year 1: 10,000 + 10% = 11,000. Year 2: 11,000 + 10%",
                    options: ["$12,000", "$12,100", "$12,500", "$11,900"],
                    correctIndex: 1,
                    explanation: "Year 1 = $10,000 * 1.10 = $11,000. Year 2 = $11,000 * 1.10 = $12,100. (Simple interest would be $12,000).",
                    keyConcept: "Compound interest generates return on previous interest.",
                    hint: "Remember the extra $100 earned on Year 1's interest.",
                    topic: "Quantitative Aptitude"
                },
                {
                    text: "What does a company's Break-Even Point represent in business financial analysis?",
                    codeSnippet: null,
                    options: [
                        "The volume where total revenue equals total costs (zero net profit/loss)",
                        "The maximum debt a company can borrow",
                        "The point where all employees receive bonuses",
                        "The price where competitor sales drop to zero"
                    ],
                    correctIndex: 0,
                    explanation: "Break-even is the sales volume where total revenue exactly covers fixed and variable operating expenses.",
                    keyConcept: "Break-even: Total Revenue = Fixed Costs + Variable Costs.",
                    hint: "No net profit, but zero net loss.",
                    topic: "Financial Analysis"
                }
            ];
        }

        // Default: Data Analytics
        return [
            {
                text: "Which SQL clause is used to filter records before any GROUP BY aggregation takes place?",
                codeSnippet: "SELECT department, AVG(salary)\nFROM employees\n??? salary > 50000\nGROUP BY department;",
                options: ["HAVING", "WHERE", "FILTER", "ORDER BY"],
                correctIndex: 1,
                explanation: "WHERE filters raw individual rows before grouping occurs. HAVING evaluates aggregate values generated after GROUP BY.",
                keyConcept: "WHERE filters base rows; HAVING filters aggregates.",
                hint: "Think about which clause precedes GROUP BY in standard SQL syntax.",
                topic: "SQL Fundamentals"
            },
            {
                text: "What is the key advantage of INDEX-MATCH over classic VLOOKUP in spreadsheet data modeling?",
                codeSnippet: null,
                options: [
                    "INDEX-MATCH only works on text columns",
                    "INDEX-MATCH can look to the left and does not break when columns are inserted",
                    "VLOOKUP is faster on 1-million row sheets",
                    "INDEX-MATCH requires an external VBA macro"
                ],
                correctIndex: 1,
                explanation: "INDEX-MATCH separates lookup array from return array, enabling leftward lookups and dynamic column references.",
                keyConcept: "Separation of lookup vector from return array ensures structural flexibility.",
                hint: "VLOOKUP requires the lookup key to be in the first column.",
                topic: "Excel Lookups"
            },
            {
                text: "Which statistic is most robust (least affected) when a dataset contains extreme outliers?",
                codeSnippet: "Dataset: [28k, 31k, 32k, 35k, 42k, 950k]",
                options: ["Mean", "Median", "Standard Deviation", "Variance"],
                correctIndex: 1,
                explanation: "The median is the physical middle value and remains stable regardless of how extreme maximum or minimum outliers are.",
                keyConcept: "Median resists skewed distributions and extreme values.",
                hint: "Consider which metric is based solely on positional rank rather than mathematical sum.",
                topic: "Descriptive Statistics"
            },
            {
                text: "What type of JOIN in SQL returns all records from the left table and matched records from the right table?",
                codeSnippet: "SELECT c.name, o.order_id\nFROM Customers c\n??? Orders o ON c.id = o.customer_id;",
                options: ["INNER JOIN", "FULL OUTER JOIN", "LEFT JOIN", "CROSS JOIN"],
                correctIndex: 2,
                explanation: "LEFT JOIN preserves all rows from the primary left table, populating right table attributes with NULL if no match exists.",
                keyConcept: "LEFT JOIN preserves all primary table observations.",
                hint: "The primary table sits on the left side of the operator.",
                topic: "SQL Joins"
            },
            {
                text: "Why are horizontal bar charts generally preferred over pie charts for comparing 6 or more categories?",
                codeSnippet: null,
                options: [
                    "Pie charts use too much GPU rendering memory",
                    "Human eyes evaluate linear length far more accurately than 2D slice angles",
                    "Bar charts cannot display percentage values",
                    "Pie charts require sorted alphabetic dimensions"
                ],
                correctIndex: 1,
                explanation: "Human visual perception evaluates 1D linear distances and heights accurately, whereas comparing subtle slice angles in pie charts introduces perceptual error.",
                keyConcept: "Linear length perception beats 2D angular estimation.",
                hint: "Think about how the human brain processes slice angles versus bar lengths.",
                topic: "Data Visualization"
            }
        ];
    }

    function loadLocalAchievements() {
        const prog = loadLocalProgress();
        const xp = state.stats.totalXp || state.user.xpPoints || 250;
        const games = (prog.stats && prog.stats.gamesCompleted) || 0;

        const isCleared = (dom, lvl) => {
            const r = prog.levels && prog.levels[dom + ':' + lvl];
            return !!(r && r.completed);
        };

        const has3Stars = Object.values(prog.levels || {}).some(l => l.stars === 3);
        const hasBoss = Object.entries(prog.levels || {}).some(([k, l]) => k.endsWith(':5') && l.completed);

        const achs = [
            { id: "first_game", title: "First Blood", description: "Complete your very first game in NextHire Arena", icon: "crosshair", xpBonus: 50, unlocked: games > 0 },
            { id: "da_explorer", title: "Data Scout", description: "Clear Level 1 of Data Analytics with 60%+ accuracy", icon: "bar-chart-2", xpBonus: 75, unlocked: isCleared('data-analytics', 1) },
            { id: "ds_explorer", title: "Neural Initiate", description: "Clear Level 1 of Data Science with 60%+ accuracy", icon: "cpu", xpBonus: 75, unlocked: isCleared('data-science', 1) },
            { id: "swe_explorer", title: "Syntax Knight", description: "Clear Level 1 of Software Development with 60%+ accuracy", icon: "code-2", xpBonus: 75, unlocked: isCleared('software-dev', 1) },
            { id: "cyber_explorer", title: "Firewall Guardian", description: "Clear Level 1 of Cybersecurity with 60%+ accuracy", icon: "shield-check", xpBonus: 75, unlocked: isCleared('cybersecurity', 1) },
            { id: "biz_explorer", title: "Strategy Tactician", description: "Clear Level 1 of Business & Aptitude with 60%+ accuracy", icon: "briefcase", xpBonus: 75, unlocked: isCleared('business-aptitude', 1) },
            { id: "perfect_accuracy", title: "Flawless Victory", description: "Achieve 100% accuracy (3 Stars) in any arena level", icon: "star", xpBonus: 100, unlocked: has3Stars },
            { id: "streak_master", title: "Combo Legend", description: "Complete 3 or more challenge sessions", icon: "zap", xpBonus: 80, unlocked: games >= 3 },
            { id: "boss_slayer", title: "Titan Conqueror", description: "Defeat any Level 5 Boss challenge in Game Arena", icon: "award", xpBonus: 200, unlocked: hasBoss },
            { id: "xp_centurion", title: "Arena Centurion", description: "Accumulate over 500 XP in your career profile", icon: "trophy", xpBonus: 150, unlocked: xp >= 500 }
        ];
        renderAchievements(achs);
    }


    function loadLocalLeaderboard() {
        const lb = [
            { rank: 1, name: 'Sarah Lin', avatar: '👩‍💻', xp: 1840, streak: 14, domainTitle: 'Lead AI Engineer', stars: 42, isCurrentUser: false },
            { rank: 2, name: 'Vikram Malhotra', avatar: '👨‍💻', xp: 1520, streak: 12, domainTitle: 'Senior Full-Stack', stars: 38, isCurrentUser: false },
            { rank: 3, name: 'Elena Rostova', avatar: '🛡️', xp: 1310, streak: 9, domainTitle: 'Cybersecurity Lead', stars: 33, isCurrentUser: false },
            { rank: 4, name: 'Marcus Vance', avatar: '📊', xp: 1150, streak: 8, domainTitle: 'Staff Data Architect', stars: 29, isCurrentUser: false },
            { rank: 5, name: state.user.firstName + ' (You)', avatar: '🎮', xp: state.stats.totalXp, streak: state.user.streakDays, domainTitle: 'NextHire Challenger', stars: 12, isCurrentUser: true }
        ];
        renderLeaderboard(lb);
    }

    // =========================================================================
    // 14. PROFILE & SETTINGS MODAL
    // =========================================================================
    function initModals() {
        const btnOpen = document.getElementById('btnOpenProfileModal');
        const modal = document.getElementById('modalProfileOverlay');
        const btnClose = document.getElementById('btnCloseProfileModal');
        const btnSave = document.getElementById('btnSaveProfile');
        const btnReset = document.getElementById('btnResetArenaProgress');

        if (btnOpen && modal) {
            btnOpen.addEventListener('click', () => {
                sound.click();
                setText('modalUserName', state.user.firstName + (state.user.lastName ? ' ' + state.user.lastName : ''));
                modal.classList.remove('hidden');
            });
        }

        if (btnClose && modal) {
            btnClose.addEventListener('click', () => {
                modal.classList.add('hidden');
            });
        }

        if (btnSave && modal) {
            btnSave.addEventListener('click', () => {
                modal.classList.add('hidden');
            });
        }

        if (modal) {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) modal.classList.add('hidden');
            });
        }

        if (btnReset) {
            btnReset.addEventListener('click', async () => {
                if (confirm('Are you sure you want to reset your local Game Arena level progress for testing?')) {
                    localStorage.removeItem('nh_arena_progress_' + state.userId);
                    try {
                        await fetch(`${API_BASE_URL}/arena/reset?userId=${state.userId}`, { method: 'POST' });
                    } catch (e) {}
                    alert('Progress reset successfully.');
                    window.location.reload();
                }
            });
        }

        // Sound Toggle Checkbox & Topbar Button
        const chkSound = document.getElementById('chkModalSound');
        const btnAudio = document.getElementById('btnToggleAudio');

        function toggleSound(enabled) {
            state.soundEnabled = enabled;
            localStorage.setItem('nh_arena_sfx', enabled);
            if (chkSound) chkSound.checked = enabled;
            setText('audioIcon', enabled ? '🔊' : '🔇');
            setText('audioText', enabled ? 'SFX' : 'Mute');
        }

        if (chkSound) {
            chkSound.checked = state.soundEnabled;
            chkSound.addEventListener('change', (e) => toggleSound(e.target.checked));
        }

        if (btnAudio) {
            btnAudio.addEventListener('click', () => {
                toggleSound(!state.soundEnabled);
            });
        }

        // High-Contrast Mode Toggle
        const chkContrast = document.getElementById('chkModalContrast');
        if (chkContrast) {
            chkContrast.checked = state.highContrast;
            chkContrast.addEventListener('change', (e) => {
                state.highContrast = e.target.checked;
                localStorage.setItem('nh_arena_contrast', e.target.checked);
                document.body.classList.toggle('high-contrast', e.target.checked);
            });
        }
        if (state.highContrast) {
            document.body.classList.add('high-contrast');
        }
    }


    // =========================================================================
    // 15. SETUP EVENT LISTENERS & INITIALIZATION
    // =========================================================================
    function setupEventListeners() {
        // Sidebar Collapsible Toggle
        const btnToggle = document.getElementById('btnToggleSidebar');
        const sidebar = document.getElementById('arenaSidebar');
        if (btnToggle && sidebar) {
            btnToggle.addEventListener('click', () => {
                sidebar.classList.toggle('collapsed');
            });
        }

        // Mobile Nav Hamburger
        const btnMobile = document.getElementById('btnMobileNavToggle');
        if (btnMobile && sidebar) {
            btnMobile.addEventListener('click', () => {
                sidebar.classList.toggle('mobile-open');
            });
        }

        // Sidebar Navigation Links
        const navLinks = document.querySelectorAll('.sidebar-nav .nav-item[data-view]');
        navLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const view = link.dataset.view;
                if (view) switchView(view);
            });
        });

        // Topbar Breadcrumbs Home Link
        const brandLogo = document.getElementById('navBrandLogo');
        if (brandLogo) {
            brandLogo.addEventListener('click', (e) => {
                e.preventDefault();
                switchView('overview');
            });
        }

        // Hero Buttons
        const btnSecondary = document.getElementById('btnHeroSecondaryCTA');
        if (btnSecondary) {
            btnSecondary.addEventListener('click', () => switchView('careers'));
        }

        // Back to Domains Button in Levels View
        const btnBackDomains = document.getElementById('btnBackToDomains');
        if (btnBackDomains) {
            btnBackDomains.addEventListener('click', () => switchView('careers'));
        }

        // URL Hash change support
        window.addEventListener('hashchange', () => {
            const hash = window.location.hash.replace('#', '');
            if (hash && ['overview', 'careers', 'progress', 'daily', 'achievements', 'leaderboard'].includes(hash)) {
                switchView(hash);
            }
        });
    }

    // Utility helpers
    function setText(id, text) {
        const el = document.getElementById(id);
        if (el) el.textContent = text;
    }

    function escapeHTML(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    // Initial Bootstrap
    document.addEventListener('DOMContentLoaded', () => {
        // Set user details in UI
        setText('sidebarUserName', state.user.firstName || 'Candidate');
        setText('modalUserName', state.user.firstName || 'Candidate');

        setupEventListeners();
        initModals();

        // Check hash or default to overview
        const initialHash = window.location.hash.replace('#', '');
        if (initialHash && ['overview', 'careers', 'progress', 'daily', 'achievements', 'leaderboard'].includes(initialHash)) {
            switchView(initialHash);
        } else {
            switchView('overview');
        }
    });

})();
