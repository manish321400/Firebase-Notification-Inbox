# Firebase Notification Inbox (Android)

A modern Native Android application built with **Kotlin** and **Jetpack Compose** that integrates **Firebase Cloud Messaging (FCM)** and provides a persistent, locally maintained **Notification Inbox**.

---

## 📱 Features

### Core Requirements
- **Firebase Cloud Messaging (FCM)**: Receives push notifications and parses custom JSON data payloads.
- **System Notifications**: Displays incoming notifications using the official Android Notification system with:
  - **Notification Channel ID**: `assignment`
  - **Notification Channel Name**: `Assignment`
  - High importance with sound, vibration, and application icon.
- **Local Persistence (Room Database)**:
  - Stores all received notifications locally using Room SQLite database.
  - History and read/unread states persist across application restarts.
- **Notification Inbox**:
  - Displays list of received notifications showing:
    - Banner image (with loading state and graceful fallback)
    - Title
    - Description
    - Formatted relative timestamp ("5 min ago", "2 hours ago", etc.)
    - Read/Unread state indicator (blue dot and tinted background)
- **Notification Details**:
  - Selecting an item opens the Notification Details screen showing banner, title, description, and full date/time.
  - Opening a notification **automatically marks it as Read**, instantly updated in the Inbox.
- **System Notification Navigation Flow**:
  - Tapping a received system notification directly opens the **Notification Inbox** (`System Notification → Notification Inbox`).
- **Duplicate Notification Handling**:
  - Notifications with duplicate `id` values do not create duplicate database entries.
  - Content is updated while preserving the user's existing read/unread state.
- **Error & Edge-Case Handling**:
  - **Missing or broken banner images**: Displays a graceful fallback without crashing or blocking notification content.
  - **Missing optional data**: Safe defaults for null/empty titles, descriptions, and banner images.
  - **Duplicate notification IDs**: Idempotent insert preventing duplicates.
  - **Application restart**: State fully persisted via Room database.

### 🌟 Enhancements
- **Unread Notification Badge**: Live unread count dynamically rendered on the top app bar.
- **Filter Chips**: Filter by **All**, **Unread**, or **Read**.
- **Sort Toggle**: Switch between **Newest First** and **Oldest First**.
- **Swipe-to-Delete**: Swipe any notification to the left to delete it with smooth exit animation.
- **Mark All as Read**: One-tap action in the top bar to mark all notifications as read.
- **Clear Inbox**: Delete all notifications with a safety confirmation dialog.
- **Empty State UI**: Informative empty states tailored to each filter.
- **In-App Notification Simulator**: Built-in test panel allowing evaluators to simulate all 5 notification scenarios (standard, duplicate ID, broken banner, no image, minimal data) directly on device without requiring a Firebase Console session.

---

## 🛠️ Architecture & Tech Stack

This project follows the **MVVM (Model-View-ViewModel)** architectural pattern adhering to official Android Architecture Guidelines:

- **Language**: Kotlin 2.0
- **UI Framework**: Jetpack Compose with Material 3 design system
- **Dependency Injection**: Hilt (Dagger Hilt 2.51.1)
- **Local Database**: Room 2.6.1 with Coroutines & Kotlin Flow
- **Image Loading**: Coil 2.6.0 (`SubcomposeAsyncImage` with crossfade and error states)
- **Navigation**: Jetpack Navigation Compose 2.7.7
- **Push Notifications**: Firebase Cloud Messaging (FCM) via Firebase BoM 33.1.0
- **Asynchronous Flow**: Kotlin Coroutines & StateFlow

---

## 📥 Notification Payload Format

The application expects FCM notifications with the following data payload format:

```json
{
  "id": "notification_101",
  "title": "New Notification",
  "description": "This is a sample notification for the assignment.",
  "bannerImg": "https://example.com/banner.jpg",
  "timestamp": "1791873000000"
}
```

- `id` (String): Unique identifier for each notification (primary key).
- `title` (String, optional): Title of the notification.
- `description` (String, optional): Notification body message.
- `bannerImg` (String, optional): URL to a banner image.
- `timestamp` (String / Long, optional): Epoch timestamp in milliseconds.

---

## 🚀 Setup & Build Instructions

### Prerequisites
- **Android Studio** (Hedgehog 2023.1.1 or Ladybug / Jellyfish / newer)
- **JDK 17**
- **Android SDK**: Min SDK 26 (Android 8.0), Target SDK 34 (Android 14)

### Clone the Repository
```bash
git clone git@github.com:manish321400/Firebase-Notification-Inbox.git
cd Firebase-Notification-Inbox
```

### Firebase Configuration
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Create a project and add an Android application with package name:
   ```
   com.assignment.notificationinbox
   ```
3. Download the `google-services.json` file.
4. Place `google-services.json` into the `app/` folder of this project:
   ```
   app/google-services.json
   ```
*(Note: Per security guidelines, credentials and private keys are excluded from git repository via `.gitignore`.)*

### Build the Project
Open the project in Android Studio, let Gradle sync, then run:

```bash
# Build Debug APK
./gradlew assembleDebug

# Output APK path:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 🧪 Testing Push Notifications

### Method 1: Using the In-App Test Simulator (Instant Verification)
1. Launch the app on an Android device or emulator.
2. Tap the floating **"Test Notification"** button at the bottom right.
3. Choose any scenario to evaluate:
   - **Standard Notification (with Image)**: Verifies image loading, title, description, timestamp, and unread state.
   - **Duplicate ID ('notification_101')**: Simulates multiple pushes with the same ID to verify that no duplicates are created in the inbox.
   - **Invalid/Broken Banner URL**: Demonstrates graceful fallback without crashing.
   - **No Banner Image**: Shows text-only layout adaptation.
   - **Missing Optional Data**: Minimal payload handling.

### Method 2: Using Firebase Console
1. In Firebase Console, go to **Engage** → **Messaging**.
2. Click **Create your first campaign** → **Firebase Notification messages**.
3. Under **Notification**, enter a title and text.
4. Under **Additional options**, add the custom data key-value pairs:
   - `id`: `notification_102`
   - `title`: `Summer Promo`
   - `description`: `Check out the latest updates inside the app.`
   - `bannerImg`: `https://picsum.photos/600/300`
   - `timestamp`: `1716384000000`
5. Send to the app target (`com.assignment.notificationinbox`).

---

## 📋 Assumptions Made During Implementation

1. **Duplicate Notification Behavior**: When a notification arrives with an ID that already exists, the repository updates the title, description, and banner image to reflect the latest content, but **preserves the user's existing read/unread status** so that a previously read notification is not erroneously reset to unread.
2. **Missing Timestamps**: If the payload timestamp is absent or cannot be parsed, the system gracefully defaults to the current epoch time (`System.currentTimeMillis()`).
3. **Missing ID**: If a payload completely lacks an `id` field, a unique ID is derived from the FCM message ID or timestamp to prevent dropped notifications.
4. **Target Devices**: Min SDK is set to 26 (Android 8.0 Oreo), covering 95%+ of active Android devices and supporting native notification channels.

---

## ⚠️ Known Limitations

1. **Network Connectivity**: In offline mode, banner images that are not yet cached by Coil will display the broken-image fallback icon until connectivity is restored.
2. **Firebase Setup**: Receiving live pushes from Firebase Console requires dropping a valid `google-services.json` into the `app/` directory (or using the integrated in-app notification simulator).

---

## 📄 License
This project is developed as an assignment submission.
