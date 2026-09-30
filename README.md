#  NOVA Health Connect

> **A dedicated Android Health Connect synchronization app for the NOVA wellness ecosystem.**

NOVA Health Connect is the Android-side data synchronization layer of **NOVA**, designed to securely access health and fitness data through **Android Health Connect** and synchronize it with the NOVA backend.

It acts as a bridge between health applications available on a user's Android device and the NOVA wellness platform.

---

##  About NOVA

**NOVA** is an AI-powered wellness and focus platform that transforms health and activity data into personalized insights and recommendations.

NOVA Health Connect handles the Android-side health data pipeline:

```text
Health & Fitness Apps
        ↓
   Android Health Connect
        ↓
   NOVA Health Connect
        ↓
      NOVA API
        ↓
     Database
        ↓
    NOVA Platform
```

---

# ✨ Key Features

### 🔄 Health Data Synchronization

Reads supported health metrics from Android Health Connect and synchronizes them with the NOVA backend.

### 🔐 Health Connect Permissions

Uses Android Health Connect's permission system to request access to supported health records.

Users remain in control of which health data the application can access.

### 📊 Health Metrics

The synchronization architecture can work with health and activity information such as:

* 💤 Sleep
* 🚶 Steps
* ❤️ Heart rate
* 🏃 Physical activity
* 🔥 Calories
* ⚖️ Other supported Health Connect records

The exact records available depend on the permissions granted and the data provided by the user's connected health applications.

### ☁️ Backend Synchronization

Collected health information is sent to the NOVA backend, where it can be stored and used by the main NOVA application.

### 🔁 Background Synchronization

The application is designed to support background synchronization so health information can be periodically updated without requiring the user to manually trigger every sync.

### 🛡️ Permission-Based Access

NOVA Health Connect does not bypass Android's health-data permission system.

Health data is accessed only through the permissions granted by the user.

---

# 🏗️ Architecture

```text
┌──────────────────────────────┐
│       Health Apps            │
│                              │
│ Google Fit / Samsung Health  │
│ Other Compatible Apps        │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│     Android Health Connect   │
│                              │
│ Standardized Health Records  │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│    NOVA Health Connect App   │
│                              │
│ HealthConnectManager         │
│ HealthSyncManager            │
│ HealthSyncWorker             │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│        NOVA Backend          │
│                              │
│ REST APIs / Data Processing  │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│         PostgreSQL           │
│                              │
│ User Health Data             │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│        NOVA Platform         │
│                              │
│ Dashboard + AI Insights      │
└──────────────────────────────┘
```

---

# 🧩 How It Works

## 1. User Grants Permissions

The user installs NOVA Health Connect and grants the required Health Connect permissions.

```text
User
 ↓
Health Connect Permission Screen
 ↓
Grant Selected Permissions
```

---

## 2. Health Data Is Read

The application communicates with Android Health Connect and retrieves the permitted health records.

```text
HealthConnectManager
        ↓
Health Connect API
        ↓
Health Records
```

---

## 3. Data Is Processed

The retrieved records are converted into NOVA's internal data models.

This provides a consistent format before the data is sent to the backend.

```text
Health Connect Records
        ↓
NOVA Data Models
        ↓
Validation / Processing
```

---

## 4. Data Is Synchronized

Processed health information is sent to the NOVA backend.

```text
Android App
     ↓
NOVA API
     ↓
PostgreSQL
```

---

## 5. NOVA Uses the Data

The main NOVA platform can then use the synchronized information to generate:

* Wellness insights
* Activity summaries
* Sleep analysis
* Personalized recommendations
* Focus-related insights

---

# 🛠️ Tech Stack

| Technology         | Purpose                    |
| ------------------ | -------------------------- |
| **Kotlin**         | Android development        |
| **Android SDK**    | Application framework      |
| **Health Connect** | Health data access         |
| **WorkManager**    | Background synchronization |
| **REST API**       | Backend communication      |
| **PostgreSQL**     | Health data storage        |
| **GitHub**         | Version control            |

---

# 📁 Project Structure

```text
NOVA-Health-Connect/
│
├── app/
│   └── src/
│       └── main/
│           ├── java/
│           │   └── ...
│           │
│           ├── res/
│           │   └── ...
│           │
│           └── AndroidManifest.xml
│
├── build.gradle
├── settings.gradle
├── gradle.properties
└── README.md
```

### Important Components

```text
HealthConnectManager
        │
        ├── Health Connect permissions
        ├── Read health records
        └── Health data operations
                │
                ▼
HealthSyncManager
        │
        ├── Process records
        ├── Prepare sync payload
        └── Communicate with backend
                │
                ▼
HealthSyncWorker
        │
        └── Background synchronization
```

---

# ⚙️ Setup

## Prerequisites

Before running the application, make sure you have:

* Android Studio
* Android SDK
* Kotlin support
* An Android device/emulator supporting Health Connect
* Health Connect installed and configured
* A running NOVA backend

---

## 1. Clone the Repository

```bash
git clone <YOUR_HEALTH_CONNECT_REPOSITORY_URL>
```

```bash
cd <YOUR_HEALTH_CONNECT_REPOSITORY>
```

---

## 2. Open in Android Studio

Open the cloned project using Android Studio.

Allow Gradle to synchronize and install the required dependencies.

---

## 3. Configure Backend

Configure the backend/API endpoint used by the application.

For example:

```text
NOVA_API_URL=https://your-backend-url
```

> Do not commit private API keys, tokens, passwords, or other secrets to GitHub.

---

## 4. Build the Application

From Android Studio:

```text
Build → Make Project
```

or use:

```bash
./gradlew build
```

---

## 5. Run on an Android Device

Connect a compatible Android device and run the application from Android Studio.

After installation:

1. Open NOVA Health Connect
2. Grant the requested Health Connect permissions
3. Ensure health data exists in Health Connect
4. Start synchronization
5. Verify the synchronized data through the NOVA backend/dashboard

---

# 🔐 Health Connect Permissions

NOVA Health Connect uses Android Health Connect's permission model.

The application should request only the health records required by NOVA.

Typical permissions may include access to records such as:

```text
Steps
Sleep
Heart Rate
Active Calories Burned
Distance
Exercise Sessions
```

The actual permissions depend on the health metrics implemented in the application.

Users can manage Health Connect permissions through Android's Health Connect settings.

---

# 🔄 Synchronization Flow

```text
┌───────────────┐
│ Health Apps   │
└───────┬───────┘
        │
        ▼
┌──────────────────┐
│ Health Connect   │
└────────┬─────────┘
         │
         ▼
┌──────────────────────┐
│ HealthConnectManager │
└──────────┬───────────┘
           │
           ▼
┌────────────────────┐
│ HealthSyncManager  │
└─────────┬──────────┘
          │
          ▼
┌────────────────────┐
│ HealthSyncWorker   │
└─────────┬──────────┘
          │
          ▼
┌────────────────────┐
│    NOVA Backend    │
└─────────┬──────────┘
          │
          ▼
┌────────────────────┐
│    PostgreSQL      │
└────────────────────┘
```

---

# 🧠 Why a Separate Health Connect App?

NOVA Health Connect is maintained as a separate Android application so that the health-data integration layer remains independent from the main NOVA web application.

This provides:

* Clear separation of responsibilities
* Native Android Health Connect integration
* Easier maintenance
* Independent development and deployment
* Background synchronization capabilities
* A dedicated security boundary for health-data access

---

# 🔒 Privacy

Health information is sensitive data.

NOVA Health Connect follows a permission-based approach:

* Health Connect controls access to health records.
* The application requests only required permissions.
* Users can revoke permissions.
* Backend communication should use secure HTTPS connections.
* Sensitive credentials should be stored outside source control.
* Health data should not be logged unnecessarily.

NOVA Health Connect is intended to support wellness applications and **does not provide medical diagnosis or treatment**.

---

# 🚀 Future Improvements

Planned or potential improvements include:

* 🔄 More Health Connect record types
* ⚡ Improved synchronization reliability
* 📡 Better offline synchronization
* 🔁 Automatic retry for failed uploads
* 📊 Improved sync status monitoring
* 🔐 Enhanced authentication
* 🧹 Duplicate-data prevention
* 📈 Historical health-data synchronization
* ⚙️ More configurable background sync
* 🩺 Additional wearable/device integrations

---

# 🌌 NOVA Ecosystem

NOVA Health Connect is one component of the larger NOVA ecosystem.

```text
                 ┌─────────────────────┐
                 │  Health / Wearable  │
                 │       Apps          │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │  NOVA Health        │
                 │     Connect         │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │    NOVA Backend     │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │    PostgreSQL       │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │    NOVA Web App     │
                 │                     │
                 │ AI + Insights +     │
                 │ Recommendations     │
                 └─────────────────────┘
```

---

# 👨‍💻 Team

Built by **Team NOVA**:

| Member             |
| ------------------ |
| **Daksh Upadhyay** |
| **Divyansh Gupta** |
| **Dipanshu Shah**  |
| **Ayush Agrawal**  |

---

# 🔗 NOVA

### Main NOVA Platform

https://nova-omega-sable.vercel.app/

### Main NOVA Repository

https://github.com/NOVA-Wellness/Nova

---

## ⭐ Contributing

This project is currently developed as part of the NOVA ecosystem.

For major changes, discuss the proposed architecture or implementation with the team before submitting changes.

---

## 📜 License

Add your project's chosen license here.

For example:

```text
MIT License
```

---

# 🌌 NOVA Health Connect

**Connecting your health data to a more personalized wellness experience.**
