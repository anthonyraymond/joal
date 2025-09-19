#!/bin/bash

# JOAL initialization script
# Downloads and extracts JOAL release if files are not present

DATA_DIR="/data"
JOAL_VERSION="2.1.36"
JOAL_URL="https://github.com/anthonyraymond/joal/releases/download/${JOAL_VERSION}/joal.tar.gz"
REQUIRED_FILES=("config.json" "clients")

echo "JOAL initialization script starting..."

# Create data directory if it doesn't exist
mkdir -p "$DATA_DIR"

# Check if required files exist
missing_files=false
for file in "${REQUIRED_FILES[@]}"; do
    if [ ! -e "$DATA_DIR/$file" ]; then
        echo "Missing required file/directory: $file"
        missing_files=true
    fi
done

# Download and extract JOAL if files are missing
if [ "$missing_files" = true ]; then
    echo "Required files are missing. Downloading JOAL release..."
    
    # Download the release
    cd /tmp
    if wget -q "$JOAL_URL" -O joal.tar.gz; then
        echo "Downloaded JOAL release successfully"
        
        # First, let's see what's in the archive
        echo "Archive contents:"
        tar -tzf joal.tar.gz | head -10
        
        # Extract to temporary directory first
        mkdir -p /tmp/joal_extract
        if tar -xzf joal.tar.gz -C /tmp/joal_extract; then
            echo "Extracted JOAL files to temporary directory"
            
            # Find the actual directory structure
            echo "Looking for config files..."
            find /tmp/joal_extract -name "config.json" -o -name "clients" -type d
            
            # Copy files to data directory
            if [ -f "/tmp/joal_extract/config.json" ]; then
                cp /tmp/joal_extract/config.json "$DATA_DIR/"
                echo "Copied config.json"
            fi
            
            if [ -d "/tmp/joal_extract/clients" ]; then
                cp -r /tmp/joal_extract/clients "$DATA_DIR/"
                echo "Copied clients directory"
            fi
            
            # Clean up
            rm -rf /tmp/joal_extract
        else
            echo "Error: Failed to extract JOAL files"
            exit 1
        fi
        
        # Clean up
        rm joal.tar.gz
    else
        echo "Error: Failed to download JOAL release from $JOAL_URL"
        exit 1
    fi
else
    echo "All required files are present. Skipping download."
fi

# Ensure required directories exist
echo "Creating required directories if they don't exist..."
mkdir -p "$DATA_DIR/torrents"
mkdir -p "$DATA_DIR/torrents/archived"
echo "Required directories created."

echo "JOAL initialization complete. Starting application..."

# Start JOAL with the provided arguments
exec java -jar /joal/joal.jar "$@"