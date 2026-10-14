# NextHire AI Platform — Deployment Guide

This guide covers deploying the **NextHire Spring Boot Backend** on **Render** (Free Web Service) and the **Frontend** on **Vercel** (or **Render Static Sites**) completely free.

---

## Architecture Overview

- **Backend**: Spring Boot 3.4 (Java 21) REST API hosted on [Render](https://render.com) as a Docker Web Service (Free Tier).
- **Frontend**: Responsive HTML5 / Vanilla JS / CSS hosted on [Vercel](https://vercel.com) (or Render Static Sites) with zero configuration.

---

## Part 1: Deploy Backend to Render (Free)

### Option A: 1-Click Render Blueprint (Recommended)
1. Push your changes to GitHub:
   ```bash
   git add .
   git commit -m "Configure Render and Vercel deployment"
   git push origin main
   ```
2. Go to [Render Dashboard](https://dashboard.render.com).
3. Click **New +** in the top navigation bar and select **Blueprint**.
4. Connect your GitHub repository: `adityaxyz09/NextHire`.
5. Render will automatically read `render.yaml` and configure:
   - `nexthire-backend`: Docker Web Service running on port `8080` with memory optimizations.
   - `nexthire-frontend`: Static site hosted directly on Render (optional).
6. Click **Apply**.
7. Wait 2–3 minutes for the build to finish. Once deployed, note your Render backend URL (e.g., `https://nexthire-backend-xxxx.onrender.com`).

---

### Option B: Manual Web Service Setup on Render
If you prefer configuring manually instead of using Blueprint:
1. In [Render Dashboard](https://dashboard.render.com), click **New +** → **Web Service**.
2. Select your repository: `adityaxyz09/NextHire`.
3. Fill in the service details:
   - **Name**: `nexthire-backend`
   - **Region**: Choose closest to you (e.g., Singapore or Oregon)
   - **Language**: `Docker`
   - **Root Directory**: `backend` (or leave blank; Dockerfiles are provided for both!)
   - **Instance Type**: `Free`
4. Expand **Advanced** and set:
   - **Health Check Path**: `/api/health`
5. Click **Create Web Service**.
6. When deployment finishes, copy your live backend URL (e.g. `https://nexthire-backend.onrender.com`).
7. Test the health endpoint in your browser: `https://<your-backend-name>.onrender.com/api/health`. You should see `{"status":"UP","service":"nexthire-backend"}`.

> [!NOTE]
> Render Free tier services spin down after 15 minutes of inactivity. When a new request arrives, it takes ~30–50 seconds to wake up (cold start).

---

## Part 2: Deploy Frontend (100% Free)

You can choose either **Vercel** (most popular) or **Render Static Site**.

### Choice 1: Deploy on Vercel (Recommended)
1. Go to [Vercel](https://vercel.com) and log in with your GitHub account.
2. Click **Add New...** → **Project**.
3. Import your repository: `adityaxyz09/NextHire`.
4. Keep the default settings:
   - **Framework Preset**: `Other`
   - **Root Directory**: `./` (leave default)
   - **Build Command**: (leave blank)
   - **Output Directory**: (leave blank)
5. Click **Deploy**.
6. Within 15 seconds, your frontend will be live at `https://nexthire-xxxx.vercel.app`!

---

## Part 3: Connect Frontend to Backend

Now link your deployed frontend to your live Render backend URL:

1. Open `config.js` in your project.
2. Replace the empty `apiBaseUrl` with your Render backend URL:
   ```javascript
   // config.js
   var config = {
       apiBaseUrl: 'https://nexthire-backend-xxxx.onrender.com',
       ...
   ```
3. Commit and push the change:
   ```bash
   git add config.js backend/src/main/resources/static/config.js
   git commit -m "Update backend URL in config.js"
   git push origin main
   ```
4. Vercel (or Render) will automatically redeploy within seconds!

---

### Instant Testing without Re-pushing:
You can also connect your frontend to your Render backend immediately in your browser:
- Open your deployed frontend: `https://your-site.vercel.app?apiUrl=https://your-backend.onrender.com`
- NextHire will automatically save the backend URL in `localStorage` and connect right away!
