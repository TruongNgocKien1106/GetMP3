package com.ngoctien.getmp3.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.ui.theme.BrandBlue
import com.ngoctien.getmp3.ui.theme.BrandCyan
import com.ngoctien.getmp3.ui.theme.BrandViolet
import com.ngoctien.getmp3.viewmodel.CompareIndexUiState
import kotlin.math.roundToInt

@Composable
internal fun LibrarySyncProgressScreen(
    state: CompareIndexUiState,
    libraryFolderName: String,
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onRetry: () -> Unit
) {
    val animatedProgress by
        animateFloatAsState(
            targetValue =
                state.progressFraction,
            animationSpec =
                tween(
                    durationMillis =
                        320
                ),
            label =
                "library-sync-progress"
        )

    val infiniteTransition =
        rememberInfiniteTransition(
            label =
                "library-sync-motion"
        )

    val rotation by
        infiniteTransition.animateFloat(
            initialValue =
                0f,
            targetValue =
                360f,
            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            durationMillis =
                                1_700,
                            easing =
                                LinearEasing
                        )
                ),
            label =
                "library-sync-rotation"
        )

    val hasError =
        !state.errorMessage
            .isNullOrBlank()

    val complete =
        !state.isScanning &&
            !hasError &&
            state.processedFiles > 0

    AppScreenBackdrop(
        modifier =
            modifier
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(
                        horizontal =
                            18.dp,
                        vertical =
                            14.dp
                    ),
            verticalArrangement =
                Arrangement.spacedBy(
                    14.dp
                )
        ) {
            SyncHeader(
                state =
                    state,
                libraryFolderName =
                    libraryFolderName,
                rotation =
                    rotation,
                hasError =
                    hasError,
                complete =
                    complete,
                onClose =
                    onClose
            )

            SyncProgressCard(
                state =
                    state,
                progress =
                    animatedProgress,
                hasError =
                    hasError,
                complete =
                    complete
            )

            SyncStatsGrid(
                state =
                    state
            )

            Spacer(
                modifier =
                    Modifier.fillMaxHeight(
                        0.04f
                    )
            )

            CurrentFileCard(
                state =
                    state
            )

            Spacer(
                modifier =
                    Modifier.fillMaxHeight(
                        0.04f
                    )
            )

            SyncBottomAction(
                state =
                    state,
                hasError =
                    hasError,
                complete =
                    complete,
                onClose =
                    onClose,
                onRetry =
                    onRetry
            )
        }
    }
}


@Composable
private fun SyncHeader(
    state: CompareIndexUiState,
    libraryFolderName: String,
    rotation: Float,
    hasError: Boolean,
    complete: Boolean,
    onClose: () -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                28.dp
            ),
        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(
                    alpha =
                        0.92f
                ),
        tonalElevation =
            7.dp,
        shadowElevation =
            8.dp,
        border =
            BorderStroke(
                1.dp,
                BrandBlue.copy(
                    alpha =
                        0.48f
                )
            )
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                BrandBlue.copy(
                                    alpha =
                                        0.20f
                                ),
                                Color.Transparent,
                                BrandViolet.copy(
                                    alpha =
                                        0.13f
                                )
                            )
                        )
                    )
                    .padding(
                        horizontal =
                            14.dp,
                        vertical =
                            13.dp
                    ),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {
                Surface(
                    modifier =
                        Modifier.size(
                            52.dp
                        ),
                    shape =
                        RoundedCornerShape(
                            17.dp
                        ),
                    color =
                        BrandBlue.copy(
                            alpha =
                                0.24f
                        ),
                    border =
                        BorderStroke(
                            1.dp,
                            BrandCyan.copy(
                                alpha =
                                    0.58f
                            )
                        )
                ) {
                    Box(
                        modifier =
                            Modifier.fillMaxSize(),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Icon(
                            imageVector =
                                when {
                                    hasError ->
                                        Icons.Rounded.ErrorOutline

                                    complete ->
                                        Icons.Rounded.CheckCircle

                                    else ->
                                        Icons.Rounded.Sync
                                },
                            contentDescription =
                                null,
                            modifier =
                                Modifier
                                    .size(
                                        27.dp
                                    )
                                    .graphicsLayer {
                                        rotationZ =
                                            if (
                                                state.isScanning
                                            ) {
                                                rotation
                                            } else {
                                                0f
                                            }
                                    },
                            tint =
                                when {
                                    hasError ->
                                        MaterialTheme
                                            .colorScheme
                                            .error

                                    complete ->
                                        BrandCyan

                                    else ->
                                        Color.White
                                }
                        )
                    }
                }

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(
                            1.dp
                        )
                ) {
                    Text(
                        text =
                            "Đồng bộ Library",
                        style =
                            MaterialTheme
                                .typography
                                .titleLarge,
                        fontWeight =
                            FontWeight.Black
                    )

                    Text(
                        text =
                            libraryFolderName
                                .ifBlank {
                                    "Library"
                                },
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
            }

            Surface(
                modifier =
                    Modifier
                        .size(
                            42.dp
                        )
                        .bouncyClickable(
                            pressedScale =
                                0.90f,
                            onClick =
                                onClose
                        ),
                shape =
                    CircleShape,
                color =
                    MaterialTheme
                        .colorScheme
                        .surfaceVariant
                        .copy(
                            alpha =
                                0.78f
                        ),
                border =
                    BorderStroke(
                        1.dp,
                        MaterialTheme
                            .colorScheme
                            .outlineVariant
                    )
            ) {
                Box(
                    modifier =
                        Modifier.fillMaxSize(),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Close,
                        contentDescription =
                            if (
                                state.isScanning
                            ) {
                                "Chạy nền"
                            } else {
                                "Đóng"
                            },
                        modifier =
                            Modifier.size(
                                20.dp
                            )
                    )
                }
            }
        }
    }
}


