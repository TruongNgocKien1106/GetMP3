package com.ngoctien.getmp3.ui.skin.compact

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush

@Composable
internal fun CompactBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors =
        listOf(
            MaterialTheme
                .colorScheme
                .surface,

            MaterialTheme
                .colorScheme
                .surfaceContainerLow,

            MaterialTheme
                .colorScheme
                .surface
        )

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                colors
                        )
                )
    ) {
        content()
    }
}
