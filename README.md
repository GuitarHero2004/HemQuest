# 🏮 HẻmQuest

> **Every alley has a story. Turn every step into a quest.**

**HẻmQuest** is a gamified urban exploration and cultural heritage mobile app for Android that transforms overlooked neighbourhoods, historic alleys (hẻm), craft workshops, and local street stories into interactive walking experiences.

By combining **Google Gemini Multimodal AI, location intelligence, gamification, cultural storytelling, and active green mobility**, HẻmQuest helps users discover local heritage, explore cities on foot, support neighbourhood micro-economies, and preserve living urban stories.

---

## 🌆 The Idea & Motivation

Ho Chi Minh City is famous for major tourist destinations such as Nguyễn Huệ Walking Street, Bến Thành Market, and landmark monuments. However, the true cultural soul and historical memory of the city live inside its **hẻm — the labyrinthine alleys and vibrant residential enclaves**.

These hidden communities contain:
- Traditional craft guilds (e.g., Phú Bình glass lantern makers, woodcarvers)
- Multi-generational family-run eateries and 70-year-old sock-filter coffee shops (*Cà phê vợt*)
- French colonial villas, mid-century modernist residences, and communal shrines (*Miếu, Đình*)
- Living oral histories and community micro-heritage

Yet discovering them independently is difficult for younger generations and international travellers due to fragmented information and lack of contextual guidance. **HẻmQuest turns the city into an interactive, playable cultural exploration map.**

---

## 🎯 Problem Statement

1. **Overtourism & Centralized Footfall:** Visitors concentrate heavily in crowded downtown areas, leaving historic craft alleys and local family businesses without discovery channels.
2. **Fragmented Cultural Knowledge:** Rich oral histories, architectural backstories, and traditional techniques are scattered across oral archives without a unified digital platform.
3. **Passive Tourism vs. Active Engagement:** Traditional mapping tools offer simple *Search → Navigate → Arrive* without fostering deep cultural understanding or physical engagement.
4. **Sedentary Urban Lifestyles:** City dwellers lack engaging incentives that make walking purposeful, rewarding, and environmentally conscious.

---

## 💡 The Solution: Gamified Urban Walking Quests

Instead of passive point-to-point navigation, HẻmQuest empowers users through a cyclic exploration loop:

$$\text{Discover} \longrightarrow \text{Walk} \longrightarrow \text{Learn} \longrightarrow \text{Challenge} \longrightarrow \text{AI Verify} \longrightarrow \text{Collect}$$

Each Quest guides users through curated checkpoints featuring:
- 📍 **Smart Proximity & Nearby Routing:** Prioritizes accessible quests within comfortable walking range ($< 1.5\text{ km}$), reserving distant or regional routes for explicit mock/explore selections.
- 🎙️ **Voice & Speech-to-Text AI Prompting:** Allows users to speak their desired walking mood, duration, or cultural interest to synthesize on-demand custom itineraries.
- 📷 **Gemini Multimodal AI Photo Verification:** Live vision analysis evaluating on-site checkpoint photos against cultural and architectural criteria before granting completion stamps.
- 📚 **Bách Khoa Hẻm (Cultural Encyclopedia):** Offline-accessible digital knowledge archive documenting terminology, heritage sites, and craft lore.
- 🌱 **Green Points & XP Progression:** Rewards sustainable active walking (carbon-reduction metrics) and unlocks badges and physical vouchers.
- 🛂 **Digital Explorer Passport:** An immutable digital passport stamping each completed alley milestone with captured photography and Firestore cloud sync.

---

## ✨ Core Features & Technical Highlights

### 1. 🧭 Dynamic Quest Discovery & Nearby Proximity Filter
- **Walkable Distance Enforcement:** Automatically ranks and presents quests relative to the user's live coordinates. Distant historical journeys ($> 3\text{ km}$) are neatly partitioned into curated exploration catalog modes to prevent unrealistic walking journeys.
- **Categorical Themes:** Culinary (*Ẩm thực hẻm*), Heritage Architecture (*Kiến trúc xưa*), Historical Sites (*Di tích lịch sử*), Craft Guilds (*Làng nghề thủ công*), and Urban Greenery (*Hẻm xanh*).