@Composable
private fun SyncProgressCard(
    state: CompareIndexUiState,
    progress: Float,
    hasError: Boolean,
    complete: Boolean
) {
    val percentage =
        (
            progress
                .coerceIn(
                    0f,
                    1f
                ) *
                100f
            )
            .roundToInt()

    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                28.dp
            ),
        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(
                    alpha =
                        0.86f
                ),
        tonalElevation =
            5.dp,
        shadowElevation =
            6.dp,
        border =
            BorderStroke(
                1.dp,
                MaterialTheme
                    .colorScheme
                    .outlineVariant
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    18.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.Bottom
            ) {
                Column {
                    Text(
                        text =
                            when {
                                hasError ->
                                    "Đồng bộ bị gián đoạn"

                                complete ->
                                    "Đồng bộ hoàn tất"

                                state.isScanning ->
                                    "Đang quét thư viện"

                                else ->
                                    "Chuẩn bị quét"
                            },
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.Black
                    )

                    Text(
                        text =
                            when {
                                state.totalFiles <= 0 &&
                                    state.isScanning ->
                                    "Đang đọc danh sách file..."

                                state.totalFiles > 0 ->
                                    "${state.processedFiles} / ${state.totalFiles} file"

                                else ->
                                    "Chưa có dữ liệu"
                            },
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }

                Text(
                    text =
                        if (
                            state.totalFiles > 0
                        ) {
                            "$percentage%"
                        } else {
                            "•••"
                        },
                    style =
                        MaterialTheme
                            .typography
                            .headlineMedium,
                    fontWeight =
                        FontWeight.Black,
                    color =
                        when {
                            hasError ->
                                MaterialTheme
                                    .colorScheme
                                    .error

                            complete ->
                                BrandCyan

                            else ->
                                MaterialTheme
                                    .colorScheme
                                    .primary
                        }
                )
            }

            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            12.dp
                        ),
                shape =
                    CircleShape,
                color =
                    MaterialTheme
                        .colorScheme
                        .surfaceVariant
            ) {
                Box(
                    modifier =
                        Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(
                                    if (
                                        state.totalFiles > 0
                                    ) {
                                        progress
                                            .coerceIn(
                                                0f,
                                                1f
                                            )
                                    } else {
                                        0.08f
                                    }
                                )
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            BrandCyan,
                                            BrandBlue,
                                            BrandViolet
                                        )
                                    ),
                                    CircleShape
                                )
                    )
                }
            }

            Text(
                text =
                    state.errorMessage
                        ?: state.message
                        ?: if (
                            state.isScanning
                        ) {
                            "Đang chuẩn bị dữ liệu..."
                        } else {
                            "Sẵn sàng."
                        },
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    if (hasError) {
                        MaterialTheme
                            .colorScheme
                            .error
                    } else {
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                    }
            )
        }
    }
}


@Composable
private fun SyncStatsGrid(
    state: CompareIndexUiState
) {
    BoxWithConstraints(
        modifier =
            Modifier.fillMaxWidth()
    ) {
        val cardWidth =
            (
                maxWidth -
                    10.dp
                ) /
                2f

        Column(
            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {
            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {
                SyncStatCard(
                    label =
                        "Đã quét",
                    value =
                        state.processedFiles
                            .toString(),
                    accent =
                        BrandBlue,
                    modifier =
                        Modifier.width(
                            cardWidth
                        )
                )

                SyncStatCard(
                    label =
                        "Tổng file",
                    value =
                        state.totalFiles
                            .toString(),
                    accent =
                        BrandViolet,
                    modifier =
                        Modifier.width(
                            cardWidth
                        )
                )
            }

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {
                SyncStatCard(
                    label =
                        "Mới / thay đổi",
                    value =
                        "${state.newFiles} / ${state.changedFiles}",
                    accent =
                        BrandCyan,
                    modifier =
                        Modifier.width(
                            cardWidth
                        )
                )

                SyncStatCard(
                    label =
                        "Bỏ qua / lỗi",
                    value =
                        "${state.skippedFiles} / ${state.failedFiles}",
                    accent =
                        if (
                            state.failedFiles > 0
                        ) {
                            MaterialTheme
                                .colorScheme
                                .error
                        } else {
                            BrandBlue
                        },
                    modifier =
                        Modifier.width(
                            cardWidth
                        )
                )
            }
        }
    }
}


@Composable
private fun SyncStatCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier =
            modifier.height(
                78.dp
            ),
        shape =
            RoundedCornerShape(
                21.dp
            ),
        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(
                    alpha =
                        0.82f
                ),
        tonalElevation =
            4.dp,
        border =
            BorderStroke(
                1.dp,
                accent.copy(
                    alpha =
                        0.38f
                )
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    horizontal =
                        13.dp,
                    vertical =
                        10.dp
                ),
            verticalArrangement =
                Arrangement.SpaceBetween
        ) {
            Text(
                text =
                    label,
                style =
                    MaterialTheme
                        .typography
                        .labelMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Text(
                text =
                    value,
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                fontWeight =
                    FontWeight.Black,
                color =
                    accent
            )
        }
    }
}


