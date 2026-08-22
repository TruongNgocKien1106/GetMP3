import glob
import json
import os
import re
import ssl
import time
import traceback
import urllib.parse
import urllib.request

import certifi
import yt_dlp
from mutagen.id3 import (
    APIC,
    ID3,
    ID3NoHeaderError,
    TIT2,
    TPE1,
)


class DownloadCancelled(Exception):
    pass


def _json(data):
    return json.dumps(data, ensure_ascii=False)


def _clean_text(value, fallback):
    if value is None:
        return fallback

    text = str(value).strip()
    return text if text else fallback


def _join_people(value):
    if not isinstance(value, list):
        return None

    people = []

    for item in value:
        text = str(item or "").strip()

        if text:
            people.append(text)

    return ", ".join(people) if people else None


def _select_title(info):
    return _clean_text(
        info.get("track") or info.get("title"),
        "Unknown Title",
    )


def _select_artist(info):
    artist = info.get("artist")

    if not artist:
        artist = _join_people(info.get("artists"))

    if not artist:
        artist = info.get("creator")

    if not artist:
        artist = _join_people(info.get("creators"))

    if not artist:
        artist = info.get("uploader")

    if not artist:
        artist = info.get("uploader_id")

    return _clean_text(artist, "Unknown Artist")


def _number(value):
    try:
        return int(value or 0)
    except (TypeError, ValueError):
        return 0


_COVER_CANDIDATE_LIMIT = 8
_COVER_RETRY_DELAYS_SECONDS = (0.0, 0.8)
_COVER_MINIMUM_BYTES = 256
_COVER_DOWNLOAD_TIMEOUT_SECONDS = 15


def _normalize_http_url(value):
    text = str(value or "").strip()

    if not text:
        return None

    parsed = urllib.parse.urlparse(text)

    if parsed.scheme not in ("http", "https") or not parsed.netloc:
        return None

    return text


def _youtube_video_id(info, source_url):
    video_id = str(info.get("id") or "").strip()

    if video_id:
        return video_id

    parsed = urllib.parse.urlparse(str(source_url or "").strip())
    host = parsed.netloc.lower().split(":", 1)[0]

    if host in ("youtu.be", "www.youtu.be"):
        return parsed.path.strip("/").split("/", 1)[0]

    if host.endswith("youtube.com"):
        query_id = urllib.parse.parse_qs(parsed.query).get("v")

        if query_id:
            return str(query_id[0]).strip()

        path_parts = [
            part
            for part in parsed.path.split("/")
            if part
        ]

        if len(path_parts) >= 2 and path_parts[0] in (
            "shorts",
            "live",
            "embed",
            "v",
        ):
            return path_parts[1]

    return ""


def _thumbnail_urls(info, preferred_url=None, source_url=None):
    ranked = []
    thumbnails = info.get("thumbnails") or []

    for thumbnail in thumbnails:
        if not isinstance(thumbnail, dict):
            continue

        url = _normalize_http_url(thumbnail.get("url"))

        if not url:
            continue

        width = _number(thumbnail.get("width"))
        height = _number(thumbnail.get("height"))
        preference = _number(thumbnail.get("preference"))

        ranked.append(
            (
                preference,
                width * height,
                width,
                height,
                url,
            )
        )

    ranked.sort(reverse=True)

    ranked_urls = [
        item[4]
        for item in ranked
    ]

    candidates = [
        _normalize_http_url(preferred_url),
        _normalize_http_url(info.get("thumbnail")),
    ]

    candidates.extend(ranked_urls[:2])

    video_id = _youtube_video_id(info, source_url)

    if video_id:
        encoded_video_id = urllib.parse.quote(
            video_id,
            safe="",
        )

        candidates.extend(
            [
                f"https://i.ytimg.com/vi/{encoded_video_id}/maxresdefault.jpg",
                f"https://i.ytimg.com/vi/{encoded_video_id}/sddefault.jpg",
                f"https://i.ytimg.com/vi/{encoded_video_id}/hqdefault.jpg",
                f"https://i.ytimg.com/vi/{encoded_video_id}/mqdefault.jpg",
            ]
        )

    candidates.extend(ranked_urls[2:])

    unique = []
    seen = set()

    for candidate in candidates:
        normalized = _normalize_http_url(candidate)

        if not normalized or normalized in seen:
            continue

        seen.add(normalized)
        unique.append(normalized)

        if len(unique) >= _COVER_CANDIDATE_LIMIT:
            break

    return unique


