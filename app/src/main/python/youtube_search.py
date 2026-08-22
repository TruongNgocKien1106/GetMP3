from __future__ import annotations

import json
from typing import Any

import yt_dlp


MAX_SEARCH_RESULTS = 200


def search_youtube_page_json(
    query: str,
    offset: int = 0,
    limit: int = 10,
) -> str:
    """
    Search one YouTube result page without downloading media.

    The function fetches one additional raw result to determine
    whether another page may exist.
    """

    clean_query = " ".join(str(query or "").split()).strip()

    if not clean_query:
        raise ValueError("Search query is empty.")

    safe_offset = max(
        0,
        min(
            int(offset),
            MAX_SEARCH_RESULTS - 1,
        ),
    )

    safe_limit = max(
        1,
        min(
            int(limit),
            20,
        ),
    )

    requested_total = min(
        safe_offset + safe_limit + 1,
        MAX_SEARCH_RESULTS,
    )

    options: dict[str, Any] = {
        "quiet": True,
        "no_warnings": True,
        "skip_download": True,
        "extract_flat": "in_playlist",
        "playlistend": requested_total,
        "socket_timeout": 20,
        "retries": 1,
        "fragment_retries": 1,
        "ignoreerrors": True,
        "noplaylist": True,
    }

    search_target = (
        f"ytsearch{requested_total}:{clean_query}"
    )

    with yt_dlp.YoutubeDL(options) as downloader:
        information = downloader.extract_info(
            search_target,
            download=False,
        )

    raw_entries: list[Any] = []

    if isinstance(information, dict):
        candidate_entries = (
            information.get("entries")
            or []
        )

        raw_entries = list(candidate_entries)

    page_end = safe_offset + safe_limit

    page_entries = raw_entries[
        safe_offset:page_end
    ]

    results: list[dict[str, Any]] = []

    for entry in page_entries:
        result = _convert_entry(entry)

        if result is not None:
            results.append(result)

    has_more = (
        len(raw_entries) > page_end
        and page_end < MAX_SEARCH_RESULTS
    )

    payload = {
        "items": results,
        "next_offset": page_end,
        "has_more": has_more,
    }

    return json.dumps(
        payload,
        ensure_ascii=False,
        separators=(",", ":"),
    )


def search_youtube_json(
    query: str,
    limit: int = 10,
) -> str:
    """
    Compatibility function for older callers.
    """

    page_json = search_youtube_page_json(
        query=query,
        offset=0,
        limit=limit,
    )

    page = json.loads(page_json)

    return json.dumps(
        page.get("items") or [],
        ensure_ascii=False,
        separators=(",", ":"),
    )


def _convert_entry(
    entry: Any,
) -> dict[str, Any] | None:
    if not isinstance(entry, dict):
        return None

    video_id = str(
        entry.get("id")
        or ""
    ).strip()

    title = str(
        entry.get("title")
        or ""
    ).strip()

    if not video_id or not title:
        return None

    live_status = str(
        entry.get("live_status")
        or ""
    ).strip().lower()

    if live_status in {
        "is_live",
        "is_upcoming",
        "post_live",
    }:
        return None

    webpage_url = str(
        entry.get("webpage_url")
        or entry.get("original_url")
        or ""
    ).strip()

    if not webpage_url.startswith(
        ("http://", "https://")
    ):
        webpage_url = (
            "https://www.youtube.com/watch"
            f"?v={video_id}"
        )

    channel = str(
        entry.get("channel")
        or entry.get("uploader")
        or entry.get("channel_id")
        or ""
    ).strip()

    duration = entry.get("duration")

    try:
        duration_value = (
            int(duration)
            if duration is not None
            else None
        )
    except (TypeError, ValueError):
        duration_value = None

    return {
        "video_id": video_id,
        "title": title,
        "channel": channel,
        "duration": duration_value,
        "thumbnail_url": _find_thumbnail(entry),
        "webpage_url": webpage_url,
    }


def _find_thumbnail(
    entry: dict[str, Any],
) -> str:
    thumbnail = str(
        entry.get("thumbnail")
        or ""
    ).strip()

    if thumbnail.startswith(
        ("http://", "https://")
    ):
        return thumbnail

    thumbnails = entry.get("thumbnails")

    if isinstance(thumbnails, list):
        for item in reversed(thumbnails):
            if not isinstance(item, dict):
                continue

            url = str(
                item.get("url")
                or ""
            ).strip()

            if url.startswith(
                ("http://", "https://")
            ):
                return url

    video_id = str(
        entry.get("id")
        or ""
    ).strip()

    if not video_id:
        return ""

    return (
        "https://i.ytimg.com/vi/"
        f"{video_id}/hqdefault.jpg"
    )