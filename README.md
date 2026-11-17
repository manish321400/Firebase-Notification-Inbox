# Notification Inbox

A native Android application built with **Kotlin** and **Jetpack Compose** that integrates **Firebase Cloud Messaging (FCM)** to receive, display, and locally persist push notifications in an interactive Notification Inbox.

---

## Project Overview

**Notification Inbox** provides a seamless push notification experience with local history management and offline persistence:

- **Push Notification Integration**: Receives remote messages via Firebase Cloud Messaging (FCM) containing rich data payloads (unique ID, title, description, banner image URL, and timestamp).
- **System Notifications**: Posts system notifications on a dedicated high-priority channel (`assignment` / `Assignment`) with application icon, expandable text, and banner images (`BigPictureStyle`).
- **Interactive Inbox**:
  - Displays a chronological list of notifications with banner previews, titles, descriptions, relative timestamps, and unread indicators.
  - Supports filter chips (**All**, **Unread**, **Read**) with live unread badge counters.
  - Supports sort toggling (**Newest First** / **Oldest First**).
  - Swipe-to-delete with animated dismissal and a confirmation dialog for clearing the entire inbox.
- **Notification Details**:
  - Tapping any notification opens a dedicated detail screen showing full-resolution banner graphics, title, formatted date/time, and complete description.
  - Opening a notification automatically transitions its state to **Read**, which immediately reflects across the inbox.
- **Deep Navigation Flow**:
  - Tapping a system tray notification launches the app directly into the **Notification Inbox**.
  - Navigating to details maintains standard back-stack behavior.
- **Duplicate Prevention & Resiliency**:
  - Guarantees that notifications sharing the same `id` never produce duplicate records in the inbox.
  - Gracefully handles missing, corrupted, or unreachable banner URLs without blocking text content or causing crashes.
  - Includes a built-in notification test simulator for offline verification.

---

## Setup and Build Instructions

### Prerequisites
- **Android Studio**: Ladybug / Jellyfish / Hedgehog (2023.1.1+) or newer
- **JDK**: Java 17 or Java 21
- **Android SDK**: Compile SDK 34, Min SDK 26 (Android 8.0 Oreo), Target SDK 34 (Android 14)

### Clone and Open
```bash
git clone git@github.com:manish321400/Firebase-Notification-Inbox.git
cd Firebase-Notification-Inbox
```
Open the project root directory in Android Studio and allow Gradle to complete dependency synchronization.

### Build via Command Line
To build the debug APK using the included Gradle wrapper:

```bash
# On Linux / macOS
./gradlew assembleDebug

# On Windows (PowerShell / Command Prompt)
.\gradlew.bat assembleDebug
```

The compiled APK will be generated at:
```text
app/build/outputs/apk/debug/app-debug.apk
```
A pre-built, tested APK is also conveniently available in the repository at [`apk/app-debug.apk`](apk/app-debug.apk).

---

## Firebase Setup and Configuration

To connect this application to your own Firebase project:

1. **Create/Open Project in Firebase Console**:
   - Navigate to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
2. **Register Android Application**:
   - Register an Android app with the package name:
     ```text
     com.assignment.notificationinbox
     ```
3. **Add `google-services.json`**:
   - Download the generated `google-services.json` from the Firebase Console.
   - Place the file inside the `app/` directory of the project:
     ```text
     app/google-services.json
     ```
   *(Note: The project includes a template configuration to allow immediate local compilation. Replace it with your genuine Firebase file for live messaging).*

### Notification Data Payload Format
When sending custom push messages via FCM REST API or Firebase Console, use the following payload structure:

```json
{
  "id": "notification_101",
  "title": "New Notification",
  "description": "This is a sample notification description.",
  "bannerImg": "https://picsum.photos/600/300",
  "timestamp": "1716384000000"
}
```

| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | String | Unique identifier for the notification (Primary Key). |
| `title` | String | Notification headline (optional). |
| `description` | String | Notification body text (optional). |
| `bannerImg` | String | HTTP/HTTPS image URL for banner display (optional). |
| `timestamp` | String / Long | Epoch timestamp in milliseconds (optional). |

### In-App Simulator (Instant Testing)
For quick testing without sending messages through the Firebase Console, tap the floating **"Test Notification"** button on the inbox screen. This enables testing:
- Standard notifications with images
- Duplicate ID handling (`notification_101`)
- Broken or unreachable image URLs
- Text-only notifications (null banner)
- Minimal payloads (missing optional fields)

---

## Libraries and Technologies Used

- **Kotlin**: Core language (v2.0.0)
- **Jetpack Compose & Material 3**: Declarative UI toolkit with Material 3 design tokens
- **Room Database**: Local SQLite abstraction layer for offline data persistence
- **Hilt (Dagger)**: Dependency injection for architecture decoupling
- **Navigation Compose**: Type-safe navigation and back-stack handling
- **Firebase Cloud Messaging (FCM)**: Push notification reception and system tray delivery
- **Firebase Analytics**: App analytics via Firebase BoM
- **Coil Compose**: Asynchronous image loading with image caching, transformation, and error placeholders
- **Kotlin Coroutines & Flow**: Asynchronous state management and reactive database queries

---

## Assumptions Made During Implementation

1. **Duplicate Notification Strategy**: When a notification is received with an `id` that already exists in the database, the system updates its content (title, description, banner) while strictly **preserving the user's current read/unread status** (`isRead`). A notification that the user has already read will not be reset to unread simply because an update payload arrived.
2. **Missing Timestamps**: If the `timestamp` field is absent or malformed in the incoming payload, the app defaults to the current system time (`System.currentTimeMillis()`) to ensure chronological consistency.
3. **Missing ID**: If a payload completely lacks an `id`, an ID is automatically derived from the FCM message ID or timestamp to prevent dropped notifications.
4. **Navigation Flow**: Following standard system notification conventions, tapping a tray notification lands the user directly on the main **Notification Inbox**. From there, tapping any specific card opens the **Notification Details** screen.
5. **Target Compatibility**: Configured for `minSdk = 26` (Android 8.0 Oreo), covering >95% of active Android devices and supporting native notification channels without legacy support workarounds.

---

## Known Limitations

1. **Offline Image Display**: If the device is offline when a notification with a banner image is first viewed, a fallback placeholder icon is shown until network connectivity is restored and Coil can fetch and cache the graphic.
2. **Device Battery Optimization**: Certain Android OEM devices (e.g., Xiaomi MIUI, Huawei EMUI, Samsung OneUI) apply aggressive background process termination. On such devices, high-priority FCM data messages or autostart permissions must be enabled in device settings for background delivery when the app is swiped away from Recents.
