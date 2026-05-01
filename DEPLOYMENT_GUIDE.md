# 🚀 SkillHire AI — Production Deployment Guide

## Updated Folder Structure

```
skillhire-ai/
├── backend/
│   ├── src/main/java/com/skillhire/
│   │   ├── SkillHireAiApplication.java        ← @EnableAsync + @EnableScheduling
│   │   ├── config/
│   │   │   └── SecurityConfig.java            ← CORS from env var
│   │   ├── controller/
│   │   │   └── AuthController.java            ← /auth/notifications endpoint
│   │   ├── service/
│   │   │   ├── AuthService.java               ← sends welcome email on register
│   │   │   ├── GeminiAIService.java           ← production-ready, key from env
│   │   │   ├── JobService.java                ← triggers email on job post
│   │   │   ├── EmailNotificationService.java  ← HTML email templates (NEW)
│   │   │   └── EmailSchedulerService.java     ← weekly digest scheduler (NEW)
│   │   ├── entity/
│   │   │   └── User.java                      ← emailNotificationsEnabled field
│   │   ├── repository/
│   │   │   ├── UserRepository.java            ← findByRoleAndEmailNotifications
│   │   │   └── JobRepository.java             ← findRecentActiveJobs
│   │   └── dto/response/
│   │       └── JwtResponse.java               ← emailNotificationsEnabled field
│   ├── src/main/resources/
│   │   └── application.properties             ← all secrets from env vars
│   └── .env.example                           ← template for Render
│
└── frontend/
    ├── index.html                             ← Vite root HTML
    ├── vite.config.js                         ← Vite config (replaces CRA)
    ├── vercel.json                            ← SPA routing fix
    ├── tailwind.config.js                     ← dark mode enabled
    ├── package.json                           ← migrated to Vite
    ├── .env.example                           ← VITE_API_URL template
    ├── .env.local                             ← local dev (gitignored)
    └── src/
        ├── App.jsx                            ← ThemeProvider added
        ├── main.jsx                           ← Vite entry point
        ├── index.css                          ← dark mode + Poppins
        ├── context/
        │   ├── AuthContext.jsx
        │   └── ThemeContext.jsx               ← dark mode (NEW)
        ├── services/
        │   └── api.js                         ← uses import.meta.env.VITE_API_URL
        └── components/common/
            ├── Navbar.jsx                     ← dark toggle + mobile menu
            └── UI.jsx                         ← reusable components (NEW)
```

---

## 🌐 1. Deploying Backend to Render

### Step 1: Create a Web Service