def _best_thumbnail_url(info, source_url=None):
    candidates = _thumbnail_urls(
        info,
        source_url=source_url,
    )

    return candidates[0] if candidates else None


def _ssl_context():
    return ssl.create_default_context(cafile=certifi.where())


def _detect_cover_format(path):
    if not path or not os.path.isfile(path):
        return None

    with open(path, "rb") as image_file:
        header = image_file.read(32)

    if header.startswith(b"\xff\xd8\xff"):
        return "jpeg"

    if header.startswith(b"\x89PNG\r\n\x1a\n"):
        return "png"

    if (
        len(header) >= 12
        and header.startswith(b"RIFF")
        and header[8:12] == b"WEBP"
    ):
        return "webp"

    return None


def _remove_file_quietly(path):
    try:
        if path and os.path.exists(path):
            os.remove(path)
    except Exception:
        pass


def _download_cover(
    url,
    destination,
    check_cancelled=None,
):
    last_error = None
    temporary_path = destination + ".part"

    for retry_index, delay_seconds in enumerate(
        _COVER_RETRY_DELAYS_SECONDS,
        start=1,
    ):
        if check_cancelled:
            check_cancelled()

        if delay_seconds > 0:
            time.sleep(delay_seconds)

        _remove_file_quietly(temporary_path)

        request = urllib.request.Request(
            url,
            headers={
                "User-Agent": (
                    "Mozilla/5.0 (Linux; Android 14) "
                    "AppleWebKit/537.36 Chrome/122 Mobile Safari/537.36"
                ),
                "Accept": (
                    "image/jpeg,image/png,image/webp,image/*;q=0.8,"
                    "*/*;q=0.1"
                ),
                "Accept-Language": "vi-VN,vi;q=0.9,en;q=0.8",
                "Cache-Control": "no-cache",
                "Referer": "https://www.youtube.com/",
            },
        )

        try:
            with urllib.request.urlopen(
                request,
                timeout=_COVER_DOWNLOAD_TIMEOUT_SECONDS,
                context=_ssl_context(),
            ) as response:
                with open(temporary_path, "wb") as output:
                    while True:
                        if check_cancelled:
                            check_cancelled()

                        chunk = response.read(64 * 1024)

                        if not chunk:
                            break

                        output.write(chunk)

            if not os.path.isfile(temporary_path):
                raise RuntimeError("Không tạo được file ảnh bìa")

            if os.path.getsize(temporary_path) < _COVER_MINIMUM_BYTES:
                raise RuntimeError("File ảnh bìa quá nhỏ")

            cover_format = _detect_cover_format(temporary_path)

            if not cover_format:
                raise RuntimeError(
                    "Phản hồi ảnh bìa không phải JPEG, PNG hoặc WebP"
                )

            _remove_file_quietly(destination)
            os.replace(temporary_path, destination)

            return cover_format

        except DownloadCancelled:
            _remove_file_quietly(temporary_path)
            raise

        except Exception as error:
            last_error = error
            _remove_file_quietly(temporary_path)

            if retry_index >= len(_COVER_RETRY_DELAYS_SECONDS):
                break

    raise RuntimeError(
        str(last_error or "Không tải được ảnh bìa")
    )


def _download_cover_candidates(
    urls,
    destination,
    check_cancelled,
):
    errors = []

    for index, url in enumerate(urls, start=1):
        if check_cancelled:
            check_cancelled()

        try:
            _download_cover(
                url,
                destination,
                check_cancelled=check_cancelled,
            )

            return (
                os.path.abspath(destination),
                None,
            )

        except DownloadCancelled:
            raise

        except Exception as error:
            errors.append(
                f"#{index}: {str(error)}"
            )

    _remove_file_quietly(destination)

    if not urls:
        return (
            None,
            "Video không cung cấp địa chỉ ảnh bìa",
        )

    details = "; ".join(errors[-3:])

    return (
        None,
        (
            "Không tải được ảnh bìa sau "
            f"{len(urls)} địa chỉ và nhiều lần thử"
            + (f": {details}" if details else "")
        ),
    )

