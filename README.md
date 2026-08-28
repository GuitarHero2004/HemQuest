# 🏮 HẻmQuest

> **Every alley has a story. Turn every step into a quest.**

**HẻmQuest** is a gamified urban exploration and cultural heritage Android app that transforms overlooked neighbourhoods, historic alleys (*hẻm*), craft communities, and local stories into interactive walking experiences.

By combining **Google Gemini Multimodal AI, location-aware exploration, gamification, cultural storytelling, and active mobility**, HẻmQuest helps users discover local heritage, explore cities on foot, and preserve living urban stories.

---

## 🌆 The Idea & Motivation

Ho Chi Minh City is famous for major destinations such as Nguyễn Huệ Walking Street and Bến Thành Market. But much of the city's everyday cultural identity lives deeper inside its **hẻm** — dense networks of alleys, residential enclaves, workshops, family businesses, and community spaces.

These neighbourhoods can contain:

- Traditional craft communities and workshops
- Multi-generational family-run eateries and local coffee culture
- Historic architecture and communal religious spaces
- Neighbourhood stories and micro-heritage
- Places that are culturally meaningful but difficult to discover through conventional tourism apps

For younger residents and international travellers, this information is often fragmented and lacks the context needed for meaningful exploration.

**HẻmQuest turns the city into an interactive, playable cultural exploration map.**

---

## 🎯 Problem Statement

HẻmQuest is designed around four connected challenges:

1. **Concentrated tourism:** Visitors tend to gather around already-famous attractions, while smaller neighbourhoods and local communities receive less visibility.
2. **Fragmented cultural knowledge:** Local history, architecture, craft traditions, and community stories are often scattered across different sources or remain offline.
3. **Passive urban discovery:** Conventional mapping tools are excellent for *Search → Navigate → Arrive*, but they rarely encourage deeper interaction with the place itself.
4. **Low motivation for active exploration:** Walking can be healthy and sustainable, but many users lack a reason to turn an ordinary walk into an engaging experience.

---

## 💡 The Solution: Gamified Urban Walking Quests

Instead of passive point-to-point navigation, HẻmQuest creates an exploration loop:

```text
Discover
   ↓
Choose a Quest
   ↓
Walk to Checkpoints
   ↓
Learn the Story
   ↓
Complete a Challenge
   ↓
Verify with AI
   ↓
Earn XP & Green Points
   ↓
Collect the Memory
```

Each Quest guides users through a set of culturally themed checkpoints with:

- 📍 **Location-aware exploration**
- 🗺️ **Quest routes and checkpoint progression**
- 📷 **Gemini Multimodal photo verification**
- 📚 **Bách Khoa Hẻm cultural knowledge**
- 🌱 **Green Points and XP**
- 🏅 **Badges and progression**
- 🛂 **Digital Explorer Passport**
- ☁️ **Firebase-backed account and cloud data**

---

## ✅ Current Product Capabilities

The current prototype focuses on the core end-to-end exploration experience.

### 1. 🧭 Quest Discovery

Users can browse urban quests by category, including themes such as:

- 🍜 *Ẩm thực hẻm* — local food
- 🏛️ *Kiến trúc xưa* — heritage architecture
- 📜 *Di tích lịch sử* — historical sites
- 🏮 *Làng nghề thủ công* — traditional crafts
- 🌿 *Hẻm xanh* — green urban exploration

Quest cards can surface information such as:

- Theme
- Estimated distance
- Estimated duration
- Number of checkpoints
- Reward value
- Quest progress

The app can use location context to make nearby exploration more relevant to the user.

---

### 2. 🗺️ Quest Journey & Checkpoints

When a user starts a Quest, HẻmQuest presents the journey as a sequence of real-world checkpoints.

A checkpoint can contain:

- Location information
- Cultural context
- A short challenge
- Progress state
- Photo verification action

This transforms navigation from simply reaching a destination into completing a structured cultural experience.

---

### 3. 📷 Gemini Multimodal AI Verification

A core technical feature of HẻmQuest is **AI-assisted checkpoint verification**.

