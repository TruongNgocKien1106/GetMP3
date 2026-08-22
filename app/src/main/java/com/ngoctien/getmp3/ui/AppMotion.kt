package com.ngoctien.getmp3.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/*
 * Motion token duy nhất của app.
 *
 * Feature UI không tự hard-code duration/spring nếu không
 * thật sự có lý do riêng.
 */
internal object AppMotion {

    const val QuickMillis =
        120

    const val ExitMillis =
        140

    const val ContentMillis =
        180

    const val EnterMillis =
        220

    const val SkeletonMillis =
        900

    const val AmbientDriftMillis =
        12_000

    const val AmbientBreatheMillis =
        6_500

    const val ActionPulseMillis =
        5_000

    val pressSpring:
        SpringSpec<Float> =
        spring(
            dampingRatio =
                Spring
                    .DampingRatioMediumBouncy,

            stiffness =
                Spring
                    .StiffnessMediumLow
        )

    val selectionSpring:
        SpringSpec<Float> =
        spring(
            dampingRatio =
                Spring
                    .DampingRatioNoBouncy,

            stiffness =
                Spring
                    .StiffnessMedium
        )

    /*
     * Success / loading state.
     *
     * Có bounce nhẹ nhưng ngắn hơn interaction chính.
     */
    val feedbackSpring:
        SpringSpec<Float> =
        spring(
            dampingRatio =
                0.72f,

            stiffness =
                Spring
                    .StiffnessMedium
        )

    /*
     * Dùng khi muốn card hoặc icon nhấn mạnh state mới,
     * không dùng cho animation trang trí vô hạn.
     */
    val emphasisSpring:
        SpringSpec<Float> =
        spring(
            dampingRatio =
                Spring
                    .DampingRatioLowBouncy,

            stiffness =
                Spring
                    .StiffnessMediumLow
        )
}