def extract_video_info(url):
    try:
        normalized_url = str(url or "").strip()

        if not normalized_url:
            raise ValueError("URL không được để trống")

        options = {
            "quiet": True,
            "no_warnings": True,
            "noplaylist": True,
            "extract_flat": False,
            "skip_download": True,
            "socket_timeout": 30,
            "retries": 3,
            "fragment_retries": 3,
        }

        with yt_dlp.YoutubeDL(options) as downloader:
            info = downloader.extract_info(
                normalized_url,
                download=False,
            )

        result = {
            "success": True,
            "title": _select_title(info),
            "artist": _select_artist(info),
            "thumbnailUrl": _best_thumbnail_url(
                info,
                source_url=normalized_url,
            ),
            "durationSeconds": _number(info.get("duration")),
        }

        return _json(result)

    except Exception as error:
        return _json(
            {
                "success": False,
                "error": str(error),
            }
        )


def download_audio(
    url,
    output_dir,
    job_id,
    callback,
    preferred_thumbnail_url=None,
):
    try:
        normalized_url = str(url or "").strip()
        normalized_output_dir = os.path.abspath(str(output_dir))
        normalized_job_id = str(job_id)

        if not normalized_url:
            raise ValueError("URL không được để trống")

        os.makedirs(normalized_output_dir, exist_ok=True)

        def check_cancelled():
            try:
                if callback.isCancelled():
                    raise DownloadCancelled("CANCELLED")
            except DownloadCancelled:
                raise
            except Exception:
                return

        def progress_hook(data):
            check_cancelled()

            status = data.get("status")

            if status == "downloading":
                total = (
                    data.get("total_bytes")
                    or data.get("total_bytes_estimate")
                    or 0
                )

                downloaded = data.get("downloaded_bytes") or 0
                speed = data.get("speed") or 0
                eta = data.get("eta") or 0

                percent = 0

                if total:
                    percent = int(downloaded * 100 / total)
                    percent = max(0, min(100, percent))

                try:
                    callback.onProgress(
                        int(percent),
                        int(downloaded),
                        int(total),
                        int(speed),
                        int(eta),
                    )
                except Exception:
                    pass

            elif status == "finished":
                try:
                    callback.onProgress(
                        100,
                        int(data.get("downloaded_bytes") or 0),
                        int(
                            data.get("total_bytes")
                            or data.get("downloaded_bytes")
                            or 0
                        ),
                        0,
                        0,
                    )
                except Exception:
                    pass

        output_template = os.path.join(
            normalized_output_dir,
            f"{normalized_job_id}_audio.%(ext)s",
        )

        options = {
            "format": "bestaudio/best",
            "outtmpl": output_template,
            "noplaylist": True,
            "quiet": True,
            "no_warnings": True,
            "progress_hooks": [progress_hook],
            "socket_timeout": 30,
            "retries": 5,
            "fragment_retries": 5,
            "continuedl": True,
            "overwrites": True,
        }

        with yt_dlp.YoutubeDL(options) as downloader:
            info = downloader.extract_info(
                normalized_url,
                download=True,
            )

        check_cancelled()

        audio_path = None

        requested_downloads = info.get("requested_downloads") or []

        for requested in requested_downloads:
            if not isinstance(requested, dict):
                continue

            candidate = requested.get("filepath")

            if candidate and os.path.isfile(candidate):
                audio_path = os.path.abspath(candidate)
                break

        if not audio_path:
            candidates = glob.glob(
                os.path.join(
                    normalized_output_dir,
                    f"{normalized_job_id}_audio.*",
                )
            )

            candidates = [
                candidate
                for candidate in candidates
                if os.path.isfile(candidate)
                and not candidate.endswith(".part")
                and not candidate.endswith(".ytdl")
                and not candidate.endswith(".tmp")
            ]

            if candidates:
                candidates.sort(
                    key=lambda candidate: os.path.getsize(candidate),
                    reverse=True,
                )
                audio_path = os.path.abspath(candidates[0])

        if not audio_path:
            raise RuntimeError("Không tìm thấy file âm thanh đã tải")

        if not os.path.isfile(audio_path):
            raise RuntimeError("File âm thanh không tồn tại")

        if os.path.getsize(audio_path) <= 0:
            raise RuntimeError("File âm thanh bị rỗng")

        check_cancelled()

        raw_cover_path = os.path.join(
            normalized_output_dir,
            f"{normalized_job_id}_cover.raw",
        )

        thumbnail_urls = _thumbnail_urls(
            info,
            preferred_url=preferred_thumbnail_url,
            source_url=normalized_url,
        )

        cover_path, cover_warning = _download_cover_candidates(
            thumbnail_urls,
            raw_cover_path,
            check_cancelled,
        )

        return _json(
            {
                "success": True,
                "audioPath": audio_path,
                "coverPath": cover_path,
                "coverWarning": cover_warning,
            }
        )

    except DownloadCancelled:
        return _json(
            {
                "success": False,
                "error": "CANCELLED",
            }
        )

    except Exception as error:
        return _json(
            {
                "success": False,
                "error": str(error),
            }
        )


