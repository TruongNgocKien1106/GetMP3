# GetMP3

[![Android CI](https://github.com/TruongNgocKien1106/GetMP3/actions/workflows/android-build.yml/badge.svg)](https://github.com/TruongNgocKien1106/GetMP3/actions/workflows/android-build.yml)
![Android](https://img.shields.io/badge/platform-Android-3DDC84)
![Kotlin](https://img.shields.io/badge/language-Kotlin-7F52FF)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4)
![Version](https://img.shields.io/badge/version-auto--dev-2EA44F)
![License](https://img.shields.io/badge/license-MIT-blue)

GetMP3 is a local-first Android audio workflow for recording songs to download later, finding suitable YouTube results, processing personal audio files, and editing MP3 metadata directly on the device.

> GetMP3 is not affiliated with, endorsed by, maintained by, or sponsored by YouTube, Google, FFmpeg, yt-dlp, or any other third-party platform.

## Features

### Download workflow

- Accept supported YouTube URLs.
- Maintain a download queue.
- Convert audio to MP3 with FFmpeg.
- Select a preferred MP3 bitrate.
- Save files to `Music/GetMP3` or a user-selected folder.
- Write ID3 title, artist, album, and cover artwork.
- Display download progress and status.

### Waiting list

- Record songs to download later.
- Parse input written as `Title - Artist`.
- Mark notes as important.
- Mark notes as completed.
- Delete individual notes.
- Delete all completed notes.
- Detect duplicate and similar titles.
- Compare titles against MP3 filenames in a selected folder.
- Search YouTube from inside the application.
- Send selected search results directly to the download queue.

### MP3 tag editor

- Scan MP3 files from the configured download folder.
- Edit title, artist, and album.
- Rename files to `Title - Artist.mp3`.
- Remove decorative Unicode symbols.
- Remove configurable promotional phrases.
- Suggest artists from the comparison-folder index.
- Save and continue to the next file.
- Skip or delete the current file.

### Settings

- Configure MP3 bitrate.
- Select a download folder.
- Select a comparison folder.
- Index artist and album names.
- Configure quick-format phrases.
- Use light, dark, or system theme.

## Screenshots

Product screenshots are stored under:

```text
docs/screenshots/
```

Recommended filenames:

```text
download.png
waiting-list.png
youtube-search.png
tag-editor.png
settings.png
```

Before committing screenshots, remove notifications, email addresses, account names, IP addresses, ADB ports, private URLs, private filenames, and other personal information.

## Current release

Development builds use an automatically generated version such as **1.0.0-dev.YYYYMMDD.HHMMSS**.

Public APK files should be signed with a stable release key and distributed through GitHub Releases.

APK files must not be committed directly to the repository.

## Technology stack

- Kotlin
- Jetpack Compose
- AndroidX Lifecycle
- Room
- Material 3
- Chaquopy
- Python
- yt-dlp
- FFmpeg
- Mutagen

## Requirements

- Android Studio with a compatible Android SDK.
- JDK 17.
- Git.
- Git LFS for bundled native binaries.
- Network access for search and supported URL processing.
- Android folder access granted explicitly by the user.

## Clone

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

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Install with ADB

```powershell
$Adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"

& $Adb install -r `
    ".\app\build\outputs\apk\debug\app-debug.apk"
```

Development scripts are available under:

```text
scripts/
```

Typical development workflow:

```powershell
.\scripts\connect_by_wifi.ps1
.\scripts\build.ps1
.\scripts\run.ps1
```

## Data and privacy

GetMP3 does not require a mandatory application account or a project-operated user backend.

Notes, preferences, folder references, and local indexes are stored on the device.

Network requests occur when the user starts an action such as search, URL processing, thumbnail retrieval, or media download.

Read [PRIVACY.md](PRIVACY.md).

## Responsible use

Users are responsible for ensuring that they have permission to access, process, convert, and store the content they select.

GetMP3 is not designed to bypass DRM, authentication, paywalls, access controls, or protected streams.

Read [DISCLAIMER.md](DISCLAIMER.md).

## Architecture

Read [docs/architecture.md](docs/architecture.md).

## Known limitations

- Source platforms may change without notice.
- Search results may contain incorrect versions, remixes, karaoke, covers, or unrelated content.
- Some Android media players cache old MP3 metadata and artwork.
- Background execution behavior may vary between device manufacturers.
- FFmpeg redistribution obligations depend on the exact bundled binary configuration.
- Native binary compatibility depends on the device ABI.

## Reporting issues

Use the provided GitHub issue templates.

Include:

- GetMP3 version.
- Android version.
- Device model.
- Reproduction steps.
- Expected behavior.
- Actual behavior.
- Relevant Logcat output with private information removed.

Security-sensitive issues must follow [SECURITY.md](SECURITY.md).

## Contributing

Read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.

## Third-party components

Read [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

## License

Original GetMP3 source code is licensed under the [MIT License](LICENSE).

Third-party libraries and native binaries remain governed by their respective licenses.