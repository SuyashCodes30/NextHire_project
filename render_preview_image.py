from PIL import Image, ImageDraw, ImageFont
import os

# Create 1920x1080 slide image
width, height = 1920, 1080
img = Image.new('RGB', (width, height), color='#FFFFFF')
draw = ImageDraw.Draw(img)

# Try loading fonts, fall back to default
try:
    font_title = ImageFont.truetype("arialbd.ttf", 36)
    font_h1 = ImageFont.truetype("arialbd.ttf", 24)
    font_h2 = ImageFont.truetype("arialbd.ttf", 20)
    font_bold = ImageFont.truetype("arialbd.ttf", 16)
    font_regular = ImageFont.truetype("arial.ttf", 15)
    font_small = ImageFont.truetype("arial.ttf", 13)
    font_tiny = ImageFont.truetype("arial.ttf", 11)
except Exception:
    font_title = font_h1 = font_h2 = font_bold = font_regular = font_small = font_tiny = ImageFont.load_default()

# 1. Main Titles
# "TECHNICAL ARCHITECTURE"
draw.text((450, 40), "TECHNICAL ARCHITECTURE", fill="#0F172A", font=font_title, anchor="mm")
# "METHODOLOGY"
draw.text((1640, 40), "METHODOLOGY", fill="#0F172A", font=font_title, anchor="mm")

# Helper for rounded rectangles
def draw_rounded_rect(xy, fill, outline, width=2, radius=12):
    draw.rounded_rectangle(xy, radius=radius, fill=fill, outline=outline, width=width)

# Helper for dashed border
def draw_dashed_rounded_rect(xy, outline="#94A3B8", width=2, radius=16, dash_len=8, gap_len=6):
    draw_rounded_rect(xy, fill=None, outline=outline, width=width, radius=radius)

# LEFT CONTAINER: Dashed Box
arch_box = (60, 95, 870, 1020)
draw_dashed_rounded_rect(arch_box, outline="#94A3B8", width=2)

# Rotated Side Labels (rendered vertically or as clear tags)
draw.text((75, 420), "External API Integration", fill="#475569", font=font_bold)
draw.text((75, 750), "AI & Data Layer", fill="#475569", font=font_bold)

# Box 1: User Interface
ui_box = (250, 120, 780, 260)
draw_rounded_rect(ui_box, fill="#E0F2FE", outline="#38BDF8", width=2)
draw.text((515, 150), "User Interface", fill="#0369A1", font=font_h2, anchor="mm")
draw.text((515, 185), "NextHire Web Application (HTML5 / Vanilla JS / CSS3)", fill="#0F172A", font=font_bold, anchor="mm")
draw.text((515, 215), "Interactive Arena  |  Voice & Audio Suite  |  Dark & Light Theme Support", fill="#475569", font=font_regular, anchor="mm")

# Down Arrow UI -> Backend
draw.polygon([(515, 290), (505, 270), (525, 270)], fill="#64748B")
draw.rectangle([(512, 260), (518, 270)], fill="#64748B")

# Row 2 (3 Boxes): APIs | API Gateway / Backend | Auth & Security
api_box = (100, 300, 340, 560)
draw_rounded_rect(api_box, fill="#FFFFFF", outline="#334155", width=2)
draw.text((220, 330), "APIs", fill="#0F172A", font=font_h2, anchor="mm")
api_items = [
    "• Web Speech API (STT Voice)",
    "• Web Audio API (TTS Audio)",
    "• OpenAI & Gemini LLM APIs",
    "• Swagger 3.0 / OpenAPI Spec",
    "• RESTful JSON Endpoints"
]
for i, item in enumerate(api_items):
    draw.text((120, 370 + i * 36), item, fill="#334155", font=font_small)

# Center Box: Backend
be_box = (370, 300, 610, 560)
draw_rounded_rect(be_box, fill="#DCFCE7", outline="#22C55E", width=2)
draw.text((490, 325), "API Gateway / Backend", fill="#15803D", font=font_bold, anchor="mm")
draw.text((490, 355), "Spring Boot 3.4 (Java 21)", fill="#166534", font=font_bold, anchor="mm")
be_items = [
    "• Java 21 RESTful API",
    "• Spring MVC Dispatcher",
    "• Controllers & Service Layer",
    "• Port 8080 Routing",
    "• CORS Origin Whitelist"
]
for i, item in enumerate(be_items):
    draw.text((390, 385 + i * 32), item, fill="#14532D", font=font_small)