Instead of allowing users to complete a checkpoint with a simple button press, the app can ask for a context-specific photograph.

Example challenge:

> **Find and photograph a traditional handmade lantern.**

The submitted image is analysed with **Google Gemini Multimodal AI** to evaluate whether it matches the checkpoint requirement.

The verification flow is designed to:

- Analyse the visual content of the photo
- Compare it with the checkpoint challenge
- Return an immediate verification result
- Unlock Quest progress when the submission is accepted
- Award XP / Green Points after successful completion

> **Note:** AI verification supports the experience but should not be treated as proof of historical authenticity or absolute physical presence on its own.

---

### 4. 📚 Bách Khoa Hẻm — Saigon Alley Encyclopedia

**Bách Khoa Hẻm** is the cultural knowledge layer of the app.

It is designed to make local stories easier to discover before or during a Quest.

Entries may include:

- Heritage locations
- Local terminology
- Architectural styles
- Traditional crafts
- Historical context
- Local gastronomy
- Community stories

The long-term purpose of Bách Khoa Hẻm is to become a **living digital archive of neighbourhood culture**, while the current prototype demonstrates how cultural content can be integrated directly into exploration.

---

### 5. 🛂 Digital Explorer Passport

Completed exploration activities contribute to the user's personal **Hẻm Passport**.

The Passport can preserve:

- Completed Quests
- Checkpoint memories
- Quest completion history
- Earned badges
- XP and Green Point progression
- Captured Quest photos where supported

> **A passport for places that normally do not give you stamps.**

---

### 6. 🏅 Gamification

HẻmQuest uses lightweight gamification to make continued exploration more rewarding.

Current progression concepts include:

- **XP** — overall exploration progress
- **Green Points** — reward points associated with active exploration
- **Badges** — milestones and themed achievements
- **Streaks** — continued engagement over time

The current prototype treats these primarily as **in-app progression mechanics**.

---

## 🧠 AI in HẻmQuest

Google Gemini is used as the intelligent layer of the experience.

### Current Focus

- Multimodal image understanding
- Checkpoint photo verification
- AI-assisted Quest content / challenge generation where enabled
- Contextual responses around Quest objectives

### AI Design Principle

HẻmQuest should use AI to **enhance curated cultural information**, not replace reliable source material.

Historical or cultural facts should be grounded in reviewed data wherever possible rather than generated without supporting sources.

---

## 🏗️ Architecture & Tech Stack

```text
┌─────────────────────────────────────────────────────────────┐
│                     HẺMQUEST ANDROID APP                    │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │            Jetpack Compose + Material 3               │  │
│  │                                                       │  │
│  │ Home • Quest • Bách Khoa • Badges • Profile/Passport  │  │
│  └──────────────────────────┬────────────────────────────┘  │
│                             │                               │
│                  StateFlow / Coroutines                     │
│                             │                               │
│  ┌──────────────────────────▼────────────────────────────┐  │
│  │               MVVM Presentation Layer                 │  │
│  │                                                       │  │
│  │ QuestViewModel • UserStatsViewModel • AuthViewModel   │  │
│  └──────────────────────────┬────────────────────────────┘  │
│                             │                               │
│  ┌──────────────────────────▼────────────────────────────┐  │
│  │              Repositories / Data Layer                │  │
│  └───────────────┬───────────────────────┬────────────── ┘  │
│                  │                       │                  │
│          ┌───────▼────────┐      ┌──────▼───────────┐       │
│          │ Local Storage  │      │ External Services│       │
│          │                │      │                  │       │
│          │ Room Database  │      │ Gemini API       │       │
│          │ Local progress │      │ Firebase         │       │
│          └────────────────┘      │ Google Sign-In   │       │
│                                  └──────────────────┘       │
└─────────────────────────────────────────────────────────────┘
```

### Android

- **Kotlin**
- **Jetpack Compose**
- **Material Design 3**
- Edge-to-edge layouts
- Lifecycle-aware state management

### Architecture

- **MVVM**
- Clean separation between UI, application logic, and data sources
- **Kotlin Coroutines**
- **Flow / StateFlow**
- Repository-based data access

