# Third-party Components

GetMP3 uses third-party software.

Each component remains governed by its own copyright and license terms.

The GetMP3 license does not replace or override third-party licenses.

## Main components

| Component | Purpose | Upstream |
|---|---|---|
| Kotlin | Android application language | https://kotlinlang.org |
| AndroidX | Android framework libraries | https://developer.android.com/jetpack/androidx |
| Jetpack Compose | Declarative user interface | https://developer.android.com/compose |
| Room | Local database | https://developer.android.com/training/data-storage/room |
| Material 3 | User interface components | https://m3.material.io |
| Chaquopy | Python runtime integration | https://chaquo.com/chaquopy |
| yt-dlp | Supported URL extraction and search | https://github.com/yt-dlp/yt-dlp |
| FFmpeg | Audio conversion and processing | https://ffmpeg.org |
| Mutagen | MP3 and ID3 metadata processing | https://mutagen.readthedocs.io |
| Coil, when included | Android image loading | https://coil-kt.github.io/coil |

## Release compliance

Before distributing an APK, the release maintainer must verify:

- Exact dependency versions.
- Exact license text for each pinned version.
- FFmpeg build configuration.
- Enabled FFmpeg codecs.
- Enabled external FFmpeg libraries.
- Whether the bundled FFmpeg build is LGPL or GPL.
- Corresponding source requirements.
- Attribution requirements.
- Notice requirements.
- Redistribution terms for native binaries.
- Chaquopy licensing terms for the selected edition.

## FFmpeg notice

FFmpeg licensing depends on the exact binary build configuration.

Enabling certain external libraries or build flags can change redistribution obligations.

Each public release should retain:

- FFmpeg version.
- Source URL or source archive.
- Complete build configuration.
- Build scripts.
- Applicable license text.
- Corresponding source availability information.

## Modified distributions

Anyone distributing a modified GetMP3 build is responsible for reviewing and satisfying all applicable third-party licenses.