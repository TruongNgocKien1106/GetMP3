package com.ngoctien.getmp3.compare

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

class CompareAudioPreview(
    context: Context,
    private val onPlayingChanged:
        (String?) -> Unit,
    private val onError:
        (String) -> Unit
) {

    private val applicationContext =
        context.applicationContext

    private var player:
        MediaPlayer? =
        null

    private var currentUri:
        String? =
        null

    private var prepared =
        false


    // ========================================================
    // TOGGLE
    // ========================================================

    fun toggle(
        uri: String
    ) {

        val existing =
            player

        if (
            currentUri ==
                uri &&
            prepared &&
            existing !=
                null
        ) {

            if (
                runCatching {
                    existing.isPlaying
                }
                    .getOrDefault(
                        false
                    )
            ) {

                existing.pause()

                onPlayingChanged(
                    null
                )

            } else {

                existing.start()

                onPlayingChanged(
                    uri
                )
            }

            return
        }

        switchTo(
            uri
        )
    }


    // ========================================================
    // SWITCH A/B AT SAME POSITION
    // ========================================================

    private fun switchTo(
        uri: String
    ) {

        val oldPosition =
            if (
                prepared &&
                player != null
            ) {
                runCatching {
                    player
                        ?.currentPosition
                        ?: 0
                }
                    .getOrDefault(
                        0
                    )
            } else {
                0
            }

        releasePlayer(
            notify = false
        )

        val newPlayer =
            MediaPlayer()

        player =
            newPlayer

        currentUri =
            uri

        prepared =
            false

        try {

            newPlayer.setDataSource(
                applicationContext,
                Uri.parse(
                    uri
                )
            )

            newPlayer
                .setOnPreparedListener {
                        preparedPlayer ->

                    prepared =
                        true

                    if (
                        oldPosition >
                        0
                    ) {

                        val safePosition =
                            if (
                                preparedPlayer
                                    .duration >
                                    500
                            ) {
                                oldPosition
                                    .coerceAtMost(
                                        preparedPlayer
                                            .duration -
                                            250
                                    )
                            } else {
                                0
                            }

                        runCatching {
                            preparedPlayer
                                .seekTo(
                                    safePosition
                                )
                        }
                    }

                    preparedPlayer.start()

                    onPlayingChanged(
                        uri
                    )
                }

            newPlayer
                .setOnCompletionListener {

                    onPlayingChanged(
                        null
                    )
                }

            newPlayer
                .setOnErrorListener {
                        _,
                        what,
                        extra ->

                    onError(
                        "Không phát được file " +
                            "($what/$extra)"
                    )

                    releasePlayer(
                        notify = true
                    )

                    true
                }

            newPlayer.prepareAsync()

        } catch (
            exception: Exception
        ) {

            onError(
                exception.message
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: "Không phát được file"
            )

            releasePlayer(
                notify = true
            )
        }
    }


    // ========================================================
    // STOP
    // ========================================================

    fun stop() {

        releasePlayer(
            notify = true
        )
    }


    fun release() {

        releasePlayer(
            notify = false
        )
    }


    private fun releasePlayer(
        notify: Boolean
    ) {

        val old =
            player

        player =
            null

        currentUri =
            null

        prepared =
            false

        if (old != null) {

            runCatching {
                old.stop()
            }

            runCatching {
                old.reset()
            }

            runCatching {
                old.release()
            }
        }

        if (notify) {

            onPlayingChanged(
                null
            )
        }
    }
}