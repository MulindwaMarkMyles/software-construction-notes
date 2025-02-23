# How to Run the Notes App

## Prerequisites

1. Make sure you have Android Studio installed
2. Install the Android SDK (minimum SDK version 24)
3. Have Java Development Kit (JDK) installed

## Setup Steps

1. Clone the repository
2. Open the project in Android Studio
3. Wait for Gradle sync to complete
4. Configure your local.properties file with your Android SDK path:
   ```
   sdk.dir=C:\\Users\\<YourUsername>\\AppData\\Local\\Android\\Sdk
   ```

## Running the App

1. Connect an Android device via USB or start an Android Emulator
2. Click the 'Run' button (green play icon) in Android Studio
3. Select your target device
4. Wait for the app to build and install

## Troubleshooting

- If Gradle sync fails, try:
  - File > Invalidate Caches / Restart
  - Update Gradle version in build.gradle
- If SDK path is not found, ensure local.properties has the correct path