# Right Box: Auth & Security
sec_box = (640, 300, 850, 560)
draw_rounded_rect(sec_box, fill="#F3E8FF", outline="#A855F7", width=2)
draw.text((745, 330), "Authentication & Security", fill="#7E22CE", font=font_bold, anchor="mm")
sec_items = [
    "• Auth Controller (/api/auth)",
    "• JWT & Session Management",
    "• BCrypt Credential Protection",
    "• Role-Based Access Guards",
    "• CORS Cross-Origin Filter"
]
for i, item in enumerate(sec_items):
    draw.text((655, 370 + i * 36), item, fill="#581C87", font=font_small)

# Arrows between APIs <-> Backend <-> Auth
draw.polygon([(360, 430), (345, 425), (345, 435)], fill="#64748B")
draw.polygon([(350, 430), (365, 425), (365, 435)], fill="#64748B")
draw.polygon([(630, 430), (615, 425), (615, 435)], fill="#64748B")
draw.polygon([(620, 430), (635, 425), (635, 435)], fill="#64748B")

# Down Arrows to Row 3
draw.polygon([(330, 600), (320, 580), (340, 580)], fill="#64748B")
draw.rectangle([(327, 565), (333, 580)], fill="#64748B")

draw.polygon([(670, 600), (660, 580), (680, 580)], fill="#64748B")
draw.rectangle([(667, 565), (673, 580)], fill="#64748B")

# Row 3: AI Engine | Data & Storage
ai_box = (100, 610, 460, 810)
draw_rounded_rect(ai_box, fill="#F0FDFA", outline="#14B8A6", width=2)
draw.text((280, 635), "AI / LLM Engine", fill="#0F766E", font=font_h2, anchor="mm")
ai_items = [
    "• Prompt-based Interview Simulation",
    "• STAR Framework Evaluation Engine",
    "• Heuristic Answer Scoring (0 - 100)",
    "• Real-time Verbal Feedback Generator",
    "• Adaptive Role & Difficulty Logic"
]
for i, item in enumerate(ai_items):
    draw.text((120, 665 + i * 27), item, fill="#134E4A", font=font_small)

db_box = (490, 610, 850, 810)
draw_rounded_rect(db_box, fill="#EFF6FF", outline="#3B82F6", width=2)
draw.text((670, 635), "Data & Storage", fill="#1D4ED8", font=font_h2, anchor="mm")
db_items = [
    "• In-Memory Progress Tracker & Mock DB",
    "• Candidate Profiles & Interview History",
    "• Gamification Records: XP & Streaks (🔥)",
    "• Game Arena High Score Maps",
    "• JPA / Hibernate (PostgreSQL-Ready)"
]
for i, item in enumerate(db_items):
    draw.text((510, 665 + i * 27), item, fill="#1E3A8A", font=font_small)

# Down Arrow to Cloud
draw.polygon([(515, 850), (505, 830), (525, 830)], fill="#64748B")
draw.rectangle([(512, 815), (518, 830)], fill="#64748B")

# Row 4: Cloud & Deployment
dep_box = (100, 860, 850, 990)
draw_rounded_rect(dep_box, fill="#FFFFFF", outline="#0F172A", width=2)
draw.text((475, 890), "Cloud & Deployment", fill="#0F172A", font=font_h2, anchor="mm")
draw.text((475, 940), "▲ Vercel (Frontend CDN)    ☁️ Render (Docker Web Service)    🐳 Docker (Java 21 JVM)    🐙 GitHub (CI/CD Repo)", fill="#1E293B", font=font_bold, anchor="mm")


# CENTER COLUMN: Tech Stack & Key Technical Highlights
ts_box = (900, 95, 1420, 520)
draw_rounded_rect(ts_box, fill="#FEE2E2", outline="#FCA5A5", width=2)
draw.text((930, 130), "Tech Stack", fill="#991B1B", font=font_h1)

