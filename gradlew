#!/bin/sh
# Prefer an installed Gradle. If absent, bootstrap the pinned distribution.
set -eu
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
if command -v gradle >/dev/null 2>&1; then exec gradle -p "$APP_HOME" "$@"; fi
VERSION=8.10.2
CACHE="${GRADLE_USER_HOME:-$HOME/.gradle}/bootstrap/gradle-$VERSION"
if [ ! -x "$CACHE/bin/gradle" ]; then
  mkdir -p "$(dirname "$CACHE")"
  ZIP="${TMPDIR:-/tmp}/gradle-$VERSION-bin.zip"
  echo "Gradle $VERSION is not installed; downloading the pinned distribution..."
  if command -v curl >/dev/null 2>&1; then curl -fL "https://services.gradle.org/distributions/gradle-$VERSION-bin.zip" -o "$ZIP";
  elif command -v wget >/dev/null 2>&1; then wget -O "$ZIP" "https://services.gradle.org/distributions/gradle-$VERSION-bin.zip";
  else echo "Install Gradle $VERSION or curl/wget and retry." >&2; exit 1; fi
  mkdir -p "$(dirname "$CACHE")"
  unzip -q "$ZIP" -d "$(dirname "$CACHE")"
fi
exec "$CACHE/bin/gradle" -p "$APP_HOME" "$@"