def _remove_id3v1_if_present(mp3_path):
    size = os.path.getsize(mp3_path)

    if size < 128:
        return

    with open(mp3_path, "rb+") as audio_file:
        audio_file.seek(-128, os.SEEK_END)
        marker = audio_file.read(3)

        if marker == b"TAG":
            audio_file.truncate(size - 128)


def _remove_existing_tags(mp3_path):
    try:
        existing = ID3(mp3_path)

        existing.delete(
            mp3_path,
            delete_v1=True,
            delete_v2=True,
        )

    except ID3NoHeaderError:
        pass

    _remove_id3v1_if_present(mp3_path)


def _is_jpeg(path):
    if not path or not os.path.isfile(path):
        return False

    with open(path, "rb") as image_file:
        signature = image_file.read(3)

    return signature == b"\xff\xd8\xff"


def write_id3_tags(mp3_path, title, artist, cover_path):
    try:
        normalized_mp3_path = os.path.abspath(str(mp3_path))

        if not os.path.isfile(normalized_mp3_path):
            raise RuntimeError("File MP3 không tồn tại")

        if os.path.getsize(normalized_mp3_path) <= 0:
            raise RuntimeError("File MP3 bị rỗng")

        normalized_title = _clean_text(
            title,
            "Unknown Title",
        )

        normalized_artist = _clean_text(
            artist,
            "Unknown Artist",
        )

        _remove_existing_tags(normalized_mp3_path)

        tags = ID3()

        tags.add(
            TIT2(
                encoding=1,
                text=[normalized_title],
            )
        )

        tags.add(
            TPE1(
                encoding=1,
                text=[normalized_artist],
            )
        )

        cover_embedded = False
        warning = None

        if cover_path:
            normalized_cover_path = os.path.abspath(str(cover_path))

            if _is_jpeg(normalized_cover_path):
                with open(normalized_cover_path, "rb") as cover_file:
                    cover_bytes = cover_file.read()

                if cover_bytes:
                    tags.add(
                        APIC(
                            encoding=0,
                            mime="image/jpeg",
                            type=3,
                            desc="Cover",
                            data=cover_bytes,
                        )
                    )

                    cover_embedded = True

            else:
                warning = "Ảnh bìa không phải JPEG hợp lệ"

        else:
            warning = "Không có ảnh bìa để nhúng"

        tags.save(
            normalized_mp3_path,
            v1=0,
            v2_version=3,
        )

        verification = ID3(
            normalized_mp3_path,
            translate=False,
        )

        frame_ids = sorted(
            {
                frame.FrameID
                for frame in verification.values()
            }
        )

        allowed_frames = {
            "TIT2",
            "TPE1",
            "APIC",
            "USLT",
        }

        unexpected_frames = [
            frame_id
            for frame_id in frame_ids
            if frame_id not in allowed_frames
        ]

        if unexpected_frames:
            raise RuntimeError(
                "Phát hiện tag không được phép: "
                + ", ".join(unexpected_frames)
            )

        title_frames = verification.getall("TIT2")
        artist_frames = verification.getall("TPE1")
        cover_frames = verification.getall("APIC")

        if not title_frames:
            raise RuntimeError("Không ghi được TIT2")

        if not artist_frames:
            raise RuntimeError("Không ghi được TPE1")

        if str(title_frames[0]) != normalized_title:
            raise RuntimeError("Title sau khi ghi không khớp")

        if str(artist_frames[0]) != normalized_artist:
            raise RuntimeError("Artist sau khi ghi không khớp")

        if cover_embedded:
            if not cover_frames:
                raise RuntimeError("Không xác minh được APIC")

            first_cover = cover_frames[0]

            if first_cover.mime != "image/jpeg":
                raise RuntimeError("APIC không có MIME image/jpeg")

            if first_cover.type != 3:
                raise RuntimeError("APIC không phải Front Cover")

        version = verification.version

        if not version or len(version) < 2 or version[0:2] != (2, 3):
            raise RuntimeError(
                "Tag không phải ID3v2.3"
            )

        return _json(
            {
                "success": True,
                "coverEmbedded": cover_embedded,
                "warning": warning,
                "frames": frame_ids,
                "id3Version": ".".join(
                    str(part)
                    for part in version
                ),
            }
        )

    except Exception as error:
        return _json(
            {
                "success": False,
                "error": str(error),
                "traceback": traceback.format_exc(),
            }
        )
