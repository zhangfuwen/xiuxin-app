#!/bin/bash

# Define the Android SDK root directory
ANDROID_SDK_ROOT="$HOME/Library/Android/sdk"

# Function to clean up and exit on error
die() {
    echo "Error: $1" >&2
    exit 1
}

# Check if Android SDK is installed
if [ ! -d "$ANDROID_SDK_ROOT" ]; then
    die "Android SDK not found at $ANDROID_SDK_ROOT. Please install Android SDK and try again."
fi

# Set environment variables
export ANDROID_HOME="$ANDROID_SDK_ROOT"
export PATH="$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools"

# Check for Gradle availability
if [ ! -x "./gradlew" ]; then
    die "Gradle not found. Please install Gradle using Android SDK Manager."
fi

# Navigate to the project directory
cd ~/Code/github.com/zhangfuwen/xiuxin-app/android || die "Unable to navigate to the project directory."

# Display start message
echo "============================"
echo "Starting Android APK build..."
echo "============================"

# Perform clean and build with SSL/TLS fixes for macOS
# --system-prop is equivalent to GRADLE_OPTS
# --no-daemon prevents background processes
# --no-build-cache ignores cached builds
# --refresh-dependencies ensures fresh dependency download
# --stacktrace provides detailed error logs
./gradlew assembleDebug --system-prop=jdk.tls.client.protocols=TLSv1.2 --system-prop=jdk.net.http.client.insecureProtocol=true --no-daemon --no-build-cache --refresh-dependencies --stacktrace

# Check if the APK was built
if [ -f "./app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "============================"
    echo "Build successful!"
    echo "APK located at: ./app/build/outputs/apk/debug/app-debug.apk"
    echo "============================"
else
    echo "Build failed or APK not found."
    exit 1
fi

exit 0