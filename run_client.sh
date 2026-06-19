#!/bin/bash

# Direct Fabric client launch script
# Usage: ./run_client.sh

cd /home/anvres/Изображения/quilt/run

# Find Java
JAVA_CMD="java"
if ! command -v java &> /dev/null; then
    echo "Java not found in PATH"
    exit 1
fi

# Find fabric loader
FABRIC_LOADER=$(find .fabric -name "fabric-loader-*.jar" | head -n 1)
if [ -z "$FABRIC_LOADER" ]; then
    echo "Fabric loader not found. Please run the game at least once."
    exit 1
fi

echo "Using Fabric Loader: $FABRIC_LOADER"
echo ""

# Run Minecraft with Fabric
$JAVA_CMD -Xmx2G -Xms1G \
    -DfabricApi=dynamical=true \
    -Dfabric.loadFromPropertiesFile=true \
    -Dfabric.mappingEnvironment=client \
    -cp ".fabric/*:mods/*" \
    net.fabricmc.loader.impl.launch.knot.KnotClient
