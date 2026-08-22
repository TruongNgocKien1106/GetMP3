package com.ngoctien.getmp3.ui.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * Đây là "skin", không phải business logic.
 *
 * Thêm giao diện mới chỉ cần thêm enum + AppDesignSpec
 * và implementation Screen tương ứng.
 */
enum class UiStyle {
    BENTO,
    COMPACT
}

@Immutable
internal data class AppSpacing(
    val tiny: Dp,
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val extraLarge: Dp,
    val screenHorizontal: Dp,
    val screenVertical: Dp,
    val cardPadding: Dp,
    val sectionGap: Dp
)

@Immutable
internal data class AppShapes(
    val controlCorner: Dp,
    val cardCorner: Dp,
    val largeCardCorner: Dp,
    val sheetCorner: Dp,
    val dockCorner: Dp
)

@Immutable
internal data class AppDesignSpec(
    val style: UiStyle,
    val spacing: AppSpacing,
    val shapes: AppShapes,
    val pressedScale: Float,
    val emphasizedPressedScale: Float
)

private val BentoDesign =
    AppDesignSpec(
        style =
            UiStyle.BENTO,

        spacing =
            AppSpacing(
                tiny = 4.dp,
                small = 8.dp,
                medium = 12.dp,
                large = 16.dp,
                extraLarge = 24.dp,
                screenHorizontal = 16.dp,
                screenVertical = 10.dp,
                cardPadding = 16.dp,
                sectionGap = 14.dp
            ),

        shapes =
            AppShapes(
                controlCorner = 20.dp,
                cardCorner = 28.dp,
                largeCardCorner = 32.dp,
                sheetCorner = 32.dp,
                dockCorner = 32.dp
            ),

        pressedScale =
            0.965f,

        emphasizedPressedScale =
            0.975f
    )

private val CompactDesign =
    AppDesignSpec(
        style =
            UiStyle.COMPACT,

        spacing =
            AppSpacing(
                tiny = 3.dp,
                small = 6.dp,
                medium = 10.dp,
                large = 14.dp,
                extraLarge = 20.dp,
                screenHorizontal = 12.dp,
                screenVertical = 8.dp,
                cardPadding = 12.dp,
                sectionGap = 10.dp
            ),

        shapes =
            AppShapes(
                controlCorner = 14.dp,
                cardCorner = 18.dp,
                largeCardCorner = 20.dp,
                sheetCorner = 22.dp,
                dockCorner = 24.dp
            ),

        pressedScale =
            0.975f,

        emphasizedPressedScale =
            0.982f
    )

internal fun appDesignFor(
    style: UiStyle
): AppDesignSpec =
    when (style) {
        UiStyle.BENTO ->
            BentoDesign

        UiStyle.COMPACT ->
            CompactDesign
    }

internal val LocalAppDesign =
    staticCompositionLocalOf {
        BentoDesign
    }

@Composable
internal fun ProvideAppDesign(
    style: UiStyle,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAppDesign provides
            appDesignFor(
                style
            ),
        content =
            content
    )
}
