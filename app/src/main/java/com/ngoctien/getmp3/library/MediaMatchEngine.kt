package com.ngoctien.getmp3.library

import com.ngoctien.getmp3.data.IndexedMediaEntity
import com.ngoctien.getmp3.note.SongNameMatcher
import kotlin.math.abs

/**
 * Two-stage entity-resolution matcher:
 *
 * 1. Blocking / candidate generation using exact filename,
 *    rare title tokens, exact artist and a small length bucket.
 * 2. Expensive fuzzy scoring only for the short candidate list.
 */
class MediaMatchEngine(
    referenceSongs: List<IndexedMediaEntity>
) {

    companion object {
        const val DEFAULT_NEAR_THRESHOLD = 0.90

        private const val MAX_BLOCK_CANDIDATES = 120
        private const val MAX_FUZZY_CANDIDATES = 40
    }

    private val references =
        referenceSongs
            .filter {
                it.normalizedTitle.isNotBlank() ||
                    it.canonicalFileName.isNotBlank()
            }

    private val exactFileNameIndex =
        references.groupBy {
            it.canonicalFileName
        }

    private val exactArtistIndex =
        references
            .filter {
                it.normalizedArtist.isNotBlank()
            }
            .groupBy {
                it.normalizedArtist
            }

    private val tokenIndex:
        Map<String, List<IndexedMediaEntity>> =
        buildMap {
            val mutable =
                linkedMapOf<
                    String,
                    MutableList<IndexedMediaEntity>
                    >()

            references.forEach { song ->
                tokens(song).forEach { token ->
                    mutable
                        .getOrPut(token) {
                            mutableListOf()
                        }
                        .add(song)
                }
            }

            mutable.forEach { (token, songs) ->
                put(token, songs)
            }
        }

    /*
     * Character trigrams make blocking resilient to small typos where no
     * complete title token matches. Only rare grams are used for candidates.
     */
    private val trigramIndex:
        Map<String, List<IndexedMediaEntity>> =
        buildMap {
            val mutable =
                linkedMapOf<
                    String,
                    MutableList<IndexedMediaEntity>
                    >()

            references.forEach { song ->
                trigrams(
                    song.normalizedTitle
                ).forEach { gram ->
                    mutable
                        .getOrPut(gram) {
                            mutableListOf()
                        }
                        .add(song)
                }
            }

            mutable.forEach { (gram, songs) ->
                put(gram, songs)
            }
        }

    private val lengthBucketIndex =
        references.groupBy {
            lengthBucketKey(
                it.normalizedTitle
            )
        }

    fun findBest(
        current: IndexedMediaEntity,
        threshold: Double =
            DEFAULT_NEAR_THRESHOLD
    ): MediaMatchCandidate? {

        val exactCandidates =
            exactFileNameIndex[
                current.canonicalFileName
            ]
                .orEmpty()
                .filter {
                    it.uri != current.uri
                }

        if (exactCandidates.isNotEmpty()) {
            val best =
                exactCandidates.maxByOrNull {
                    exactTieBreakScore(
                        current,
                        it
                    )
                }
                ?: return null

            return MediaMatchCandidate(
                currentUri = current.uri,
                referenceUri = best.uri,
                score = 1.0,
                exactFileName = true
            )
        }

        val blocked =
            buildCandidateSet(current)

        if (blocked.isEmpty()) {
            return null
        }

        val shortlist =
            blocked
                .asSequence()
                .filter {
                    it.uri != current.uri
                }
                .map { candidate ->
                    candidate to
                        cheapBlockingScore(
                            current,
                            candidate
                        )
                }
                .sortedByDescending {
                    it.second
                }
                .take(
                    MAX_FUZZY_CANDIDATES
                )
                .map {
                    it.first
                }
                .toList()

        var bestSong:
            IndexedMediaEntity? =
            null

        var bestScore = 0.0

        shortlist.forEach { candidate ->
            val score =
                detailedScore(
                    current,
                    candidate
                )

            if (score > bestScore) {
                bestScore = score
                bestSong = candidate
            }
        }

        val winner =
            bestSong
                ?: return null

        if (bestScore < threshold) {
            return null
        }

        return MediaMatchCandidate(
            currentUri = current.uri,
            referenceUri = winner.uri,
            score = bestScore,
            exactFileName = false
        )
    }

    private fun buildCandidateSet(
        current: IndexedMediaEntity
    ): Set<IndexedMediaEntity> {

        val result =
            linkedSetOf<
                IndexedMediaEntity
                >()

        val titleTokens =
            tokens(current)

        /*
         * Rarest tokens carry more information than common tokens.
         * Example: "giấu", "kín" is more selective than "tình".
         */
        titleTokens
            .mapNotNull { token ->
                tokenIndex[token]
                    ?.let {
                        token to it
                    }
            }
            .sortedBy {
                it.second.size
            }
            .take(4)
            .forEach { (_, songs) ->
                result.addAll(songs)
            }

        /*
         * Rare trigrams are a second blocking route for misspellings or
         * punctuation differences which break exact token matching.
         */
        trigrams(
            current.normalizedTitle
        )
            .mapNotNull { gram ->
                trigramIndex[gram]
                    ?.let {
                        gram to it
                    }
            }
            .sortedBy {
                it.second.size
            }
            .take(6)
            .forEach { (_, songs) ->
                result.addAll(songs)
            }

        if (
            current.normalizedArtist
                .isNotBlank()
        ) {
            exactArtistIndex[
                current.normalizedArtist
            ]
                ?.let(
                    result::addAll
                )
        }

        lengthBucketIndex[
            lengthBucketKey(
                current.normalizedTitle
            )
        ]
            ?.let(
                result::addAll
            )

        if (
            result.size <=
            MAX_BLOCK_CANDIDATES
        ) {
            return result
        }

        return result
            .asSequence()
            .map {
                it to
                    cheapBlockingScore(
                        current,
                        it
                    )
            }
            .sortedByDescending {
                it.second
            }
            .take(
                MAX_BLOCK_CANDIDATES
            )
            .map {
                it.first
            }
            .toSet()
    }

    private fun cheapBlockingScore(
        left: IndexedMediaEntity,
        right: IndexedMediaEntity
    ): Double {

        val leftTokens =
            tokens(left)

        val rightTokens =
            tokens(right)

        val tokenScore =
            if (
                leftTokens.isEmpty() ||
                rightTokens.isEmpty()
            ) {
                0.0
            } else {
                val intersection =
                    leftTokens
                        .intersect(
                            rightTokens
                        )
                        .size

                val union =
                    leftTokens
                        .union(
                            rightTokens
                        )
                        .size

                intersection
                    .toDouble()
                    .div(
                        union.toDouble()
                    )
            }

        val maxLength =
            maxOf(
                left.normalizedTitle.length,
                right.normalizedTitle.length,
                1
            )

        val lengthScore =
            1.0 -
                abs(
                    left.normalizedTitle.length -
                        right.normalizedTitle.length
                )
                    .toDouble()
                    .div(
                        maxLength.toDouble()
                    )

        val trigramScore =
            diceCoefficient(
                trigrams(
                    left.normalizedTitle
                ),
                trigrams(
                    right.normalizedTitle
                )
            )

        val artistBoost =
            if (
                left.normalizedArtist
                    .isNotBlank() &&
                left.normalizedArtist ==
                right.normalizedArtist
            ) {
                1.0
            } else {
                0.0
            }

        return (
            tokenScore * 0.45 +
                trigramScore * 0.30 +
                lengthScore * 0.15 +
                artistBoost * 0.10
            )
            .coerceIn(
                0.0,
                1.0
            )
    }

    private fun detailedScore(
        left: IndexedMediaEntity,
        right: IndexedMediaEntity
    ): Double {

        val fields =
            mutableListOf<
                Pair<Double, Double>
                >()

        fields +=
            0.55 to
                SongNameMatcher
                    .similarity(
                        left.normalizedTitle,
                        right.normalizedTitle
                    )

        if (
            left.normalizedArtist.isNotBlank() &&
            right.normalizedArtist.isNotBlank()
        ) {
            fields +=
                0.25 to
                    SongNameMatcher
                        .similarity(
                            left.normalizedArtist,
                            right.normalizedArtist
                        )
        }

        fields +=
            0.15 to
                SongNameMatcher
                    .similarity(
                        left.normalizedFileName,
                        right.normalizedFileName
                    )

        if (
            left.normalizedAlbum.isNotBlank() &&
            right.normalizedAlbum.isNotBlank()
        ) {
            fields +=
                0.05 to
                    SongNameMatcher
                        .similarity(
                            left.normalizedAlbum,
                            right.normalizedAlbum
                        )
        }

        val weight =
            fields.sumOf {
                it.first
            }

        if (weight <= 0.0) {
            return 0.0
        }

        return fields
            .sumOf {
                it.first * it.second
            }
            .div(weight)
            .coerceIn(
                0.0,
                1.0
            )
    }

    private fun exactTieBreakScore(
        left: IndexedMediaEntity,
        right: IndexedMediaEntity
    ): Int {
        var score = 0

        if (
            left.normalizedArtist.isNotBlank() &&
            left.normalizedArtist ==
            right.normalizedArtist
        ) {
            score += 4
        }

        if (
            left.normalizedAlbum.isNotBlank() &&
            left.normalizedAlbum ==
            right.normalizedAlbum
        ) {
            score += 2
        }

        if (
            left.sizeBytes ==
            right.sizeBytes
        ) {
            score += 1
        }

        return score
    }

    private fun tokens(
        song: IndexedMediaEntity
    ): Set<String> {
        return song.titleTokens
            .split(' ')
            .asSequence()
            .map {
                it.trim()
            }
            .filter {
                it.length >= 2
            }
            .toSet()
    }

    private fun trigrams(
        value: String
    ): Set<String> {
        val clean =
            value
                .trim()
                .replace(
                    Regex("\\s+"),
                    " "
                )

        if (clean.isBlank()) {
            return emptySet()
        }

        if (clean.length <= 3) {
            return setOf(clean)
        }

        return buildSet {
            for (index in 0..clean.length - 3) {
                add(
                    clean.substring(
                        index,
                        index + 3
                    )
                )
            }
        }
    }

    private fun diceCoefficient(
        left: Set<String>,
        right: Set<String>
    ): Double {
        if (left.isEmpty() || right.isEmpty()) {
            return 0.0
        }

        val intersection =
            left.intersect(right).size

        return (
            2.0 * intersection.toDouble() /
                (left.size + right.size).toDouble()
            )
            .coerceIn(0.0, 1.0)
    }

    private fun lengthBucketKey(
        normalizedTitle: String
    ): String {
        val clean =
            normalizedTitle.trim()

        if (clean.isBlank()) {
            return "_"
        }

        val bucket =
            clean.length / 4

        return "${clean.first()}|$bucket"
    }
}