ts_items = [
    ("• Frontend", "HTML5, Vanilla JS (ES6+), CSS3 (Modern Dark/Light UI)"),
    ("• Backend", "Java 21, Spring Boot 3.4.3 REST API (Maven)"),
    ("• Database", "In-Memory Concurrent Store (PostgreSQL / JPA Architecture)"),
    ("• AI Engine", "Heuristic STAR Evaluator + LLM APIs (OpenAI / Gemini)"),
    ("• Authentication", "Session & Token Security, CORS Registry"),
    ("• Deployment", "Vercel (Frontend) + Render (Docker JVM) + GitHub Actions")
]
for i, (cat, val) in enumerate(ts_items):
    draw.text((930, 180 + i * 55), cat, fill="#7F1D1D", font=font_bold)
    draw.text((950, 205 + i * 55), val, fill="#1E293B", font=font_regular)

kth_box = (900, 550, 1420, 1020)
draw_rounded_rect(kth_box, fill="#E0F2FE", outline="#7DD3FC", width=2)
draw.text((930, 585), "Key Technical Highlights", fill="#0369A1", font=font_h1)

kth_items = [
    "• Real-time AI Mock Interviews with adaptive role & difficulty questioning",
    "• Automated STAR Method evaluation with quantitative scoring (0-100)",
    "• Duolingo-style Gamified Arena with XP (⚡), daily streaks (🔥) & badges",
    "• Dual-mode Audio System with Speech-to-Text & Speech Synthesis",
    "• Interactive Swagger / OpenAPI 3.0 documentation & live test UI",
    "• Zero-cost serverless frontend on Vercel & Docker backend on Render"
]
for i, item in enumerate(kth_items):
    draw.text((930, 640 + i * 60), item, fill="#0F172A", font=font_regular)


# RIGHT COLUMN: METHODOLOGY FLOW (6 Steps + Badges + Arrows)
meth_x1, meth_x2 = 1450, 1860
card_h = 110
gap = 42

steps = [
    ("1", "1. ONBOARD & PROFILE", "Register / Login → Select Target Role & Skills"),
    ("2", "2. SELECT & CONFIGURE", "Choose Category (Tech/HR/Arena) → Set Difficulty"),
    ("3", "3. AI MOCK INTERVIEW", "Adaptive AI Questions → Voice / Text Real-time Input"),
    ("4", "4. STAR EVALUATION", "Automated Scoring (0-100) → STAR Analysis & Feedback"),
    ("5", "5. EARN XP & ARENA", "Earn XP (⚡) & Streaks (🔥) → Game Arena Level Progress"),
    ("6", "6. CAREER IMPACT", "Readiness Report → Confidence Boost → Dream Job Ready")
]

for i, (num, title, desc) in enumerate(steps):
    y1 = 100 + i * (card_h + gap)
    y2 = y1 + card_h
    # Card
    draw_rounded_rect((meth_x1, y1, meth_x2, y2), fill="#FFFFFF", outline="#22C55E", width=2)
    # Circle badge
    badge_box = (meth_x1 + 18, y1 + 22, meth_x1 + 84, y1 + 88)
    draw.ellipse(badge_box, fill="#DCFCE7", outline="#16A34A", width=2)
    draw.text((meth_x1 + 51, y1 + 55), num, fill="#16A34A", font=font_h2, anchor="mm")
    # Title & text
    draw.text((meth_x1 + 105, y1 + 32), title, fill="#15803D", font=font_bold)
    draw.text((meth_x1 + 105, y1 + 65), desc, fill="#334155", font=font_small)

    # Down arrow between cards
    if i < 5:
        ay = y2 + 8
        mid_x = (meth_x1 + meth_x2) // 2
        draw.polygon([(mid_x, ay + 24), (mid_x - 12, ay + 8), (mid_x + 12, ay + 8)], fill="#16A34A")
        draw.rectangle([(mid_x - 4, ay), (mid_x + 4, ay + 8)], fill="#16A34A")

# Save PNG image
output_img = "NextHire_Technical_Architecture_and_Methodology_Preview.png"
img.save(output_img, quality=95)
print(f"High-resolution preview image saved: {output_img}")