def read_mp3_editor_tags(mp3_path, cover_output_path):
    try:
        from mutagen.id3 import ID3, ID3NoHeaderError

        normalized_path = os.path.abspath(str(mp3_path))
        normalized_cover_path = os.path.abspath(str(cover_output_path))

        if not os.path.isfile(normalized_path):
            raise RuntimeError("File MP3 không tồn tại")

        try:
            tags = ID3(
                normalized_path,
                translate=False,
            )
        except ID3NoHeaderError:
            tags = ID3()

        def first_text(frame_id):
            frames = tags.getall(frame_id)

            if not frames:
                return ""

            frame = frames[0]
            values = getattr(frame, "text", [])

            if not values:
                return ""

            return str(values[0]).strip()

        title = first_text("TIT2")
        artist = first_text("TPE1")
        album = first_text("TALB")

        raw_year = (
            first_text("TYER")
            or first_text("TDRC")
            or first_text("TYE")
        )

        year_match = re.search(
            r"(?<!\d)(\d{4})(?!\d)",
            raw_year or "",
        )

        year = (
            year_match.group(1)
            if year_match
            else ""
        )

        cover_path = None
        cover_frames = tags.getall("APIC")

        if cover_frames:
            cover = cover_frames[0]
            cover_bytes = bytes(cover.data or b"")

            if cover_bytes:
                os.makedirs(
                    os.path.dirname(normalized_cover_path),
                    exist_ok=True,
                )

                with open(normalized_cover_path, "wb") as output:
                    output.write(cover_bytes)

                if os.path.getsize(normalized_cover_path) > 0:
                    cover_path = normalized_cover_path

        return _json(
            {
                "success": True,
                "title": title,
                "artist": artist,
                "album": album,
                "year": year,
                "coverPath": cover_path,
            }
        )

    except Exception as error:
        return _json(
            {
                "success": False,
                "error": str(error),
            }
        )


