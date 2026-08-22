package com.ngoctien.getmp3.ui.skin.compact

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.ui.AppHeaderActionButton
import com.ngoctien.getmp3.ui.AppPageHeader
import com.ngoctien.getmp3.ui.design.LocalAppDesign
import com.ngoctien.getmp3.ui.theme.BrandBlue
import com.ngoctien.getmp3.ui.theme.BrandCyan
import com.ngoctien.getmp3.ui.theme.BrandPink
import com.ngoctien.getmp3.ui.theme.BrandViolet

/*
 * Compact Home is intentionally independent from BentoHome.
 *
 * Both consume the same semantic values and callbacks.
 * Only layout and presentation differ.
 */
@Composable
internal fun CompactHome(
    inboxFolderName: String,
    libraryFolderName: String,
    activeDownloadCount: Int,
    librarySongCount: Int,
    attentionCount: Int,
    duplicateCount: Int?,
    modifier: Modifier = Modifier,
    onOpenInbox: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenTagEditor: () -> Unit,
    onOpenCompare: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val design =
        LocalAppDesign.current

    val duplicateText =
        duplicateCount
            ?.toString()
            ?: "—"

    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .statusBarsPadding(),

        contentPadding =
            PaddingValues(
                horizontal =
                    design
                        .spacing
                        .screenHorizontal,

                vertical =
                    design
                        .spacing
                        .screenVertical
            ),

        verticalArrangement =
            Arrangement.spacedBy(
                design
                    .spacing
                    .small
            )
    ) {
        item {
            AppPageHeader(
                title =
                    "GetMP3",

                subtitle =
                    "Inbox → Library",

                icon =
                    Icons.Rounded
                        .GraphicEq,

                action = {
                    AppHeaderActionButton(
                        icon =
                            Icons.Rounded
                                .Settings,

                        contentDescription =
                            "Mở cài đặt",

                        onClick =
                            onOpenSettings
                    )
                }
            )
        }

        item {
            CompactHomeRow(
                title =
                    "Inbox",

                subtitle =
                    inboxFolderName
                        .ifBlank {
                            "Nhạc mới chưa xử lý"
                        },

                metric =
                    activeDownloadCount
                        .toString(),

                metricLabel =
                    "đang tải",

                icon =
                    Icons.Rounded
                        .Download,

                accent =
                    BrandBlue,

                onClick =
                    onOpenInbox
            )
        }

        item {
            CompactHomeRow(
                title =
                    "Library",

                subtitle =
                    libraryFolderName
                        .ifBlank {
                            "Nhạc đã chuẩn hóa"
                        },

                metric =
                    librarySongCount
                        .toString(),

                metricLabel =
                    "bài",

                icon =
                    Icons.Rounded
                        .LibraryMusic,

                accent =
                    BrandCyan,

                onClick =
                    onOpenCompare
            )
        }

        item {
            CompactHomeRow(
                title =
                    "Lyrics",

                subtitle =
                    "Tìm • đọc • chèn lời",

                metric =
                    "LRC",

                metricLabel =
                    "lyrics",

                icon =
                    Icons.Rounded
                        .LibraryMusic,

                accent =
                    BrandViolet,

                onClick =
                    onOpenLyrics
            )
        }

        item {
            CompactHomeRow(
                title =
                    "Sửa thẻ",

                subtitle =
                    "Title • Artist • Album • Year",

                metric =
                    "ID3",

                metricLabel =
                    "metadata",

                icon =
                    Icons.Rounded
                        .Edit,

                accent =
                    BrandPink,

                onClick =
                    onOpenTagEditor
            )
        }

        item {
            CompactHomeRow(
                title =
                    "Cần xử lý",

                subtitle =
                    "$duplicateText cặp trùng • $attentionCount file lỗi",

                metric =
                    duplicateCount
                        ?.let {
                            (
                                it +
                                    attentionCount
                                )
                                .toString()
                        }
                        ?: attentionCount
                            .toString(),

                metricLabel =
                    "mục",

                icon =
                    Icons.Rounded
                        .Sync,

                accent =
                    BrandViolet,

                onClick =
                    onOpenCompare
            )
        }

        item {
            CompactHomeRow(
                title =
                    "Cài đặt",

                subtitle =
                    "Bitrate • thư mục • giao diện • index",

                metric =
                    "→",

                metricLabel =
                    "mở",

                icon =
                    Icons.Rounded
                        .Settings,

                accent =
                    BrandBlue,

                onClick =
                    onOpenSettings
            )
        }
    }
}

@Composable
private fun CompactHomeRow(
    title: String,
    subtitle: String,
    metric: String,
    metricLabel: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    val design =
        LocalAppDesign.current

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onClick
                ),

        shape =
            RoundedCornerShape(
                design
                    .shapes
                    .cardCorner
            ),

        color =
            MaterialTheme
                .colorScheme
                .surface,

        tonalElevation =
            1.dp,

        border =
            BorderStroke(
                width =
                    1.dp,

                color =
                    MaterialTheme
                        .colorScheme
                        .outline
                        .copy(
                            alpha =
                                0.12f
                        )
            )
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        design
                            .spacing
                            .cardPadding
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Surface(
                shape =
                    RoundedCornerShape(
                        design
                            .shapes
                            .controlCorner
                    ),

                color =
                    accent.copy(
                        alpha =
                            0.12f
                    )
            ) {
                Box(
                    modifier =
                        Modifier.size(
                            42.dp
                        ),

                    contentAlignment =
                        Alignment.Center
                ) {
                    Icon(
                        imageVector =
                            icon,

                        contentDescription =
                            null,

                        tint =
                            accent,

                        modifier =
                            Modifier.size(
                                22.dp
                            )
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.width(
                        design
                            .spacing
                            .medium
                    )
            )

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {
                Text(
                    text =
                        title,

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.SemiBold
                )

                Text(
                    text =
                        subtitle,

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis
                )
            }

            Spacer(
                modifier =
                    Modifier.width(
                        design
                            .spacing
                            .small
                    )
            )

            Column(
                horizontalAlignment =
                    Alignment.End
            ) {
                Text(
                    text =
                        metric,

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        accent
                )

                Text(
                    text =
                        metricLabel,

                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }
        }
    }
}
