#!/bin/bash

# Quilt Client Launch Script
# Minecraft 1.21.4 + Fabric

set -e

echo "=== Quilt Client ==="
echo ""

# Check if mod jar exists and is recent
MOD_JAR="run/mods/quilt.jar"
BUILD_JAR=$(find build/libs -name "*dev*.jar" -newer "$MOD_JAR" 2>/dev/null | head -n 1)

if [ -n "$BUILD_JAR" ]; then
    echo "Building mod..."
    ./gradlew remapJar --no-daemon
    BUILD_JAR=$(find build/libs -name "*dev*.jar" | head -n 1)
    if [ -z "$BUILD_JAR" ]; then
        echo "Error: Could not find built jar file"
        exit 1
    fi
    echo "Built: $BUILD_JAR"
    cp "$BUILD_JAR" "$MOD_JAR"
else
    echo "Using existing mod jar"
fi

echo ""
echo "Mod jar copied to: $MOD_JAR"
echo ""
echo "To launch manually:"
echo "  1. Run fabric-loader from .fabric/"
echo "  2. Or use your IDE's run configuration"
echo ""
echo "Attempting to launch via gradle..."
echo ""

# Launch using gradle runClient task
./gradlew runClient --no-daemon 2>&1 || {
    echo ""
    echo "Gradle launch failed (likely xvfb issue)"
    echo "You can manually launch Minecraft with the mod in run/mods/"
}
