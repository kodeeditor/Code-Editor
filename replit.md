# Android Code Editor Application

## Overview
This is a mobile code editor application for Android 9 (API 28) and Android 10 (API 29). The app provides a full-featured code editing experience with syntax highlighting, GitHub integration, terminal emulator, and APK build capabilities.

## Recent Changes (December 2025)

### Error Handling Improvements
- Added `GlobalExceptionHandler` to catch all uncaught exceptions and display error dialogs instead of force closing
- Created `ErrorActivity` as a fallback screen to show crash details with options to restart or copy error logs
- Created `CodeEditorApplication` class to initialize the global exception handler
- Added `ErrorDialogHelper` utility class for showing error dialogs throughout the app
- Added try-catch blocks to onCreate methods in MainActivity, TerminalActivity, and BuildActivity
- All errors now display in dialogs with "View Details" option to see full stack trace

### SDK Target Changes
- minSdk: 28 (Android 9)
- targetSdk: 29 (Android 10)
- compileSdk: 34 (for compatibility with dependencies)

### Key Files Added
- `app/src/main/java/com/codeeditor/android/GlobalExceptionHandler.java` - Global crash handler
- `app/src/main/java/com/codeeditor/android/ErrorActivity.java` - Error display activity
- `app/src/main/java/com/codeeditor/android/CodeEditorApplication.java` - Application class
- `app/src/main/java/com/codeeditor/android/utils/ErrorDialogHelper.java` - Error dialog utilities

## Project Architecture

### Main Components
- **MainActivity** - Main code editor interface with tabs, file tree, and GitHub integration
- **TerminalActivity** - Terminal emulator using Termux libraries
- **BuildActivity** - APK build interface
- **SettingsActivity** - App preferences

### Features
- Syntax highlighting for multiple languages (Java, Kotlin, Python, JavaScript, etc.)
- GitHub integration for repository browsing and editing
- Code autocomplete
- Find and replace
- Snippets support
- Theme customization
- Terminal emulator
- Android APK build support

### Key Packages
- `com.codeeditor.android.adapter` - RecyclerView adapters
- `com.codeeditor.android.autocomplete` - Autocomplete engine
- `com.codeeditor.android.build` - APK build system
- `com.codeeditor.android.github` - GitHub API integration
- `com.codeeditor.android.model` - Data models
- `com.codeeditor.android.syntax` - Syntax highlighting
- `com.codeeditor.android.theme` - Theme management
- `com.codeeditor.android.utils` - Utility classes
- `com.codeeditor.android.view` - Custom views

## Build Instructions
This is an Android Gradle project. Build commands:
- Debug APK: `./gradlew assembleDebug`
- Release APK: `./gradlew assembleRelease`

## Crash Logs
Crash logs are saved to: `{app_files_dir}/crash_logs/crash_{timestamp}.log`
Maximum 10 log files are kept, older ones are automatically deleted.