### Local Persistence

- **Room Database**
- Offline-first storage for Quest and progression data where supported

### Cloud & Identity

- **Firebase Cloud Firestore**
- **Google Credential Manager / Google Sign-In**

### Artificial Intelligence

- **Google Gemini Multimodal API**
- Used primarily for AI-assisted Quest interaction and image analysis

---

## 💾 Offline-First Approach

Urban exploration should not completely depend on stable connectivity.

HẻmQuest therefore uses local persistence for data that can remain available on-device, such as:

- Quest information
- Checkpoints
- Cultural content
- User progress
- Cached application state

Cloud-connected functionality can synchronise when connectivity is available.

---

## ☁️ Firebase Integration

Firebase supports the cloud-connected parts of HẻmQuest.

Depending on the active build and configuration, Firestore can be used for:

- User profile data
- Quest progress
- Achievements
- Passport data
- Cross-device persistence

Google sign-in provides account-based access without requiring a separate username/password system.

---

## 🔄 Example User Journey

```text
Open HẻmQuest
      ↓
Browse Quest Categories
      ↓
Select a Quest
      ↓
Read Cultural Context
      ↓
Start the Journey
      ↓
Reach a Checkpoint
      ↓
Read the Challenge
      ↓
Capture a Photo
      ↓
Gemini AI Verification
      ↓
Complete Checkpoint
      ↓
Earn XP / Green Points
      ↓
Update Passport & Progress
```

### Example

**Phú Bình Lantern Quest**  
*Traditional Crafts · Walking Exploration*

**Challenge**

> Find and photograph a traditional handmade lantern or a characteristic detail of its construction.

**Verification**

Gemini evaluates whether the image matches the visual objective of the checkpoint.

**Reward**

```text
+ XP
+ Green Points
+ Quest Progress
```

---

## 🗺️ Exploration Themes

HẻmQuest is designed around culturally distinctive urban experiences in Ho Chi Minh City.

Potential and prototype Quest themes include:

### 🏮 Phú Bình

Traditional lantern-making and local craftsmanship.

### 🏘️ Hào Sĩ Phường

Historic Chợ Lớn alley architecture and Chinese-Vietnamese cultural heritage.

### 🌆 Cư xá Đô Thành

A distinctive residential neighbourhood offering a different perspective on central Saigon.

### 🏯 Chợ Lớn

Temples, food, architecture, markets, and Chinese-Vietnamese cultural heritage.

These locations demonstrate how HẻmQuest can transform different types of neighbourhood culture into structured walking experiences.

---

## 🌱 Social & Environmental Impact

HẻmQuest is designed around four broader impact areas.

### 🌿 Greener Exploration

Encourage users to discover nearby areas on foot instead of treating every urban journey as motorised transport.

### 🏮 Cultural Discovery & Preservation

Make neighbourhood histories, craft traditions, and cultural stories easier for younger users and travellers to encounter.

### 🏪 Local Economic Discovery

Create opportunities for users to discover:

- Family-run shops
- Independent cafés
- Traditional workshops
- Local markets
- Craftspeople
- Community businesses

### ❤️ Active Urban Lifestyle

Turn walking into a purposeful activity through challenges, progression, and exploration.

---

## 📊 Product Metrics We Want to Validate

As HẻmQuest moves from prototype to user testing, useful metrics include:

### Exploration

- Quest completion rate
- Walking distance
- Checkpoints completed
- Repeat Quest participation
- Neighbourhoods explored

### Cultural Engagement

- Bách Khoa entries viewed
- Cultural checkpoints completed
- Heritage locations explored
- Cultural content saved or revisited

### Product Experience

- AI verification success rate
- Quest abandonment points
- Average Quest duration
- User retention
- Most popular Quest categories

---

## 🚧 Current Scope vs. Future Ideas

To keep the repository clear about what the current prototype demonstrates, the following ideas are **not presented as completed core features**.

