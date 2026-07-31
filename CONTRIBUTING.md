# Contributing to GetMP3

Thank you for contributing.

## Before starting

Search existing issues and pull requests before creating a new one.

For significant changes, open a feature request before writing a large implementation.

## Development requirements

- Android Studio.
- JDK 17.
- Android SDK.
- Git.
- Git LFS.
- A supported Android device or emulator.

## Setup

```bash
git clone https://github.com/TruongNgocKien1106/GetMP3.git
cd GetMP3
git lfs pull
```

## Build

### Windows

```powershell
.\gradlew.bat assembleDebug
```

### Linux or macOS

```bash
chmod +x gradlew
./gradlew assembleDebug
```

## Branch naming

Use clear branch names:

```text
feature/song-search
feature/download-history
fix/cover-embedding
fix/database-migration
docs/readme
refactor/download-pipeline
```

## Commit messages

Use concise conventional-style messages:

```text
feat: add in-app YouTube search
fix: preserve embedded MP3 cover
docs: update privacy policy
refactor: separate note repository
test: add filename normalization tests
```

## Pull requests

A pull request should:

- Address one focused change.
- Explain the problem.
- Explain the implementation.
- Build successfully.
- Include screenshots for UI changes.
- Include tests where practical.
- Avoid unrelated formatting changes.
- Avoid committed APK files.
- Avoid committed private data.
- Avoid committed signing keys.
- Avoid binaries with unknown origin.

## Coding guidelines

- Keep UI, repository, database, and processing logic separated.
- Use background dispatchers for file and network operations.
- Do not block the Compose main thread.
- Handle cancellation correctly.
- Prefer immutable UI state.
- Avoid destructive database migrations.
- Preserve existing user data.
- Validate external URLs and URIs.
- Return user-friendly errors instead of raw Python tracebacks.

## Responsible-use requirement

Pull requests must not add functionality intended to:

- Bypass DRM.
- Bypass authentication.
- Circumvent paywalls.
- Access private content without authorization.
- Hide prohibited activity.

## Before submitting

Run:

```powershell
.\gradlew.bat assembleDebug
git status
```

Confirm that generated files, local settings, credentials, and media files are not staged.