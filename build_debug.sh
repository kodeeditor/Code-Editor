#!/bin/bash

echo "=========================================="
echo "  Building Debug APK"
echo "=========================================="

ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
export ANDROID_HOME
export ANDROID_SDK_ROOT="$ANDROID_HOME"

install_android_sdk() {
    if [ -f "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" ]; then
        echo "[SDK] Android SDK already installed"
        return 0
    fi
    
    echo "[SDK] Installing Android SDK..."
    
    mkdir -p "$ANDROID_HOME/cmdline-tools"
    
    echo "[SDK] Downloading command line tools..."
    wget -q --show-progress https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/cmdline-tools.zip
    
    if [ $? -ne 0 ]; then
        echo "[SDK] Failed to download SDK tools"
        return 1
    fi
    
    echo "[SDK] Extracting..."
    unzip -q -o /tmp/cmdline-tools.zip -d "$ANDROID_HOME/cmdline-tools"
    mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
    rm /tmp/cmdline-tools.zip
    
    echo "[SDK] Accepting licenses..."
    yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --licenses --sdk_root="$ANDROID_HOME" > /dev/null 2>&1
    
    echo "[SDK] Installing platform-tools and build-tools..."
    "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" \
        "platform-tools" \
        "platforms;android-34" \
        "build-tools;34.0.0" \
        --sdk_root="$ANDROID_HOME"
    
    if [ $? -eq 0 ]; then
        echo "[SDK] Android SDK installed successfully!"
        return 0
    else
        echo "[SDK] Failed to install SDK packages"
        return 1
    fi
}

install_android_sdk
if [ $? -ne 0 ]; then
    echo "Failed to setup Android SDK"
    exit 1
fi

export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools/34.0.0:$PATH"

echo "[1/3] Cleaning previous build..."
./gradlew clean 2>/dev/null || true

echo "[2/3] Assembling Debug APK..."
./gradlew assembleDebug --stacktrace

if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "  Build Successful!"
    echo "=========================================="
    
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    
    if [ -f "$APK_PATH" ]; then
        APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
        echo "APK Location: $APK_PATH"
        echo "APK Size: $APK_SIZE"
        
        mkdir -p output
        cp "$APK_PATH" "output/app-debug-$(date +%Y%m%d_%H%M%S).apk"
        echo "Copied to: output/"
    else
        echo "Warning: APK file not found at expected location"
        find . -name "*.apk" -type f 2>/dev/null | head -5
    fi
else
    echo ""
    echo "=========================================="
    echo "  Build Failed!"
    echo "=========================================="
    exit 1
fi
