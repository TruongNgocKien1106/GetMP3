from __future__ import annotations

import json
from typing import Any

import yt_dlp


def search_youtube_json(
    query: str,
    limit: int = 10,
) -> str:
    """
    Search YouTube without downloading any media.

    Returns a JSON array containing:
    - video_id
    - title
    - channel
    - duration
    - thumbnail_url
    - webpage_url
    """

    clean_query = " ".join(str(query or "").split()).strip()

    if not clean_query:
        raise ValueError("Search query is empty.")

    safe_limit = max(1, min(int(limit), 15))

    options: dict[str, Any] = {
        "quiet": True,
        "no_warnings": True,
        "skip_download": True,
        "extract_flat": "in_playlist",
        "playlistend": safe_limit,
        "socket_timeout": 20,
        "retries": 1,
        "fragment_retries": 1,
        "ignoreerrors": True,
        "noplaylist": True,
    }

    search_target = f"ytsearch{safe_limit}:{clean_query}"

    with yt_dlp.YoutubeDL(options) as downloader:
        information = downloader.extract_info(
            search_target,
            download=False,
        )

    entries = []

    if isinstance(information, dict):
        entries = information.get("entries") or []

    results: list[dict[str, Any]] = []

    for entry in entries:
        if not isinstance(entry, dict):
            continue

        video_id = str(entry.get("id") or "").strip()
        title = str(entry.get("title") or "").strip()

        if not video_id or not title:
            continue

        live_status = str(entry.get("live_status") or "").strip().lower()

        if live_status in {
            "is_live",
            "is_upcoming",
            "post_live",
        }:
            continue

        webpage_url = str(
            entry.get("webpage_url")
            or entry.get("original_url")
            or ""
        ).strip()

        if not webpage_url.startswith(("http://", "https://")):
            webpage_url = (
                f"https://www.youtube.com/watch?v={video_id}"
            )

        channel = str(
            entry.get("channel")
            or entry.get("uploader")
            or entry.get("channel_id")
            or ""
        ).strip()

        duration = entry.get("duration")

        try:
            duration_value = int(duration) if duration is not None else None
        except (TypeError, ValueError):
            duration_value = None

        thumbnail_url = _find_thumbnail(entry)

        results.append(
            {
                "video_id": video_id,
                "title": title,
                "channel": channel,
                "duration": duration_value,
                "thumbnail_url": thumbnail_url,
                "webpage_url": webpage_url,
            }
        )

    return json.dumps(
        results,
        ensure_ascii=False,
        separators=(",", ":"),
    )


def _find_thumbnail(
    entry: dict[str, Any],
) -> str:
    thumbnail = str(entry.get("thumbnail") or "").strip()

    if thumbnail.startswith(("http://", "https://")):
        return thumbnail

    thumbnails = entry.get("thumbnails")

    if not isinstance(thumbnails, list):
        return ""

    for item in reversed(thumbnails):
        if not isinstance(item, dict):
            continue

        url = str(item.get("url") or "").strip()

        if url.startswith(("http://", "https://")):
            return url

    return ""