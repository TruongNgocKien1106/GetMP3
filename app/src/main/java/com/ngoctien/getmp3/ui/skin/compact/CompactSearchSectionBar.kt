package com.ngoctien.getmp3.ui.skin.compact

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.ui.AppMotion
import com.ngoctien.getmp3.ui.design.appPressable
import com.ngoctien.getmp3.ui.design.LocalAppDesign
import com.ngoctien.getmp3.viewmodel.SearchDownloadSection

@Composable
internal fun CompactSearchSectionBar(
    selected: SearchDownloadSection,
    resultCount: Int,
    queueCount: Int,
    referenceCount: Int,
    onSelected:
        (SearchDownloadSection) -> Unit,
    onOpenReferenceMatches: () -> Unit
) {
    val design =
        LocalAppDesign.current

    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                design.shapes.controlCorner
            ),

        color =
            MaterialTheme
                .colorScheme
                .surfaceVariant
                .copy(
                    alpha = 0.50f
                )
    ) {
        Row(
            modifier =
                Modifier.padding(
                    4.dp
                ),

            horizontalArrangement =
                Arrangement.spacedBy(
                    4.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            CompactSearchSegment(
                label =
                    "Kết quả",

                count =
                    resultCount,

                selected =
                    selected ==
                        SearchDownloadSection
                            .RESULTS,

                onClick = {
                    onSelected(
                        SearchDownloadSection
                            .RESULTS
                    )
                }
            )

            CompactSearchSegment(
                label =
                    "Hàng đợi",

                count =
                    queueCount,

                selected =
                    selected ==
                        SearchDownloadSection
                            .QUEUE,

                onClick = {
                    onSelected(
                        SearchDownloadSection
                            .QUEUE
                    )
                }
            )

            if (referenceCount > 0) {
                CompactSearchSegment(
                    label =
                        "Library",

                    count =
                        referenceCount,

                    selected =
                        false,

                    onClick =
                        onOpenReferenceMatches
                )
            }
        }
    }
}

@Composable
private fun RowScope.CompactSearchSegment(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val design =
        LocalAppDesign.current

    val targetBackground =
        if (selected) {
            MaterialTheme
                .colorScheme
                .primaryContainer
        }
        else {
            Color.Transparent
        }

    val background by
        animateColorAsState(
            targetValue =
                targetBackground,

            animationSpec =
                tween(
                    AppMotion.ContentMillis
                ),

            label =
                "compact-search-segment-background"
        )

    val targetContent =
        if (selected) {
            MaterialTheme
                .colorScheme
                .onPrimaryContainer
        }
        else {
            MaterialTheme
                .colorScheme
                .onSurfaceVariant
        }

    val contentColor by
        animateColorAsState(
            targetValue =
                targetContent,

            animationSpec =
                tween(
                    AppMotion.ContentMillis
                ),

            label =
                "compact-search-segment-content"
        )

    Row(
        modifier =
            Modifier
                .weight(1f)
                .clip(
                    RoundedCornerShape(
                        design.shapes.controlCorner
                    )
                )
                .background(
                    background
                )
                .appPressable(
                    haptic =
                        true,

                    onClick =
                        onClick
                )
                .padding(
                    horizontal =
                        8.dp,

                    vertical =
                        10.dp
                ),

        horizontalArrangement =
            Arrangement.Center,

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            text =
                if (count > 0) {
                    "$label $count"
                }
                else {
                    label
                },

            maxLines =
                1,

            style =
                MaterialTheme
                    .typography
                    .labelMedium,

            fontWeight =
                if (selected) {
                    FontWeight.Bold
                }
                else {
                    FontWeight.Medium
                },

            color =
                contentColor
        )
    }
}
