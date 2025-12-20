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

## Custom Termux Bootstrap (NEW - December 2025)

### Problem
The official Termux bootstrap has hardcoded paths for `com.termux` package:
- `/data/data/com.termux/files/usr/...`

This causes permission errors when used with `com.codeeditor.android`:
```
-l: /data/data/com.termux/files/usr/etc/profile: Permission denied
termux:$ pkg
-l: /data/user/0/com.codeeditor.android/files/usr/bin/pkg: /data/data/com.termux/files/usr/bin/bash: bad interpreter: Permission denied
```

### Solution
A custom GitHub Actions workflow has been created to compile Termux bootstrap with the correct package name.

### How to Build Custom Bootstrap

1. **Push to GitHub** - Push this repository to your GitHub account

2. **Run the Workflow**
   - Go to Actions tab in your GitHub repository
   - Select "Build Custom Termux Bootstrap" workflow
   - Click "Run workflow"
   - Choose architectures (default: aarch64,arm)
   - Wait for build completion (may take 2-6 hours)

3. **Download Artifacts**
   - After build completes, download the bootstrap zip files from artifacts
   - Or create a GitHub Release with the bootstrap files

4. **Update BOOTSTRAP_URL**
   - Edit `TermuxBootstrap.java`
   - Replace `BOOTSTRAP_URL` with your GitHub Release URL
   - Update `BOOTSTRAP_VERSION` accordingly

### Workflow Details
- File: `.github/workflows/build-termux-bootstrap.yml`
- Clones termux-packages repository
- Patches `scripts/properties.sh` to use `com.codeeditor.android`
- Builds bootstrap using Docker
- Generates checksums for verification
- Uploads artifacts and optionally creates release

### Supported Architectures
- `aarch64` - ARM64 (most modern Android devices)
- `arm` - ARMv7 (older 32-bit devices)
- `i686` - x86 32-bit (emulators)
- `x86_64` - x86 64-bit (some tablets/Chromebooks)

## Package Recompilation and APT Server Upload (NEW - December 2025)

### Overview
A new GitHub Actions workflow has been created to automatically recompile all Termux packages with the custom `com.codeeditor.android` namespace and upload them to an APT server.

### Workflow File
- **File:** `.github/workflows/recompile-and-upload-apt.yml`
- **Name:** "Recompile and Upload Termux Packages"

### Features
- Recompile Termux packages for multiple architectures (aarch64, arm, i686, x86_64)
- Package filtering support (build specific packages only)
- Two upload modes:
  - GitHub Pages (deb [trusted=yes] repo URL)
  - External APT server (SSH/SFTP)
- Automatic package index generation
- Build reports and artifacts
- Weekly auto-run schedule (Sunday at 2 AM)

### Build Presets

The workflow supports 4 build presets for flexibility:

#### 1. **popular** (Default)
Builds most commonly used packages:
- **Languages:** Node.js, Python, PHP, Lua, Perl, Ruby, Golang, Rust, Clang
- **Tools:** Git, Curl, Wget, OpenSSH, Vim, Nano, Bash, Zsh, Fish
- **Development:** Make, CMake, GCC, G++, Cargo, Node-gyp
- **Utilities:** FFmpeg, ImageMagick, SQLite, Redis, Htop, Tmux, Screen
- **Estimated time:** 4-8 hours per architecture

#### 2. **all**
Builds every package in the Termux repository
- Complete package set (~200+ packages)
- Requires significant disk space and time
- **Estimated time:** 24+ hours per architecture

#### 3. **custom**
Builds packages matching a pattern (regex)
- Use `package_filter` with pattern (e.g., `.*python.*` for all Python packages)
- **Example:** Filter `node.*` to build Node.js and related packages

#### 4. **single**
Builds exactly one package by name
- Use `package_filter` with exact package name
- **Example:** `nodejs`, `python`, `php`, `git`, etc.

### Usage

#### Manual Trigger
1. Go to GitHub Actions tab
2. Select "Recompile and Upload Termux Packages" workflow
3. Click "Run workflow"
4. Configure:
   - **build_preset**: one of `popular`, `all`, `custom`, `single` (default: popular)
   - **architectures**: comma-separated list (aarch64,arm,i686,x86_64)
   - **package_filter**: for custom/single mode only (e.g., `nodejs`, `*python*`)
   - **upload_mode**: github-pages or external-apt

#### Examples

**Build popular packages for aarch64 (Default)**
- build_preset: `popular`
- architectures: `aarch64`
- upload_mode: `github-pages`

**Build only Node.js and related packages**
- build_preset: `custom`
- architectures: `aarch64`
- package_filter: `node.*`

**Build single package (PHP for multiple architectures)**
- build_preset: `single`
- architectures: `aarch64,arm`
- package_filter: `php`
- upload_mode: `github-pages`

**Build everything (all packages)**
- build_preset: `all`
- architectures: `aarch64` (⚠️ very long!)
- upload_mode: `github-pages`

### Repository Configuration

#### GitHub Pages (Default)
APT repository is automatically deployed to GitHub Pages:
```bash
deb [trusted=yes] https://{owner}.github.io/{repo-name}/apt stable main
```

#### External APT Server
Set these secrets in GitHub:
- `APT_REPO_URL` - Server URL/hostname
- `APT_REPO_USER` - SSH username
- `APT_REPO_PASSWORD` - Optional password
- `APT_REPO_SSH_KEY` - SSH private key (for key-based auth)
- `APT_REPO_PATH` - Path on server (/home/user/apt-repo)

### Available Packages
After running the workflow, users can:
```bash
apt update
apt install base-files
apt install [package-name]
```

### Build Artifacts
Each build generates:
- DEB packages for selected architecture
- Build report (markdown)
- Complete APT repository structure
- Available on Actions artifacts page (30-day retention)

## Known Issues
- GraalVM jlink not compatible with Android build - requires OpenJDK 17
- LSP errors in TerminalActivity.java (Termux library imports)
- Package compilation requires adequate disk space and build time (multi-hour builds possible)
