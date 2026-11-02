---
name: release
description: Prepare a new jtx Board release for the internal, beta or production channel. Takes a version number (e.g. 2.17.05, 2.17.05-alpha01, 2.17.05-beta02) and a release channel, bumps versionName/versionCode, writes English and German release notes for Google Play and F-Droid plus English GitHub release notes, cumulatively from all changes since the last production release, and opens a pull request against develop. Does not tag; the user merges and tags manually. Use when the user asks to release, publish, ship or prepare a new version / alpha / beta / internal / production build.
argument-hint: <version> <internal|beta|production>
---

# Create a jtx Board release

Input: a **version number** and a **release channel**. If either is missing, ask for it.

The version number is taken **exactly as given** — it is never auto-numbered:

- `2.17.05` → production release `v2.17.05`
- `2.17.05-alpha01` → internal release `v2.17.05-alpha01`
- `2.17.05-beta02` → beta release `v2.17.05-beta02`

The channel must match the suffix; if it doesn't (e.g. `2.17.05` with channel `beta`), tell the user and ask which one they meant.

The skill prepares everything in a **pull request against `develop`**. It does **not** create or push a tag: the user reviews and merges the PR and then tags the merge commit manually, which starts the release workflow.

## How releases work in this repo

| Channel      | Tag format          | Workflow triggered                     | Publishes to                                      |
|--------------|---------------------|----------------------------------------|---------------------------------------------------|
| `internal`   | `vX.YY.ZZ-alphaNN`  | `.github/workflows/create-alpha.yml`   | Google Play `internal` (no GitHub release)        |
| `beta`       | `vX.YY.ZZ-betaNN`   | `.github/workflows/create-beta.yml`    | GitHub pre-release + Google Play `internal,beta`  |
| `production` | `vX.YY.ZZ`          | `.github/workflows/create-release.yml` | GitHub release + Google Play `internal,beta,production` |

Pushing the tag (done manually by the user after merging the PR) is what starts the build and the upload. Release notes live in:

| Store                   | English (`en-US`)                                           | German (`de-DE`)                                            |
|-------------------------|-------------------------------------------------------------|-------------------------------------------------------------|
| Google Play             | `whatsnew/whatsnew-en-US`                                   | `whatsnew/whatsnew-de-DE`                                   |
| F-Droid                 | `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt` | `fastlane/metadata/android/de-DE/changelogs/<versionCode>.txt` |
| GitHub                  | `.github/release-notes.md`                                  | — (English only)                                            |

- **Google Play** reads every `whatsnew/whatsnew-<locale>` file via `whatsNewDirectory: whatsnew`. Max **500 characters** per language.
- **F-Droid** reads `fastlane/metadata/android/<locale>/changelogs/<versionCode>.txt`. Each file must be **byte-identical** to the `whatsnew-<locale>` file of the same language (the workflows warn otherwise). Max 500 characters.
- **GitHub** uses `.github/release-notes.md` as `body_path` of the GitHub release; GitHub's auto-generated PR list and contributors are appended below it.

Version names/codes live in `app/build.gradle.kts` (`versionCode = ...`, `versionName = "..."`). Production releases have no suffix in `versionName` (e.g. `2.17.04`). Version codes are `MAJOR MINOR(2) PATCH(2) BUILD(4)`, e.g. `2.17.04-beta07` → `217040007`; the build counter just increments per release of the same version and the code must always increase.

## Steps

### 1. Gather release info

First make sure you are on an up-to-date `develop` with a clean working tree (`git status --porcelain`, `git fetch origin develop`, `git checkout develop`, `git pull --ff-only origin develop`). If there are uncommitted changes or develop can't be fast-forwarded, tell the user and ask how to proceed.

Then run the helper from the repo root:

```bash
.claude/skills/release/scripts/release-info.sh <version> <channel>
```

The script takes the version exactly as given, checks that its suffix matches the channel, refuses existing tags, and prints `TAG`, `VERSION_NAME`, `VERSION_CODE`, `BASE_TAG`, `BASE_REASON`, the current branch and the commits since `BASE_TAG`.

`BASE_TAG` is always the **last production release**, for every channel. Release notes are therefore **cumulative**: an internal or beta release lists everything that changed since the last production release, not just since the previous alpha/beta, and the production release lists the same full set.

If the script fails, report the error to the user and stop.

Then create the release branch from develop: `git checkout -b release/<VERSION_NAME>` (if it already exists locally or on origin, ask the user before reusing or replacing it).