1. Go to [render.com](https://render.com) → **New → Web Service**
2. Connect your GitHub repo
3. Set the **Root Directory** to `backend`
4. Configure:
   - **Runtime**: Docker (use the Dockerfile) OR Java
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -jar target/skillhire-ai-1.0.0.jar`

### Step 2: Add Environment Variables in Render Dashboard

Go to your service → **Environment** tab and add:

```
DATABASE_URL          = jdbc:postgresql://<render-db-host>:5432/skillhire_db
DATABASE_USERNAME     = <your_db_user>
DATABASE_PASSWORD     = <your_db_password>
JWT_SECRET            = <run: openssl rand -hex 64>
GEMINI_API_KEY        = <from Google AI Studio>
MAIL_USERNAME         = your_gmail@gmail.com
MAIL_PASSWORD         = <16-char Gmail App Password>
MAIL_FROM             = noreply@skillhire.ai
ALLOWED_ORIGINS       = https://skillhire-ai.vercel.app
FRONTEND_URL          = https://skillhire-ai.vercel.app
```

### Step 3: Create PostgreSQL Database on Render

1. **New → PostgreSQL**
2. Copy the **Internal Database URL** → set as `DATABASE_URL` (change `postgresql://` to `jdbc:postgresql://`)

---

## ▲ 2. Deploying Frontend to Vercel

### Step 1: Push to GitHub

Ensure your repo has the updated frontend with `vite.config.js` and `vercel.json`.

### Step 2: Import to Vercel

1. Go to [vercel.com](https://vercel.com) → **Add New Project**
2. Import your GitHub repository
3. Set **Root Directory** to `frontend`
4. Framework should auto-detect as **Vite**

### Step 3: Add Environment Variables in Vercel

Go to **Settings → Environment Variables**:

```
VITE_API_URL = https://your-backend-name.onrender.com/api
```

> ⚠️ Must start with `VITE_` to be exposed to the browser bundle.

### Step 4: Deploy

Click **Deploy**. Vercel will run `npm run build` → generates `dist/` → serves it.

The `vercel.json` ensures all routes (e.g., `/candidate/dashboard`) redirect to `index.html` for SPA routing.

---

## 🔑 3. Gemini API Setup

1. Go to [aistudio.google.com](https://aistudio.google.com/app/apikey)
2. Click **Create API Key**
3. Copy the key → set as `GEMINI_API_KEY` in Render
4. The model used is `gemini-1.5-flash` (fast and free tier available)

---

## 📬 4. Gmail SMTP Setup

1. Enable **2-Factor Authentication** on your Gmail account
2. Go to: **Google Account → Security → 2-Step Verification → App Passwords**
3. Select **Mail** → **Other** → name it "SkillHire AI"
4. Copy the 16-character password (shown once)
5. Set as `MAIL_PASSWORD` in Render (remove spaces)

---

## 🌙 5. Dark Mode Usage

The dark mode is:
- Stored in `localStorage` as `'theme': 'dark'|'light'`
- Activated via Tailwind's `dark:` class strategy
- Toggled with the moon/sun button in the Navbar
- Respects system preference on first load

---

## 📧 6. Email Notification System

| Email Type | When Triggered |
|-----------|---------------|
| Welcome email | User registers |
| Job match | New job posted (to all subscribed candidates) |
| Application status | Recruiter updates application status |
| Weekly digest | Every Monday 9 AM IST (scheduled) |

Users can enable/disable notifications via the `PUT /api/auth/notifications` endpoint.

---

## 🔒 7. Security Checklist

- [x] No hardcoded secrets in any file
- [x] All secrets loaded from environment variables
- [x] CORS restricted to Vercel domain only in production
- [x] JWT tokens expire in 24 hours
- [x] Passwords hashed with BCrypt
- [x] Email sending is async (non-blocking)
- [x] Input validation on all DTOs (@Valid)
- [x] SQL injection prevented (Spring Data JPA)
- [x] `.env.local` is gitignored

---

## 📁 8. .gitignore Additions

Add these to your `.gitignore`:

```gitignore
# Environment files
.env
.env.local
.env.production
backend/.env

# Build outputs
frontend/dist/
backend/target/

# Uploads
backend/uploads/
```

---

## ⚡ 9. Local Development

### Backend
```bash
cd backend
# Create application-local.properties with your local DB/keys
./mvnw spring-boot:run
# Runs at http://localhost:8080/api
```

### Frontend
```bash
cd frontend
cp .env.example .env.local
# Edit .env.local: VITE_API_URL=http://localhost:8080/api
npm install
npm run dev
# Runs at http://localhost:3000
```

---

## 🆘 Common Issues & Fixes

| Problem | Fix |
|---------|-----|
| CORS error in production | Add your Vercel URL to `ALLOWED_ORIGINS` in Render |
| 404 on page refresh in Vercel | Ensure `vercel.json` is in `frontend/` root |
| Build fails on Vercel | Check `vite.config.js` exists, not `react-scripts` |
| Gemini API timeout | Increase `app.gemini.timeout.read` in properties |
| Emails not sending | Check Gmail App Password (not account password) |
| DB connection error | Use `jdbc:postgresql://` not `postgresql://` for `DATABASE_URL` |
| Render free tier sleeps | Add a health check ping service or upgrade plan |
