#!/bin/bash
# Build script for xiuxin-app Android application on macOS
# This script builds the debug APK using Gradle wrapper

set -e  # Exit on any error

echo "=== Building xiuxin-app for Android on macOS ==="
echo "Working directory: $(pwd)"
echo ""

# Ensure we're in the project root
cd "$(dirname "$0")"

# Check if gradlew exists and make it executable
if [ ! -f "gradlew" ]; then
    echo "Error: gradlew not found in project root"
    exit 1
fi

chmod +x gradlew

# Clean previous build artifacts
echo "Cleaning previous build..."
./gradlew clean

# Assemble debug APK
echo "Assembling debug APK..."
./gradlew assembleDebug

# Check if build was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "=== BUILD SUCCESSFUL ==="
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    if [ -f "$APK_PATH" ]; then
        echo "Debug APK location: $APK_PATH"
        echo "APK size: $(du -h "$APK_PATH" | cut -f1)"
        echo ""
        echo "To install on connected device:"
        echo "  ./gradlew installDebug"
        echo ""
        echo "To run tests:"
        echo "  ./gradlew test"
    else
        echo "Warning: Expected APK not found at $APK_PATH"
        # Look for any APK in the build outputs
        find app/build/outputs/apk -name "*.apk" 2>/dev/null | while read apk; do
            echo "Found APK: $apk"
        done
    fi
else
    echo "=== BUILD FAILED ==="
    exit 1
fi

echo ""
echo "Build completed at: $(date)"