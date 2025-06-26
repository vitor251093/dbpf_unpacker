#!/bin/bash

# Fetch tags
git fetch --tags

# Get the latest tag or use a default version if no tag exists
current_version=$(git describe --tags --abbrev=0 2>/dev/null || echo "v0.0.0")

# Remove the 'v' prefix if it exists
current_version="${current_version#v}"

# Separate the string into an array
IFS='.' read -ra numbers <<< "$current_version"

# Ensure we have three numbers, defaulting to 0 if not present
major=${numbers[0]:-0}
minor=${numbers[1]:-0}
patch=${numbers[2]:-0}

# Logic for version increment
if [ $patch -ge 9 ]; then
    # If patch is 9 or higher, increment minor and reset patch
    new_minor=$((minor + 1))
    new_patch=0
    
    # If minor reaches 10, increment major and reset minor
    if [ $new_minor -ge 10 ]; then
        new_major=$((major + 1))
        new_minor=0
    else
        new_major=$major
    fi
else
    # Otherwise, just increment patch
    new_major=$major
    new_minor=$minor
    new_patch=$((patch + 1))
fi

# Output the new version
new_version="${new_major}.${new_minor}.${new_patch}"
echo "$new_version"