### 2. 🎙️ Natural Voice & AI-Assisted Quest Synthesis
- **Speech-to-Text Input:** Users can tap the microphone icon and speak prompt instructions (e.g., *"I want a 30-minute quiet architectural walk with iced coffee near District 3"*).
- **Gemini Engine Integration:** Translates voice and natural language prompts into a structured multi-stop JSON quest with coordinates, checkpoints, cultural summaries, and photo challenges.

### 3. 📷 Multimodal AI Vision Verification
- Prevents artificial check-in exploitation. When a user reaches a checkpoint (e.g., *"Photograph the hand-assembled wireframe of a Phú Bình lantern"*), the camera captures the subject and sends it to Gemini Multimodal Vision API.
- The model validates the scene's semantic relevance, provides instant educational commentary, and signs off on the checkpoint.

### 4. 📚 Bách Khoa Hẻm (Saigon Alley Encyclopedia)
- A searchable, offline-first compendium of cultural terms, historical timeline entries, architectural terminology (Indochine, Art Deco, Modernist tube houses), and local gastronomy guides.
- Multilingual accessibility supporting Vietnamese, English, Chinese, Japanese, and Korean.

### 5. 🛂 Digital Explorer Passport & Firestore Cloud Sync
- Every verified photo and completed quest is committed locally to **Room Database** and seamlessly mirrored to **Firebase Cloud Firestore** under `users/{uid}/...`.
- Tracks streaks, badges, cumulative walking distance, and global leaderboards.

---

## 🏗️ Architecture & Tech Stack

```text
┌──────────────────────────────────────────────────────────────────────────┐
│                             HẺMQUEST CLIENT                              │
│  ┌────────────────────────────────────────────────────────────────────┐  │
│  │                     Jetpack Compose UI (M3)                        │  │
│  │   HomeScreen   QuestScreen   GlossaryScreen   Passport/Leaderboard │  │
│  └─────────────────────────────────┬──────────────────────────────────┘  │
│                                    │ StateFlow / Coroutines              │
│  ┌─────────────────────────────────▼──────────────────────────────────┐  │
│  │                       MVVM Presentation Layer                      │  │
│  │     QuestViewModel     UserStatsViewModel     AuthViewModel        │  │
│  └──────────────┬───────────────────────────────┬─────────────────────┘  │
│                 │                               │                        │
│  ┌──────────────▼─────────────┐   ┌─────────────▼─────────────────────┐  │
│  │     Local Persistence      │   │       Services & Repositories     │  │
│  │  • Room DB (v10 Schema)    │   │  • GeminiQuestRepository          │  │
│  │  • DataStore / SharedPrefs │   │  • UserAuthRepository             │  │
│  │  • CulturalGlossaryRepo    │   │  • SpeechRecognitionManager       │  │
│  └────────────────────────────┘   └─────────────┬─────────────────────┘  │
└─────────────────────────────────────────────────┼────────────────────────┘
                                                  │
                                                  ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                            CLOUD & AI SERVICES                           │
│  ┌─────────────────────────┐  ┌────────────────┐  ┌───────────────────┐  │
│  │ Google AI Studio        │  │ Firebase Cloud │  │ Google Credential │  │
│  │ Gemini 2.5 Flash / API  │  │ Firestore DB   │  │ Manager (Sign-In) │  │
│  └─────────────────────────┘  └────────────────┘  └───────────────────┘  │
└──────────────────────────────────────────────────────────────────────────┘
```

