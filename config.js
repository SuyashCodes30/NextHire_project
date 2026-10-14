/**
 * NextHire AI Platform — Client API Configuration
 */
(function (root) {
    'use strict';

    var DEPLOYED_RENDER_URL = 'https://nexthire-platform.onrender.com';

    // Check for runtime query param override (?apiUrl=...) or localStorage
    var runtimeOverride = '';
    try {
        if (typeof window !== 'undefined' && window.location && window.location.search) {
            var urlParams = new URLSearchParams(window.location.search);
            var queryApi = urlParams.get('apiUrl');
            if (queryApi) {
                localStorage.setItem('NEXTHIRE_API_URL', queryApi);
                runtimeOverride = queryApi;
            }
        }
        if (!runtimeOverride && typeof localStorage !== 'undefined') {
            runtimeOverride = localStorage.getItem('NEXTHIRE_API_URL') || '';
        }
    } catch (e) {
        // Ignore cross-origin storage restrictions
    }

    var config = {
        // Render Backend Production URL
        apiBaseUrl: DEPLOYED_RENDER_URL,

        // Resolves the host origin without trailing slash or trailing /api
        getBaseUrl: function () {
            var explicit = (runtimeOverride || this.apiBaseUrl || (typeof window !== 'undefined' && window.NEXTHIRE_BACKEND_URL) || '').trim().replace(/\/+$/, '');

            if (typeof window !== 'undefined') {
                var hostname = window.location.hostname;
                var port = window.location.port;

                // 1. If currently opened directly on the Render domain (same origin as backend)
                // Use relative calls ('') so it ALWAYS works with zero CORS or Mixed-Content issues!
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

        // Resolves the full API endpoint base URL (ensures ending with /api)
        getApiUrl: function () {
            var base = this.getBaseUrl();
            return base ? (base + '/api') : '/api';
        }
    };

    root.NEXTHIRE_CONFIG = config;
})(typeof window !== 'undefined' ? window : (typeof globalThis !== 'undefined' ? globalThis : this));