def update_mp3_editor_tags(
    mp3_path,
    title,
    artist,
    album,
    year="",
):
    try:
        from mutagen.id3 import (
            APIC,
            ID3,
            ID3NoHeaderError,
            TALB,
            TIT2,
            TPE1,
            TYER,
            USLT,
        )

        normalized_path = os.path.abspath(str(mp3_path))

        if not os.path.isfile(normalized_path):
            raise RuntimeError("File MP3 không tồn tại")

        normalized_title = str(title or "").strip()
        normalized_artist = str(artist or "").strip()
        normalized_album = str(album or "").strip()

        normalized_year = str(
            year or ""
        ).strip()

        if (
            normalized_year
            and not re.fullmatch(
                r"\d{4}",
                normalized_year,
            )
        ):
            raise RuntimeError(
                "Year phải gồm đúng 4 chữ số"
            )

        if not normalized_title:
            raise RuntimeError("Title không được để trống")

        if not normalized_artist:
            raise RuntimeError("Artist không được để trống")

        preserved_cover = None
        preserved_lyrics = []

        try:
            current_tags = ID3(
                normalized_path,
                translate=False,
            )

            covers = current_tags.getall("APIC")

            preserved_lyrics = [
                {
                    "lang": str(
                        getattr(frame, "lang", "und")
                        or "und"
                    ),
                    "desc": str(
                        getattr(frame, "desc", "")
                        or ""
                    ),
                    "text": str(
                        getattr(frame, "text", "")
                        or ""
                    ),
                }
                for frame in current_tags.getall("USLT")
                if str(
                    getattr(frame, "text", "")
                    or ""
                ).strip()
            ]

            if covers:
                original_cover = covers[0]

                preserved_cover = {
                    "mime": (
                        original_cover.mime
                        or "image/jpeg"
                    ),
                    "data": bytes(
                        original_cover.data
                        or b""
                    ),
                }

            current_tags.delete(
                normalized_path,
                delete_v1=True,
                delete_v2=True,
            )

        except ID3NoHeaderError:
            pass

        _remove_id3v1_if_present(
            normalized_path
        )

        tags = ID3()

        tags.add(
            TIT2(
                encoding=1,
                text=[normalized_title],
            )
        )

        tags.add(
            TPE1(
                encoding=1,
                text=[normalized_artist],
            )
        )

        if normalized_album:
            tags.add(
                TALB(
                    encoding=1,
                    text=[normalized_album],
                )
            )

        if normalized_year:
            tags.add(
                TYER(
                    encoding=1,
                    text=[normalized_year],
                )
            )

        if (
            preserved_cover
            and preserved_cover["data"]
        ):
            tags.add(
                APIC(
                    encoding=0,
                    mime=preserved_cover["mime"],
                    type=3,
                    desc="Cover",
                    data=preserved_cover["data"],
                )
            )

        for lyrics_frame in preserved_lyrics:
            tags.add(
                USLT(
                    encoding=1,
                    lang=(
                        lyrics_frame["lang"][:3]
                        if len(
                            lyrics_frame["lang"]
                        ) >= 3
                        else "und"
                    ),
                    desc=lyrics_frame["desc"],
                    text=lyrics_frame["text"],
                )
            )

        tags.save(
            normalized_path,
            v1=0,
            v2_version=3,
        )

        verification = ID3(
            normalized_path,
            translate=False,
        )

        version = verification.version

        if (
            not version
            or version[0:2] != (2, 3)
        ):
            raise RuntimeError(
                "Tag không phải ID3v2.3"
            )

        allowed_frames = {
            "TIT2",
            "TPE1",
            "TALB",
            "TYER",
            "APIC",
            "USLT",
        }

        unexpected_frames = sorted(
            {
                frame.FrameID
                for frame in verification.values()
                if frame.FrameID not in allowed_frames
            }
        )

        if unexpected_frames:
            raise RuntimeError(
                "Phát hiện tag không được phép: "
                + ", ".join(unexpected_frames)
            )

        return _json(
            {
                "success": True,
                "id3Version": ".".join(
                    str(part)
                    for part in version
                ),
            }
        )

    except Exception as error:
        return _json(
            {
                "success": False,
                "error": str(error),
            }
        )

