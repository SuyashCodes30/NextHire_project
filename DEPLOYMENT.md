# NextHire AI Platform — Complete Deployment Guide

This guide covers deploying the **NextHire Spring Boot Backend** on **Render** (Free Web Service) and the **Frontend** on **Vercel** completely free, and connecting them together.

---

## Architecture Overview

```
                      ┌───────────────────────────────────────────┐
                      │             User's Browser                │
                      └─────────────┬─────────────────────────────┘
                                    │
            ┌───────────────────────┴───────────────────────┐
            │                                               │
   [Frontend Assets]                               [API REST Requests]
            │                                               │
            ▼                                               ▼
┌──────────────────────────┐                    ┌──────────────────────────┐
│          Vercel          │                    │          Render          │
│   (HTML5, CSS, JS)       │                    │    (Spring Boot 3.4)     │
│   nexthire.vercel.app    │                    │  nexthire.onrender.com   │
└──────────────────────────┘                    └──────────────────────────┘
```

- **Backend**: Spring Boot 3.4 (Java 21) REST API hosted on [Render](https://render.com) as a Docker Web Service (Free Tier).
- **Frontend**: Responsive HTML5 / Vanilla JS / CSS hosted on [Vercel](https://vercel.com) (Free Hobby Tier).
- **CORS**: Fully preconfigured in `backend/src/main/java/com/nexthire/config/CorsConfig.java` to accept requests from any `*.vercel.app` origin with credentials support.
- **Cold-Start Wake-Up**: `config.js` automatically initiates a background health check upon page load to pre-warm the Render container.

---

## Step 1: Push Backend & Configs to GitHub

1. Ensure all files (including the newly added `backend/` directory) are staged and pushed to your GitHub repository:
   ```bash
   git add .
   git commit -m "Add backend service and configure Render + Vercel deployment"
   git push origin main
   ```
2. Verify on GitHub: Go to your repository `https://github.com/adityaxyz09/NextHire_project` and confirm that the `backend/` directory is visible.

---

## Step 2: Deploy Backend on Render (100% Free)

You can deploy using either the **Render Blueprint** (simplest) or create a **Web Service** manually.

### Option A: Render Blueprint (Recommended)
1. Log in to [Render Dashboard](https://dashboard.render.com).
2. Click **New +** (top right) → **Blueprint**.
3. Select your repository: **`adityaxyz09/NextHire_project`**.
4. Render will read `render.yaml` automatically and configure:
   - **Service Name**: `nexthire-backend`
   - **Runtime**: `Docker`
   - **Plan**: `Free`
   - **Port**: `8080`
   - **Health Check Path**: `/api/health`
5. Click **Apply**.
6. Wait 3–4 minutes for Render to build the Docker image and deploy.
7. Once deployment succeeds, copy your live backend URL (e.g., `https://nexthire-backend-xxxx.onrender.com`).

---

### Option B: Manual Web Service Setup on Render
1. In [Render Dashboard](https://dashboard.render.com), click **New +** → **Web Service**.
2. Select your repository: **`adityaxyz09/NextHire_project`**.
3. Fill in the service details:
   - **Name**: `nexthire-backend` (or your choice)
   - **Region**: Choose closest to you (e.g., Singapore, Frankfurt, or Oregon)
   - **Language / Runtime**: **`Docker`**
   - **Branch**: `main`
   - **Root Directory**: Leave blank (uses root `Dockerfile`) or set to `backend`
   - **Instance Type**: **`Free`**
4. Expand **Advanced** and set:
   - **Health Check Path**: `/api/health`
5. Click **Create Web Service**.
6. When deployment finishes, copy your live backend URL (e.g. `https://nexthire-backend-xxxx.onrender.com`).

---

### Verify Backend Deployment
Open this URL in your browser:
```
https://<your-backend-name>.onrender.com/api/health
```
You should see:
```json
{
  "status": "UP",
  "service": "nexthire-backend",
  "timestamp": "..."
}
```
You can also view the interactive Swagger API documentation at:
```
https://<your-backend-name>.onrender.com/swagger-ui.html
```

> [!NOTE]
> **Render Free Tier Cold-Starts**: Render free services spin down after 15 minutes of inactivity. When a new request arrives, it takes ~30–45 seconds to wake up. `config.js` will automatically ping `/api/health` on frontend page load to start warming it up in advance!

---

## Step 3: Deploy Frontend on Vercel (100% Free)

1. Go to [Vercel](https://vercel.com) and log in with GitHub.
2. Click **Add New...** → **Project**.
3. Import your repository: **`adityaxyz09/NextHire_project`**.
4. Configure settings:
   - **Framework Preset**: `Other`
   - **Root Directory**: `./` (leave default)
   - **Build Command**: (leave empty)
   - **Output Directory**: (leave empty)
5. Click **Deploy**.
6. In ~15 seconds, your frontend will be live at `https://nexthire-project-xxxx.vercel.app`!

---

## Step 4: Connect Frontend to Render Backend

You have two easy options to connect your Vercel frontend to your Render backend:

### Option 1: Instant URL Query Parameter (Zero Redeploy)
Open your live Vercel site with your Render backend URL appended as a query parameter:
```
https://<your-frontend>.vercel.app?apiUrl=https://<your-backend>.onrender.com
```
NextHire will automatically save the backend URL in browser `localStorage` and persist it for all future visits!

---

### Option 2: Set in `config.js` (Permanent for all visitors)
1. Open `config.js`.
2. Update line 17 with your actual Render URL:
   ```javascript
   var DEPLOYED_RENDER_URL = 'https://<your-backend-name>.onrender.com';
   ```
3. Commit and push:
   ```bash
   git add config.js backend/src/main/resources/static/config.js
   git commit -m "Update Render backend URL"
   git push origin main
   ```
4. Vercel will automatically redeploy in ~10 seconds with the new backend URL live for all users!

---

## Step 5: Test Full System Functionality

Once both are deployed and connected:
1. Open your Vercel frontend in your browser.
2. Open DevTools (`F12` → `Console`). You will see:
   ```
   [NextHire] Active Backend Origin: https://... | API Endpoint: https://.../api
   ✅ [NextHire] Render backend is UP and responsive: { status: 'UP', service: 'nexthire-backend' }
   ```
3. Test **User Authentication**:
   - Navigate to `login.html`.
   - Sign up with a candidate profile or sign in with test credentials:
     - **Email**: `alex.smith@example.com`
     - **Password**: `password123`
4. Test **AI Mock Interview**:
   - Select a role (e.g., Software Engineering or Java).
   - Click **Start practicing** to fetch real AI questions from the backend.
   - Submit answers and view evaluation scores and analytics.
5. Test **Game Arena**:
   - Click **Game Arena** in the navbar (`arena.html`).
   - Play career domain challenges and track your streak and XP!
