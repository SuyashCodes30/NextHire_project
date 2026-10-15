/**
 * NextHire AI Platform — Client API Configuration
 * 
 * Supports:
 * 1. Hardcoded DEPLOYED_RENDER_URL below
 * 2. Runtime URL parameter: ?apiUrl=https://your-backend.onrender.com (persisted to localStorage)
 * 3. window.NEXTHIRE_BACKEND_URL override
 * 4. Automatic local development detection (localhost / 127.0.0.1)
 * 5. Automatic Render same-origin detection
 * 6. Automatic background wake-up ping for Render Free Tier cold-start
 */
(function (root) {
    'use strict';

    // =========================================================================
    // 1. PRODUCTION RENDER BACKEND URL
    // =========================================================================
    // When you deploy your backend on Render, Render will assign a URL like:
    // https://nexthire-backend.onrender.com (or https://nexthire-backend-xxxx.onrender.com)
    // Update the URL below with your actual Render service URL!
    var DEPLOYED_RENDER_URL = 'https://nexthire-project.onrender.com';

    // =========================================================================
    // 2. RUNTIME OVERRIDE DETECTION (?apiUrl=... or ?backend=... or localStorage)
    // =========================================================================
    var runtimeOverride = '';
    try {
        if (typeof window !== 'undefined' && window.location && window.location.search) {
            var urlParams = new URLSearchParams(window.location.search);
            var queryApi = urlParams.get('apiUrl') || urlParams.get('backend') || urlParams.get('renderUrl');
            if (queryApi) {
                queryApi = queryApi.trim().replace(/\/+$/, '');
                localStorage.setItem('NEXTHIRE_API_URL', queryApi);
                runtimeOverride = queryApi;
                console.log('[NextHire] Backend URL saved from query parameter:', runtimeOverride);
            }
        }
        if (!runtimeOverride && typeof localStorage !== 'undefined') {
            runtimeOverride = (localStorage.getItem('NEXTHIRE_API_URL') || '').trim().replace(/\/+$/, '');
        }
    } catch (e) {
        // Ignore cross-origin storage restrictions
    }

    var config = {
        // Configured Render Backend Production URL
        apiBaseUrl: DEPLOYED_RENDER_URL,

        /**
         * Resolves the host origin without trailing slash or trailing /api
         */
        getBaseUrl: function () {
            var explicit = (runtimeOverride || this.apiBaseUrl || (typeof window !== 'undefined' && window.NEXTHIRE_BACKEND_URL) || '').trim().replace(/\/+$/, '');

            if (typeof window !== 'undefined') {
                var hostname = window.location.hostname;
                var port = window.location.port;

                // 1. If currently opened directly on the Render domain (same origin as backend)
                if (hostname.endsWith('.onrender.com')) {
                    return '';
                }

                // 2. If running locally on port 8080 (served by Spring Boot locally)
                if ((hostname === 'localhost' || hostname === '127.0.0.1') && port === '8080') {
                    return '';
                }

                // 3. If running on local development dev servers (Live Server 5500, Vite 5173, etc.)
                if (hostname === 'localhost' || hostname === '127.0.0.1') {
                    if (runtimeOverride) {
                        return runtimeOverride.replace(/\/api$/, '');
                    }
                    return 'http://localhost:8080';
                }

                // 4. If running on a deployed frontend (Vercel, Netlify, custom domain, etc.)
                if (explicit) {
                    return explicit.replace(/\/api$/, '');
                }
                return DEPLOYED_RENDER_URL;
            }

            return explicit ? explicit.replace(/\/api$/, '') : DEPLOYED_RENDER_URL;
        },

        /**
         * Resolves the full API endpoint base URL (ensures ending with /api)
         */
        getApiUrl: function () {
            var base = this.getBaseUrl();
            return base ? (base + '/api') : '/api';
        },

        /**
         * Utility to update the backend URL dynamically at runtime
         */
        setApiUrl: function (url) {
            if (!url) return;
            url = url.trim().replace(/\/+$/, '').replace(/\/api$/, '');
            try {
                localStorage.setItem('NEXTHIRE_API_URL', url);
            } catch (e) {}
            this.apiBaseUrl = url;
            runtimeOverride = url;
            console.log('[NextHire] Backend URL updated to:', url);
            window.location.reload();
        },

        /**
         * Check backend health status
         */
        checkHealth: function (callback) {
            var url = this.getApiUrl() + '/health';
            fetch(url, { method: 'GET', mode: 'cors' })
                .then(function (res) { return res.json(); })
                .then(function (data) {
                    if (typeof callback === 'function') callback(null, data);
                })
                .catch(function (err) {
                    if (typeof callback === 'function') callback(err);
                });
        }
    };

    // =========================================================================
    // 3. AUTOMATIC RENDER COLD-START WAKE-UP PING
    // =========================================================================
    // Render Free tier Web Services sleep after 15 minutes of inactivity.
    // Pinging /api/health as soon as the frontend loads initiates the wake-up process
    // so the backend is ready by the time the user performs an action!
    if (typeof window !== 'undefined') {
        var announce = function () {
            var base = config.getBaseUrl() || '(same-origin)';
            console.log('[NextHire] Active Backend Origin:', base, '| API Endpoint:', config.getApiUrl());

            var hostname = window.location.hostname;
            var isLocal = hostname === 'localhost' || hostname === '127.0.0.1';
            var apiUrl = config.getApiUrl();

            if (!isLocal && apiUrl && !apiUrl.startsWith('/')) {
                config.checkHealth(function (err, data) {
                    if (err) {
                        console.info('[NextHire] Render backend is waking up or initializing (Render free tier cold start takes ~30s)...');
                    } else {
                        console.log('✅ [NextHire] Render backend is UP and responsive:', data);
                    }
                });
            }
        };

        if (document.readyState === 'loading') {
            window.addEventListener('DOMContentLoaded', announce);
        } else {
            announce();
        }
    }

    root.NEXTHIRE_CONFIG = config;
})(typeof window !== 'undefined' ? window : (typeof globalThis !== 'undefined' ? globalThis : this));