def read_mp3_lyrics(mp3_path):
    try:
        from mutagen.id3 import (
            ID3,
            ID3NoHeaderError,
        )

        normalized_path = os.path.abspath(
            str(mp3_path)
        )

        if not os.path.isfile(normalized_path):
            raise RuntimeError(
                "File MP3 không tồn tại"
            )

        try:
            tags = ID3(
                normalized_path,
                translate=False,
            )
        except ID3NoHeaderError:
            return _json(
                {
                    "success": True,
                    "lyrics": "",
                    "language": "und",
                    "description": "",
                }
            )

        frames = tags.getall("USLT")

        if not frames:
            return _json(
                {
                    "success": True,
                    "lyrics": "",
                    "language": "und",
                    "description": "",
                }
            )

        empty_description_frame = next(
            (
                frame
                for frame in frames
                if not str(
                    getattr(
                        frame,
                        "desc",
                        "",
                    )
                    or ""
                ).strip()
            ),
            None,
        )

        legacy_getmp3_frame = next(
            (
                frame
                for frame in frames
                if str(
                    getattr(
                        frame,
                        "desc",
                        "",
                    )
                    or ""
                ).strip().lower()
                == "getmp3 lyrics"
            ),
            None,
        )

        selected = (
            empty_description_frame
            or legacy_getmp3_frame
            or frames[0]
        )

        return _json(
            {
                "success": True,
                "lyrics": str(
                    getattr(
                        selected,
                        "text",
                        "",
                    )
                    or ""
                ),
                "language": str(
                    getattr(
                        selected,
                        "lang",
                        "und",
                    )
                    or "und"
                ),
                "description": str(
                    getattr(
                        selected,
                        "desc",
                        "",
                    )
                    or ""
                ),
            }
        )

    except Exception as error:
        return _json(
            {
                "success": False,
                "error": str(error),
            }
        )


def update_mp3_lyrics(
    mp3_path,
    lyrics,
    language="eng",
):
    try:
        from mutagen.id3 import (
            ID3,
            ID3NoHeaderError,
            USLT,
        )

        normalized_path = os.path.abspath(
            str(mp3_path)
        )

        if not os.path.isfile(normalized_path):
            raise RuntimeError(
                "File MP3 không tồn tại"
            )

        normalized_lyrics = str(
            lyrics or ""
        ).replace(
            "\r\n",
            "\n",
        ).replace(
            "\r",
            "\n",
        ).strip()

        if not normalized_lyrics:
            raise RuntimeError(
                "Nội dung lyrics đang trống"
            )

        normalized_language = str(
            language or "eng"
        ).strip().lower()

        if len(normalized_language) != 3:
            normalized_language = "eng"

        try:
            tags = ID3(
                normalized_path,
                translate=False,
            )
        except ID3NoHeaderError:
            tags = ID3()

        tags.delall("USLT")

        # Description để rỗng nhằm tương thích với
        # nhiều trình phát và trình sửa tag Android.
        tags.add(
            USLT(
                encoding=1,
                lang=normalized_language,
                desc="",
                text=normalized_lyrics,
            )
        )

        tags.save(
            normalized_path,
            v1=0,
            v2_version=3,
        )

        verification = ID3(
            normalized_path,
            translate=False,
        )

        version = verification.version

        if (
            not version
            or version[0:2] != (2, 3)
        ):
            raise RuntimeError(
                "Tag không phải ID3v2.3"
            )

        lyrics_frames = verification.getall(
            "USLT"
        )

        if not lyrics_frames:
            raise RuntimeError(
                "Không xác minh được USLT"
            )

        selected = next(
            (
                frame
                for frame in lyrics_frames
                if not str(
                    getattr(
                        frame,
                        "desc",
                        "",
                    )
                    or ""
                ).strip()
            ),
            lyrics_frames[0],
        )

        verified_text = str(
            getattr(
                selected,
                "text",
                "",
            )
            or ""
        ).replace(
            "\r\n",
            "\n",
        ).replace(
            "\r",
            "\n",
        ).strip()

        verified_description = str(
            getattr(
                selected,
                "desc",
                "",
            )
            or ""
        )

        if verified_text != normalized_lyrics:
            raise RuntimeError(
                "Lyrics đọc lại không khớp"
            )

        if verified_description:
            raise RuntimeError(
                "Description của USLT chưa rỗng"
            )

        return _json(
            {
                "success": True,
                "id3Version": ".".join(
                    str(part)
                    for part in version
                ),
                "language": normalized_language,
                "description": verified_description,
                "frameCount": len(lyrics_frames),
            }
        )

    except Exception as error:
        return _json(
            {
                "success": False,
                "error": str(error),
                "traceback": traceback.format_exc(),
            }
        )
