---
name: release
description: Create a new jtx Board release for the internal, beta or production channel. Takes a version number (e.g. 2.17.05 or 2.17.05-beta02) and a release channel, bumps versionName/versionCode, writes release notes for GitHub, Google Play and F-Droid from the changes since the last release of that channel (or since the last production release), commits, tags and pushes. Use when the user asks to release, publish, ship or tag a new version / alpha / beta / internal / production build.
argument-hint: <version> <internal|beta|production>
---

# Create a jtx Board release

Input: a **version number** and a **release channel**. If either is missing, ask for it.

## How releases work in this repo

| Channel      | Tag format          | Workflow triggered                     | Publishes to                                      |
|--------------|---------------------|----------------------------------------|---------------------------------------------------|
| `internal`   | `vX.YY.ZZ-alphaNN`  | `.github/workflows/create-alpha.yml`   | Google Play `internal` (no GitHub release)        |
| `beta`       | `vX.YY.ZZ-betaNN`   | `.github/workflows/create-beta.yml`    | GitHub pre-release + Google Play `internal,beta`  |
| `production` | `vX.YY.ZZ`          | `.github/workflows/create-release.yml` | GitHub release + Google Play `internal,beta,production` |

Pushing the tag is what starts the build and the upload. Release notes live in:

- **Google Play**: `whatsnew/whatsnew-en-US` (read by the upload action via `whatsNewDirectory: whatsnew`). Max **500 characters**.
- **F-Droid**: `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`. Must be **byte-identical** to `whatsnew/whatsnew-en-US` (the workflows warn otherwise). Max 500 characters.
- **GitHub**: `.github/release-notes.md`, used as `body_path` of the GitHub release; GitHub's auto-generated PR list and contributors are appended below it.

Version names/codes live in `app/build.gradle.kts` (`versionCode = ...`, `versionName = "..."`). Production releases have no suffix in `versionName` (e.g. `2.17.04`). Version codes are `MAJOR MINOR(2) PATCH(2) BUILD(4)`, e.g. `2.17.04-beta07` → `217040007`; the build counter just increments per release of the same version and the code must always increase.

## Steps

### 1. Gather release info

Run the helper from the repo root:

```bash
.claude/skills/release/scripts/release-info.sh <version> <channel>
```

It accepts `2.17.05` (it then picks the next free `-alphaNN`/`-betaNN` number) or a full name like `2.17.05-beta02`, validates the suffix against the channel, refuses existing tags, and prints `TAG`, `VERSION_NAME`, `VERSION_CODE`, `BASE_TAG`, `BASE_REASON`, the current branch and the commits since `BASE_TAG`.

`BASE_TAG` is the last release of the **same channel**, unless the last **production** release is newer than it (or there is none) — then it is the last production release. For a production release it is always the previous production release.

If the script fails, report the error to the user and stop. If `BRANCH` is not `develop`, or the working tree is not clean (`git status --porcelain`), tell the user and ask whether to continue.

### 2. Understand the changes

For every commit in `BASE_TAG..HEAD`:

- Read the PR description when available (`gh pr view <number> --json title,body` if `gh` works, otherwise the GitHub MCP tools), and look at the diff (`git show <sha>`) when the title is not enough to understand the user-facing effect.
- Classify: **new feature / improvement**, **bug fix**, **translations** (`New translations ...`, Crowdin, new languages), **library/dependency updates** (`Bump ...`, dependabot), or **internal only** (CI, workflows, tests, refactoring, build scripts, version bumps).

### 3. Write the release notes

Write for end users, in English, in the style of the existing notes (see `git log -p -- whatsnew/whatsnew-en-US` and `fastlane/metadata/android/en-US/changelogs/`): short `- ` bullet lines, starting with a verb or "Fixed …", no PR numbers, no technical jargon. Order: features/improvements, then fixes, then (if any) `- Updated translations` and `- Library and stability updates`. Leave out internal-only changes. A newly supported language is worth its own bullet (e.g. `- Added Danish translation`).

**Google Play + F-Droid** (`whatsnew/whatsnew-en-US`):
- Plain text bullets only, no headings, no Markdown, no trailing newline (match the existing file).
- **At most 500 characters** — check with `wc -m whatsnew/whatsnew-en-US`. If too long, merge or drop the least important bullets (keep fixes general, e.g. "Various bug fixes").
- If there are no user-facing changes, use `- Library and stability updates`.
- Then copy it exactly: `cp whatsnew/whatsnew-en-US fastlane/metadata/android/en-US/changelogs/<VERSION_CODE>.txt`.

**GitHub** (`.github/release-notes.md`):
- Markdown, no length limit. Start with `## What's new`, then the same bullets, optionally with a bit more detail per item and a `### Fixes` sub-section when there are many fixes. Issue references like `#1234` are fine here.
- Do not list every PR — GitHub appends the generated PR list automatically.
- For `internal` releases no GitHub release is created; still update the file so it matches the latest build.

### 4. Bump the version

In `app/build.gradle.kts` change only the two values, keeping the trailing comment on the `versionName` line:

```
versionCode = <VERSION_CODE>
versionName = "<VERSION_NAME>"
```

### 5. Review with the user

Show the user: tag, channel, version name/code, base tag (with `BASE_REASON`), the Google Play/F-Droid text with its character count, and the GitHub notes. **Wait for confirmation** before committing — pushing the tag publishes to Google Play and GitHub and cannot easily be undone. Apply any requested changes to the notes first.

### 6. Commit, tag and push

```bash
git add app/build.gradle.kts whatsnew/whatsnew-en-US fastlane/metadata/android/en-US/changelogs/<VERSION_CODE>.txt .github/release-notes.md
git commit -m "Release <VERSION_NAME>"
git tag -a <TAG> -m "<TAG>"
git push origin HEAD
git push origin <TAG>
```

Push the branch before the tag so the commit exists on the branch when the workflow runs. If a push fails because of the network, retry with backoff; if it is rejected (e.g. branch behind the remote), stop and tell the user — do not force-push and do not move existing tags.

### 7. Report

Tell the user the tag that was pushed, which workflow it triggers and which tracks it publishes to, and link the Actions run page (`https://github.com/TechbeeAT/jtxBoard/actions`).