### 2. Understand the changes

For every commit in `BASE_TAG..HEAD`:

- Read the PR description when available (`gh pr view <number> --json title,body` if `gh` works, otherwise the GitHub MCP tools), and look at the diff (`git show <sha>`) when the title is not enough to understand the user-facing effect.
- Classify: **new feature / improvement**, **bug fix**, **translations** (`New translations ...`, Crowdin, new languages), **library/dependency updates** (`Bump ...`, dependabot), or **internal only** (CI, workflows, tests, refactoring, build scripts, version bumps).

### 3. Write the release notes

Write for end users, in English, in the style of the existing notes (see `git log -p -- whatsnew/whatsnew-en-US` and `fastlane/metadata/android/en-US/changelogs/`): short `- ` bullet lines, starting with a verb or "Fixed …", no PR numbers, no technical jargon. Order: features/improvements, then fixes, then (if any) `- Updated translations` and `- Library and stability updates`. Leave out internal-only changes. A newly supported language is worth its own bullet (e.g. `- Added Danish translation`).

**Google Play + F-Droid, English** (`whatsnew/whatsnew-en-US`):
- Plain text bullets only, no headings, no Markdown, no trailing newline (match the existing file).
- **At most 500 characters** — check with `wc -m whatsnew/whatsnew-en-US`. If too long, merge or drop the least important bullets (keep fixes general, e.g. "Various bug fixes").
- If there are no user-facing changes, use `- Library and stability updates`.

**Google Play + F-Droid, German** (`whatsnew/whatsnew-de-DE`):
- A German version of the English notes with the **same bullets in the same order**. Translate the meaning naturally rather than word by word; don't add or drop items compared to English.
- Address users formally (`Sie`), like the app's German strings in `app/src/main/res/values-de/strings.xml`. Use the terms the app uses there for UI elements (e.g. look up the German string for "collection", "checkbox", "detail view") so the notes match what users see.
- Standard phrases: `- Übersetzungen aktualisiert`, `- Bibliotheks- und Stabilitätsupdates`, `- Fehler behoben: …` / `- Behoben: …`, a new language as e.g. `- Dänische Übersetzung hinzugefügt`.
- Same format rules: plain text, no trailing newline, **at most 500 characters** (`wc -m whatsnew/whatsnew-de-DE`). German is usually longer than English — if it exceeds the limit, shorten the German wording first; only merge bullets if still necessary, and then shorten the English text the same way so both stay aligned.

Then copy both exactly into the F-Droid changelogs (create the `de-DE` directory if needed):

```bash
cp whatsnew/whatsnew-en-US fastlane/metadata/android/en-US/changelogs/<VERSION_CODE>.txt
mkdir -p fastlane/metadata/android/de-DE/changelogs
cp whatsnew/whatsnew-de-DE fastlane/metadata/android/de-DE/changelogs/<VERSION_CODE>.txt
```

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

### 5. Commit, push and open a pull request

```bash
git add app/build.gradle.kts whatsnew/whatsnew-en-US whatsnew/whatsnew-de-DE \
  fastlane/metadata/android/en-US/changelogs/<VERSION_CODE>.txt \
  fastlane/metadata/android/de-DE/changelogs/<VERSION_CODE>.txt \
  .github/release-notes.md
git commit -m "Release <VERSION_NAME>"
git push -u origin release/<VERSION_NAME>
```

If the push fails because of the network, retry with backoff; never force-push.

Open a pull request **into `develop`** (with `gh pr create --base develop --head release/<VERSION_NAME>` if `gh` works, otherwise the GitHub MCP `create_pull_request` tool):

- Title: `Release <VERSION_NAME>`
- Body:
  - Channel, tag to create after merging (`<TAG>`), version name/code, and the base tag with `BASE_REASON`
  - The English and the German Google Play / F-Droid text, each in its own code block with its character count (`/500`)
  - The GitHub release notes
  - The commands to tag after merging (see step 6)

**Do not create or push any tag.**

### 6. Report

Give the user the PR link, a short summary (tag, channel, version code, base tag, English and German Play/F-Droid character counts), and the commands to run after reviewing and merging the PR. The tag must point at the commit on `develop` that contains the merged release changes (with squash merges this is the new squash commit, not the commit on the release branch):

```bash
git checkout develop
git pull --ff-only origin develop
git tag -a <TAG> -m "<TAG>"
git push origin <TAG>
```

Mention which workflow the tag triggers and which tracks it publishes to (see the table above).
