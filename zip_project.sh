#!/bin/bash

PROJECT_NAME="codeeditor-android"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
OUTPUT_FILE="${PROJECT_NAME}_${TIMESTAMP}.zip"

echo "================================================"
echo "  Project Zipper - High Compression Mode"
echo "================================================"
echo ""
echo "Starting to zip project: $PROJECT_NAME"
echo "Output file: $OUTPUT_FILE"
echo ""

if command -v zip &> /dev/null; then
    echo "Using zip with maximum compression (-9)..."
    
    zip -9 -r "$OUTPUT_FILE" \
        app/ \
        gradle/ \
        .github/ \
        .gitignore \
        build.gradle \
        build_debug.sh \
        build_release.sh \
        gradle.properties \
        gradlew \
        LICENSE \
        README.md \
        release-keystore.jks \
        replit.md \
        settings.gradle \
        validate_project.sh \
        zip_project.sh \
        -x "*.git/*" \
        -x "*build/*" \
        -x "*.idea/*" \
        -x "*.gradle/*" \
        -x "*__pycache__/*"
    
    if [ $? -eq 0 ]; then
        FILE_SIZE=$(du -h "$OUTPUT_FILE" | cut -f1)
        echo ""
        echo "================================================"
        echo "  ZIP COMPLETED SUCCESSFULLY!"
        echo "================================================"
        echo "File: $OUTPUT_FILE"
        echo "Size: $FILE_SIZE"
        echo ""
        ls -lh "$OUTPUT_FILE"
    else
        echo "ERROR: Failed to create zip file"
        exit 1
    fi
else
    echo "ERROR: zip command not found. Please install zip."
    exit 1
fi
