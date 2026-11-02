#!/usr/bin/env bash
# Works out everything needed for a new jtx Board release.
#
# Usage: release-info.sh <version> <channel>
#   version: X.YY.ZZ (e.g. 2.17.05) or a full name with suffix (2.17.05-beta02)
#   channel: internal | beta | production
#
# Prints KEY=value lines (TAG, VERSION_NAME, VERSION_CODE, CURRENT_VERSION_NAME,
# CURRENT_VERSION_CODE, BASE_TAG, BASE_REASON) followed by the commits since BASE_TAG.
# Exits non-zero with a message on invalid input.
set -euo pipefail

die() { echo "ERROR: $*" >&2; exit 1; }

[[ $# -eq 2 ]] || die "usage: $0 <version> <channel>"
version="$1"
channel="$(echo "$2" | tr '[:upper:]' '[:lower:]')"
version="${version#v}"

case "$channel" in
  internal|alpha) channel=internal; suffix=alpha ;;
  beta)           suffix=beta ;;
  production|release|prod) channel=production; suffix="" ;;
  *) die "unknown channel '$2' (use internal, beta or production)" ;;
esac

repo_root="$(git rev-parse --show-toplevel)"
gradle_file="$repo_root/app/build.gradle.kts"

# Split the requested version into base and optional suffix.
if [[ "$version" =~ ^([0-9]+)\.([0-9]{2})\.([0-9]{2})(-(alpha|beta)([0-9]{2}))?$ ]]; then
  major="${BASH_REMATCH[1]}"; minor="${BASH_REMATCH[2]}"; patch="${BASH_REMATCH[3]}"
  given_suffix="${BASH_REMATCH[5]:-}"; given_number="${BASH_REMATCH[6]:-}"
else
  die "version '$version' must look like 2.17.05 or 2.17.05-beta02"
fi
base_version="$major.$minor.$patch"

if [[ -n "$given_suffix" && "$given_suffix" != "$suffix" ]]; then
  expected="no suffix"; [[ -n "$suffix" ]] && expected="-$suffix"
  die "version suffix '-$given_suffix' does not match channel '$channel' (expected $expected)"
fi

# Make sure all tags are known locally.
git fetch --tags --quiet origin 2>/dev/null || echo "WARNING: could not fetch tags from origin, using local tags" >&2

release_tag_re='^v[0-9]+\.[0-9]{2}\.[0-9]{2}(-(alpha|beta)[0-9]+)?$'
production_re='^v[0-9]+\.[0-9]{2}\.[0-9]{2}$'

# Determine the tag/version name.
if [[ -z "$suffix" ]]; then
  version_name="$base_version"
elif [[ -n "$given_number" ]]; then
  version_name="$base_version-$suffix$given_number"
else
  last_n=$(git tag -l "v$base_version-$suffix*" | sed -nE "s/^v$base_version-$suffix([0-9]+)$/\1/p" | sort -n | tail -1)
  version_name=$(printf '%s-%s%02d' "$base_version" "$suffix" $((10#${last_n:-0} + 1)))
fi
tag="v$version_name"
git rev-parse -q --verify "refs/tags/$tag" >/dev/null && die "tag $tag already exists"

# Determine the version code.
current_code=$(grep -oP 'versionCode = \K[0-9]+' "$gradle_file")
current_name=$(grep -oP 'versionName = "\K[^"]+' "$gradle_file")
current_base="${current_name%%-*}"
if [[ "$current_base" == "$base_version" ]]; then
  version_code=$((current_code + 1))
else
  version_code=$(( 10#$major$minor$patch * 10000 + 1 ))
  (( version_code > current_code )) || version_code=$((current_code + 1))
fi

# Determine the base tag for the release notes:
# the last tag of the same channel, unless a production release happened after it,
# in which case the last production release is used.
last_production=$(git tag -l --sort=-creatordate | grep -E "$production_re" | head -1 || true)
[[ -n "$last_production" ]] || die "no production release tag found"
if [[ "$channel" == production ]]; then
  base_tag="$last_production"
  base_reason="last production release"
else
  last_channel=$(git tag -l --sort=-creatordate | grep -E "$release_tag_re" | grep -E -- "-$suffix[0-9]+$" | head -1 || true)
  if [[ -n "$last_channel" ]] && \
     [[ $(git log -1 --format=%ct "$last_channel") -gt $(git log -1 --format=%ct "$last_production") ]] && \
     git merge-base --is-ancestor "$last_production" "$last_channel"; then
    base_tag="$last_channel"
    base_reason="last $channel release (newer than last production release $last_production)"
  else
    base_tag="$last_production"
    base_reason="no $channel release since last production release"
  fi
fi

echo "CHANNEL=$channel"
echo "TAG=$tag"
echo "VERSION_NAME=$version_name"
echo "VERSION_CODE=$version_code"
echo "CURRENT_VERSION_NAME=$current_name"
echo "CURRENT_VERSION_CODE=$current_code"
echo "BASE_TAG=$base_tag"
echo "BASE_REASON=$base_reason"
echo "BRANCH=$(git rev-parse --abbrev-ref HEAD)"
echo
echo "--- commits $base_tag..HEAD (without version bumps) ---"
git log --no-merges --format='%h %s' "$base_tag..HEAD" | grep -viE '^[0-9a-f]+ version bump$' || true