| Capability | Status |
|---|---|
| Quest browsing and categories | ✅ Current |
| Checkpoint-based Quest journey | ✅ Current |
| XP / Green Point progression | ✅ Current |
| Bách Khoa Hẻm | ✅ Current |
| Gemini multimodal photo verification | ✅ Current |
| Hẻm Passport / progression view | ✅ Current |
| Firebase-backed account/cloud data | ✅ Current / configuration-dependent |
| AI-assisted Quest generation | 🟡 Experimental |
| Full offline Quest experience | 🟡 In progress / build-dependent |
| Speech-to-Text Quest prompting | 🔵 Planned |
| Public global leaderboard | 🔵 Planned |
| Physical Green Point vouchers | 🔵 Planned |
| Partner reward marketplace | 🔵 Planned |
| Verified carbon-offset calculation | 🔵 Planned |
| Community-created Quest platform | 🔵 Planned |

---

## 🚀 Development Roadmap

### Phase 1 — Core Exploration ✅

- Quest browsing
- Categories and discovery
- Checkpoint journey
- Gamification
- Cultural content

### Phase 2 — AI Experience ✅ / 🟡

- Gemini multimodal image verification
- AI-assisted Quest interaction
- Improve verification reliability
- Improve cultural grounding

### Phase 3 — Persistence & Accounts ✅ / 🟡

- Local Room persistence
- Firebase user data
- Google authentication
- Cloud-connected progression

### Phase 4 — Product Validation

- Field-test selected HCMC Quest routes
- Evaluate usability
- Measure Quest completion
- Test AI verification in real environments
- Collect cultural-content feedback

### Phase 5 — Expansion

Potential future areas:

- Community-authored Quests
- Partner-created cultural trails
- Local merchant rewards
- More neighbourhoods
- More cities
- Richer multilingual support
- Optional voice-based Quest prompting

---

## 🛠️ Getting Started

### Prerequisites

- Android Studio
- Android SDK compatible with the project
- JDK 17+
- Required Firebase configuration for cloud-enabled builds
- A valid Gemini API configuration for AI-enabled features

### Clone the Repository

```bash
# Clone the repository
git clone https://github.com/your-username/hemquest.git
cd hemquest

# Build the project
./gradlew assembleDebug
```

Open the project in **Android Studio** and allow Gradle to synchronise dependencies.

You can also build from the command line:

```bash
./gradlew assembleDebug
```

---

## 🔑 Configuration

Some functionality depends on external Google services.

Depending on the build, you may need to configure:

- Gemini API credentials
- Firebase
- Google Sign-In
- Maps / location services

Do **not** commit private credentials to the repository.

Use the project's local secret-management approach for API keys and environment-specific configuration.

---

## 🔐 Privacy & Security

HẻmQuest may work with sensitive device capabilities such as location and camera access.

Development should follow these principles:

- Request location only when required for exploration features
- Request camera access only when needed for checkpoint verification
- Never commit API keys or private credentials
- Restrict user-specific Firestore data through appropriate security rules
- Avoid presenting AI-generated cultural information as verified fact without review

Sensitive values should remain outside version control.

---

## 🔭 Future Vision

HẻmQuest begins with Ho Chi Minh City, but the underlying concept is broader:

> **A playable cultural layer for neighbourhood exploration.**

Future versions could allow local historians, students, cultural organisations, tourism partners, and communities to create their own curated Quest experiences.

The same model could eventually support hidden-neighbourhood exploration across other Vietnamese and Southeast Asian cities.

For now, the priority is simpler:

**build a strong, believable, and enjoyable HẻmQuest experience in Ho Chi Minh City first.**

---

## 🌏 Mission

HẻmQuest does not need to create artificial attractions.

They already exist inside our neighbourhood alleys, workshops, markets, cafés, homes, and communities.

What is missing is a digital layer that helps people **discover, understand, and remember them**.

By combining **AI, gamification, active mobility, and cultural storytelling**, HẻmQuest aims to make every walk an opportunity to:

**Explore Culture · Walk Greener · Support Local · Keep Stories Alive.**

---

# 🏮 HẻmQuest

### Every alley has a story.

**Go find it.**
