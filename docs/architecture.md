# GetMP3 Architecture

## Overview

GetMP3 follows a local-first Android architecture.

The main layers are:

```text
Compose UI
    |
ViewModels
    |
Repositories
    |
Room / SharedPreferences / MediaStore / SAF
    |
Chaquopy Python bridge
    |
yt-dlp / Mutagen / FFmpeg
```

## User interface

Jetpack Compose renders the main application tabs:

- Download.
- Waiting list.
- Tag editor.
- Settings.

UI state is exposed through `StateFlow` and observed with lifecycle-aware Compose APIs.

## ViewModels

ViewModels manage:

- Screen state.
- User actions.
- Validation.
- Background jobs.
- Events.
- User-facing messages.

Long-running work must not execute on the main thread.

## Repositories

Repositories isolate:

- Room access.
- SharedPreferences access.
- MediaStore operations.
- Storage Access Framework operations.
- Python bridge calls.
- File scanning.
- Metadata reading and writing.
- Comparison-folder indexing.

## Download pipeline

```text
Supported URL
    |
Download queue
    |
yt-dlp extraction
    |
Temporary audio
    |
FFmpeg MP3 conversion
    |
Mutagen ID3 metadata
    |
Cover verification
    |
MediaStore or SAF destination
```

The final destination should be written only after conversion and metadata operations complete successfully.

## Waiting-list pipeline

```text
User input
    |
Title and artist parsing
    |
Duplicate note check
    |
Comparison-folder similarity check
    |
Room persistence
    |
In-app YouTube search
    |
Selected URL
    |
Download queue
```

## Tag editor

The tag editor:

- Scans configured MP3 files.
- Loads metadata in the background.
- Updates title, artist, album, and filename.
- Preserves or updates cover artwork.
- Supports save-and-next, skip, and delete operations.

## Storage

GetMP3 uses:

- Room for structured local records.
- SharedPreferences for lightweight settings.
- MediaStore for public media collections.
- Storage Access Framework for user-selected folders.
- Private application storage for temporary and cached data.

## Security boundaries

The application should:

- Access only user-authorized folders.
- Validate external URIs.
- Avoid arbitrary shell commands.
- Avoid exposing local paths in public logs.
- Avoid destructive database migrations.
- Avoid storing secrets in the repository.
- Avoid displaying raw Python tracebacks to users.