- **Runtime & UI:** 100% Kotlin, Jetpack Compose, Material Design 3, Edge-to-Edge with `WindowInsets.safeDrawing`.
- **Concurrency & State:** Kotlin Coroutines, Flow, StateFlow, Lifecycle-aware view models.
- **Local Storage (Offline-first):** Android Room SQLite database with automated migration fallback.
- **Backend & Identity:** Firebase Firestore (Realtime NoSQL sync), Google Credential Manager (One-Tap Google Sign-In).
- **Artificial Intelligence:** Google AI Studio REST / Gemini 2.5 Flash Multimodal Vision & Generation models.

---

## 🎬 Live Product Demonstration Flow (3–4 Mins)

| Timecode | Screen / Action | Presentation Script & Focus |
| :--- | :--- | :--- |
| **0:00 – 0:45** | **Home Screen Overview**<br>• View streak, XP, and Green Points.<br>• Language switcher (VI, EN, ZH, JA, KO).<br>• Proximity filter ($< 1.5\text{ km}$ vs. Mock catalog). | *"Welcome to HẻmQuest. The home screen presents the user's real-time exploration streak, XP, and Green Points earned through active walking. The UI intelligently surfaces quests located within immediate walking distance while allowing multi-language selection."* |
| **0:45 – 1:15** | **Bách Khoa Hẻm (Encyclopedia)**<br>• Open **📚 Bách Khoa** from stats header.<br>• Browse architectural & culinary lore. | *"Before stepping out, users can consult Bách Khoa Hẻm—an offline digital encyclopedia cataloging hidden heritage stories, traditional crafting methods, and historical timelines."* |
| **1:15 – 2:00** | **Voice Prompting & Quest Activation**<br>• Trigger voice or tap *Phú Bình Lantern Alley*.<br>• View route, distance, checkpoints, and calories. | *"Users can customize their journey via voice input or select a themed route like the Phú Bình Lantern Guild. The app displays checkpoint waypoints, walking duration, and estimated carbon offset."* |
| **2:00 – 2:50** | **Camera & Gemini AI Vision Verification**<br>• Arrive at checkpoint & launch camera.<br>• Submit photo for Gemini Multimodal verification. | *"At each checkpoint, users encounter a cultural task. When submitting their photo, Gemini Multimodal Vision inspects the photo in real-time, verifying that the user found the authentic artifact before awarding XP and Green Points."* |
| **2:50 – 3:30** | **Hẻm Passport & Firebase Firestore Sync**<br>• Open **Badges & Profile Passport**.<br>• Review photo collection and synced stats. | *"Upon completion, the stamped milestone and photo are immortalized in the user's Digital Passport and synchronized in real-time to Firebase Firestore."* |
| **3:30 – 3:45** | **Closing Summary** | *"HẻmQuest bridges physical urban exercise, AI intelligence, and cultural preservation into every step. Thank you!"* |

---

## 🛠️ Getting Started & Setup

### Prerequisites
- Android Studio Ladybug | 2024.2+ or Google AI Studio Android runtime
- Android SDK API 34+ (Min SDK 26)
- JDK 17+
- A configured Google AI Studio Gemini API key or Firebase project credentials

### Installation & Run
```bash
# Clone the repository
git clone https://github.com/your-username/hemquest.git
cd hemquest

# Open in Android Studio or compile via Gradle
./gradlew assembleDebug
```

---

## 🔐 Privacy & Security Best Practices
- **API Keys & Secrets:** Kept out of version control and injected via `BuildConfig` / Secrets Gradle Plugin.
- **Firestore Security Rules:** Restricted to authenticated owner accounts (`request.auth.uid == userId`).
- **Location & Camera:** Strictly requested on-demand during active navigation and photo challenges in compliance with Android privacy policies.

---

## 🌏 Mission

HẻmQuest doesn't need to build artificial attractions. They already thrive inside our neighborhood alleys, artisan workshops, and morning coffee tables. We build the digital and AI layer to help everyone discover and cherish them.

**Explore Culture · Walk Greener · Support Local · Keep Stories Alive.**
