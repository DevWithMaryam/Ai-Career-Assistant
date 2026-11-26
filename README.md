# 🤖 AI Career Assistant

> An AI-powered Android application that helps students and job seekers
> accelerate their careers through intelligent resume analysis, skill gap
> detection, personalised learning roadmaps, and interview preparation.

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Java-ED8B00?logo=java&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-blue)
![Min SDK](https://img.shields.io/badge/Min%20SDK-API%2024-yellow)
![Firebase](https://img.shields.io/badge/Firebase-Auth%20%2B%20Firestore-FF6F00?logo=firebase&logoColor=white)
![AI](https://img.shields.io/badge/Gemini-Flash-8E24AA?logo=google&logoColor=white)

---

## Overview

AI Career Assistant is a professional Android portfolio project built with
**Java**, **MVVM architecture**, and **Gemini AI**. Users upload their PDF
resume, receive an AI-powered analysis with a score, discover missing skills
for their target career, get a personalised 4-week learning roadmap, and
practice with AI-generated interview questions — all in one app.

---

## Features

| Feature | Description |
|---------|----------|
| 🔐 Authentication | Email/password login with **email verification enforced** |
| 📄 Resume Upload | PDF picker, local text extraction via PDFBox, saved in Firestore, one-tap reuse         |
| 🧠 Resume Analysis | AI score (0–100), strengths, weaknesses and suggestions |
| 🎯 Career Selection | Android, Frontend, Backend , Data Analyst or target career |
| 📊 Skill Gap Detection | Compare resume skills vs target career requirements |
| 🗺️ Learning Roadmap | Personalised 4-week AI-generated weekly plan |
| 💬 Interview Prep | Technical, HR and resume-based questions via Gemini AI |
| 📋 History | Past analyses loaded from Firestore with score badges |
| 👤 Profile | Update name, change career target, upload new resume |


---

## Architecture

The app follows **MVVM + Repository Pattern** with strict separation of concerns
across four layers:

    UI Layer (Fragments)
          ↓  observes LiveData
    ViewModel Layer
          ↓  calls repository methods
    Repository Layer
          ↓  reads / writes
    Data Layer
      ├── Firebase Auth     (user authentication + email verification)
      ├── Cloud Firestore   (user profiles, analysis history)
      └── Gemini Flash API   (AI analysis via Retrofit)

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Java | Primary programming language |
| XML + Material Design 3 | UI layouts and components |
| MVVM + Repository Pattern | Architecture |
| Firebase Authentication | Login, signup, email verification |
| Cloud Firestore | User profiles, analysis history |
| User profiles, resume text, analysis history|
| Gemini Flash API | AI resume analysis, skill gap, roadmap, interview |
| Retrofit + OkHttp | Network layer for Gemini API |
| PDFBox-Android | Local PDF text extraction |
| Android Navigation Component | Single-Activity fragment navigation |
| LiveData + ViewModel | Reactive UI state management |
| ViewBinding | Type-safe view references |

---

## Package Structure

    com.maryam.aicareerassistant
    ├── activities/      MainActivity (single-activity host)
    ├── fragments/       All 13 screen fragments
    ├── viewmodel/       AuthViewModel, ResumeViewModel, CareerViewModel
    ├── repository/      AuthRepository, ResumeRepository, GeminiRepository
    ├── network/         ApiClient, ApiService (Retrofit + Gemini)
    ├── firebase/        FirebaseAuthHelper, StorageHelper
    ├── model/           User, ResumeAnalysis, GeminiRequest, GeminiResponse
    ├── adapter/         DashboardAdapter, HistoryAdapter
    └── utils/           Resource, Validator, PdfExtractor,
                         PromptBuilder, JsonParser, FileUtils, Constants

---

## App Screens

| # | Screen | Description |
|---|--------|-|
| 1 | Splash | Session check with server-side token validation |
| 2 | Login | Firebase auth with email verification enforcement |
| 3 | Signup | Account creation + automatic verification email |
| 4 | Forgot Password | Reset link sent to registered email |
| 5 | Home Dashboard | 6 feature cards with Material Design grid |
| 6 | Resume Upload | PDF picker, size validation, local text extraction, saved resume reuse|
| 7 | Resume Analysis | AI score, strengths, weaknesses, suggestions |
| 8 | Career Selection | 4 preset careers + custom "Other" career input|
| 9 | Skill Gap | Existing vs missing vs priority skills |
| 10 | Learning Roadmap | 4-week personalised plan with tasks and resources |
| 11 | Interview Prep | Technical, HR and resume-based questions |
| 12 | History | Past analyses with score badges from Firestore |
| 13 | Profile | Name, career, resume management |

---

## Firestore Structure

    users (collection)
    └── {userId} (document)
        ├── name: string
        ├── email: string
        ├── targetCareer: string
        ├── resumeText: string
            resumeFileName: string
            resumeUpdatedAt: number
        │
        ├── analysis (subcollection)
        │   └── {docId}
        │       ├── score: number
        │       ├── strengths: array
        │       ├── weaknesses: array
        │       ├── suggestions: array
        │       └── timestamp: number
        │
        ├── roadmaps (subcollection)
        │   └── {docId}
        │       ├── roadmapJson: string
        │       ├── career: string
        │       └── timestamp: number
        │
        └── interviews (subcollection)
            └── {docId}
                ├── interviewJson: string
                ├── career: string
                └── timestamp: number

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or newer
- Java 17
- Android device or emulator (API 24+)
- Firebase project
- Gemini API key from [Google AI Studio](https://aistudio.google.com/app/apikey)

### Setup

**1. Clone or download the repository**

Download the project and open it in Android Studio.

**2. Add Firebase configuration**

- Create a project at [Firebase Console](https://console.firebase.google.com)
- Enable **Email/Password** authentication
- Create a **Firestore** database
- Download `google-services.json` and place it in the `app/` folder

**3. Add your Gemini API key**

Open `local.properties` in the project root and add:

```properties
GEMINI_API_KEY=your_api_key_here
```

**4. Sync and run**

Open in Android Studio, click **Sync Now**, then run on a device or emulator.

---

## Security

- `google-services.json` is excluded from version control via `.gitignore`
- `local.properties` (API key) is excluded from version control
- Firestore rules enforce `request.auth != null` for all reads and writes
- Email verification is required before login is permitted
## Future Improvements

- Store the original PDF in Firebase Storage once billing is enabled
- OCR support for scanned resumes
- Offline caching with Room
- Unit tests for ViewModels and repositories
---

## Developer

**Maryam Akram** — Android Developer

- GitHub: [DevWithMaryam](https://github.com/DevWithMaryam)
- LinkedIn: [linkedin.com/in/maryam-akram-2b183b338](https://www.linkedin.com/in/maryam-akram-2b183b338/)

---

## License

Copyright 2026 Maryam Akram. All Rights Reserved.

This project is created for portfolio and demonstration purposes.
The source code may be viewed publicly but may not be copied, modified,
redistributed, or used in other projects without explicit written
permission from the author.

For licensing inquiries or collaboration, please reach out via LinkedIn.

---

*Built to demonstrate modern Android development with AI integration,
clean MVVM architecture, and a production-ready Firebase backend.*