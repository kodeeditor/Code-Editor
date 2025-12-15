# Android Code Editor Application

## Overview
This is a mobile code editor application for Android 9 (API 28) and Android 10 (API 29). The app provides a full-featured code editing experience with syntax highlighting, GitHub integration, terminal emulator, and APK build capabilities.

## Recent Changes (December 2025)

### UI/UX Improvements
- Updated `colors.xml` with modern GitHub-style dark theme palette
- Updated `themes.xml` with new button, card, and input styles
- Updated `activity_main.xml` with cleaner layout, better spacing, and visual hierarchy
- More professional appearance with consistent styling

### Build System Updates
- Updated `build_debug.sh` with automatic Android SDK installation
- Creates `local.properties` automatically with SDK path
- GitHub Actions workflow now only builds Debug APK (removed release build)

### Error Handling Improvements
- Added `GlobalExceptionHandler` to catch all uncaught exceptions
- Created `ErrorActivity` as fallback screen for crash details
- Created `CodeEditorApplication` class for global exception handler
- Added `ErrorDialogHelper` utility for error dialogs

### SDK Target Changes
- minSdk: 28 (Android 9)
- targetSdk: 29 (Android 10)
- compileSdk: 34

## Project Architecture

### Main Components
- **MainActivity** - Main code editor with tabs, file tree, GitHub integration
- **TerminalActivity** - Terminal emulator (Termux libraries)
- **BuildActivity** - APK build interface
- **SettingsActivity** - App preferences

### Key Packages
- `com.codeeditor.android.adapter` - RecyclerView adapters
- `com.codeeditor.android.autocomplete` - Autocomplete engine
- `com.codeeditor.android.build` - APK build system
- `com.codeeditor.android.github` - GitHub API integration
- `com.codeeditor.android.syntax` - Syntax highlighting
- `com.codeeditor.android.theme` - Theme management
- `com.codeeditor.android.view` - Custom views

## Build Instructions

### Local Build (Replit)
```bash
./build_debug.sh
```
This script automatically:
1. Downloads Android SDK if not present
2. Creates local.properties
3. Builds debug APK

### GitHub Actions
Push to main/master triggers automatic debug APK build.
Download from Actions > Artifacts.

### Requirements
- JDK 17 (standard, not GraalVM - compatibility issue with Android Gradle Plugin)
  - Location: `~/jdk/jdk-17.0.9+9`
  - Auto-configured in `build_debug.sh`
- Android SDK 34
  - Location: `~/android-sdk`
  - Auto-installed by build script

### Output
- Debug APK: `output/app-debug.apk` (~20MB)
- Build time: ~4 minutes

## Terminal Feature

### Termux Bootstrap Integration (December 2025)
Terminal sekarang mendukung package manager (pkg/apt) melalui Termux bootstrap:
- Download otomatis bootstrap dari Termux GitHub releases (~50MB)
- Extract ke `files/usr/` dengan symlinks dan permissions yang benar
- Environment variables dikonfigurasi untuk Termux compatibility
- User dapat install packages: `pkg install python nodejs git vim`

### Files Baru
- `TermuxBootstrap.java` - Class untuk download, extract, dan setup bootstrap
- Updated `activity_terminal.xml` - Progress bar untuk instalasi

### Shell Priority
1. `{app_files}/usr/bin/bash` (Termux bootstrap - jika terinstall)
2. `{app_files}/usr/bin/sh`
3. `/system/bin/sh` (fallback)
4. `/system/bin/bash`
5. `/system/xbin/bash`

### Bootstrap URL
```
https://github.com/termux/termux-packages/releases/download/bootstrap-2025.12.14-r1+apt.android-7/bootstrap-aarch64.zip
```

## Known Issues
- GraalVM jlink not compatible with Android build - requires OpenJDK 17
- LSP errors in TerminalActivity.java (Termux library imports)
- Bootstrap hanya untuk aarch64 (ARM64) - perlu tambahkan support untuk ARM32, x86, x86_64
