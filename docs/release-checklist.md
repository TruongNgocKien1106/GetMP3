# Release Checklist

## Versioning

- [ ] Update `versionName`.
- [ ] Increase `versionCode`.
- [ ] Update `CHANGELOG.md`.
- [ ] Confirm the Git tag matches the version.

## Build quality

- [ ] Debug build succeeds.
- [ ] Release build succeeds.
- [ ] Release APK is signed with the stable release key.
- [ ] No signing files are committed.
- [ ] No local paths are committed.
- [ ] No private URLs are committed.
- [ ] No downloaded media is committed.

## Device testing

- [ ] Test on a physical arm64 Android device.
- [ ] Test on a second Android version.
- [ ] Test fresh installation.
- [ ] Test upgrade from the previous release.
- [ ] Test note database persistence.
- [ ] Test download folder persistence.
- [ ] Test comparison-folder permissions.
- [ ] Test download queue recovery.
- [ ] Test tag editing.
- [ ] Test file deletion.
- [ ] Test embedded cover artwork.
- [ ] Test light theme.
- [ ] Test dark theme.

## Network testing

- [ ] Test normal Wi-Fi.
- [ ] Test mobile data.
- [ ] Test unavailable network.
- [ ] Test timeout behavior.
- [ ] Test invalid URL behavior.
- [ ] Test search cancellation.

## Privacy and security

- [ ] Review requested Android permissions.
- [ ] Review logging.
- [ ] Remove private data from screenshots.
- [ ] Review URI validation.
- [ ] Review file deletion paths.
- [ ] Review dependency vulnerabilities.
- [ ] Enable private vulnerability reporting.

## Third-party compliance

- [ ] Record exact dependency versions.
- [ ] Record FFmpeg version.
- [ ] Record FFmpeg build configuration.
- [ ] Confirm applicable FFmpeg license.
- [ ] Store applicable license texts.
- [ ] Confirm native binary redistribution rights.
- [ ] Review Chaquopy distribution terms.
- [ ] Review yt-dlp distribution requirements.
- [ ] Review Mutagen distribution requirements.

## GitHub release

- [ ] Create an annotated version tag.
- [ ] Create a GitHub Release.
- [ ] Upload the signed APK.
- [ ] Publish release notes.
- [ ] Include known limitations.
- [ ] Include Android requirements.
- [ ] Include the APK SHA-256 checksum.
- [ ] Verify that the APK can be downloaded and installed.