@Composable
private fun CurrentFileCard(
    state: CompareIndexUiState
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                22.dp
            ),
        color =
            MaterialTheme
                .colorScheme
                .surfaceVariant
                .copy(
                    alpha =
                        0.60f
                ),
        border =
            BorderStroke(
                1.dp,
                MaterialTheme
                    .colorScheme
                    .outlineVariant
            )
    ) {
        Row(
            modifier =
                Modifier.padding(
                    13.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(
                    11.dp
                )
        ) {
            Surface(
                modifier =
                    Modifier.size(
                        39.dp
                    ),
                shape =
                    RoundedCornerShape(
                        12.dp
                    ),
                color =
                    BrandBlue.copy(
                        alpha =
                            0.20f
                    )
            ) {
                Box(
                    modifier =
                        Modifier.fillMaxSize(),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Folder,
                        contentDescription =
                            null,
                        modifier =
                            Modifier.size(
                                20.dp
                            ),
                        tint =
                            BrandCyan
                    )
                }
            }

            Column {
                Text(
                    text =
                        "Đang xử lý",
                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Text(
                    text =
                        state.currentFileName
                            .ifBlank {
                                if (
                                    state.isScanning
                                ) {
                                    "Đang đọc danh sách file..."
                                } else {
                                    "Không có file đang xử lý"
                                }
                            },
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    fontWeight =
                        FontWeight.Bold,
                    maxLines =
                        1,
                    overflow =
                        TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Composable
private fun SyncBottomAction(
    state: CompareIndexUiState,
    hasError: Boolean,
    complete: Boolean,
    onClose: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                9.dp
            )
    ) {
        if (
            hasError &&
            !state.isScanning
        ) {
            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            50.dp
                        )
                        .bouncyClickable(
                            pressedScale =
                                0.97f,
                            onClick =
                                onRetry
                        ),
                shape =
                    RoundedCornerShape(
                        18.dp
                    ),
                color =
                    BrandBlue.copy(
                        alpha =
                            0.24f
                    ),
                border =
                    BorderStroke(
                        1.dp,
                        BrandCyan.copy(
                            alpha =
                                0.64f
                        )
                    )
            ) {
                Row(
                    modifier =
                        Modifier.fillMaxSize(),
                    horizontalArrangement =
                        Arrangement.Center,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Refresh,
                        contentDescription =
                            null,
                        modifier =
                            Modifier.size(
                                19.dp
                            )
                    )

                    Spacer(
                        modifier =
                            Modifier.width(
                                7.dp
                            )
                    )

                    Text(
                        text =
                            "Thử quét lại",
                        fontWeight =
                            FontWeight.Black
                    )
                }
            }
        }

        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        54.dp
                    )
                    .bouncyClickable(
                        pressedScale =
                            0.97f,
                        onClick =
                            onClose
                    ),
            shape =
                RoundedCornerShape(
                    19.dp
                ),
            color =
                Color.Transparent,
            border =
                BorderStroke(
                    1.dp,
                    BrandCyan.copy(
                        alpha =
                            0.68f
                    )
                ),
            tonalElevation =
                7.dp,
            shadowElevation =
                7.dp
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                if (
                                    complete
                                ) {
                                    listOf(
                                        BrandCyan,
                                        BrandBlue,
                                        BrandViolet
                                    )
                                } else {
                                    listOf(
                                        BrandBlue.copy(
                                            alpha =
                                                0.55f
                                        ),
                                        BrandViolet.copy(
                                            alpha =
                                                0.50f
                                        )
                                    )
                                }
                            )
                        ),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text =
                        when {
                            state.isScanning ->
                                "Chạy nền"

                            hasError ->
                                "Đóng"

                            else ->
                                "Xong"
                        },
                    fontWeight =
                        FontWeight.Black,
                    color =
                        Color.White
                )
            }
        }
    }
}