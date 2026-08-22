package com.ngoctien.getmp3.youtube

data class YouTubeSearchResult(
    val videoId: String,
    val title: String,
    val channel: String,
    val durationSeconds: Long?,
    val thumbnailUrl: String?,
    val webpageUrl: String
) {
    val effectiveThumbnailUrl: String
        get() {
            return thumbnailUrl
                ?.trim()
                ?.takeIf {
                    it.startsWith(
                        "http://"
                    ) ||
                        it.startsWith(
                            "https://"
                        )
                }
                ?: (
                    "https://i.ytimg.com/vi/" +
                        videoId +
                        "/hqdefault.jpg"
                    )
        }

    val formattedDuration: String
        get() {
            val totalSeconds =
                durationSeconds
                    ?.takeIf {
                        it >= 0L
                    }
                    ?: return "--:--"

            val hours =
                totalSeconds / 3600L

            val minutes =
                (
                    totalSeconds %
                        3600L
                    ) / 60L

            val seconds =
                totalSeconds % 60L

            return if (hours > 0L) {
                "%d:%02d:%02d".format(
                    hours,
                    minutes,
                    seconds
                )
            } else {
                "%d:%02d".format(
                    minutes,
                    seconds
                )
            }
        }
}