package com.ngoctien.getmp3.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ngoctien.getmp3.settings.AppUiStyle

@Composable
internal fun UiStyleSelector(
    style: AppUiStyle,
    onStyleChange:
        (AppUiStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier =
            modifier
    ) {
        AnimatedSelectorField(
            label =
                "Bố cục",

            value =
                when (style) {
                    AppUiStyle.BENTO ->
                        "Bento"

                    AppUiStyle.COMPACT ->
                        "Compact"
                },

            options =
                listOf(
                    "Bento",
                    "Compact"
                ),

            onSelected = {
                    selected ->

                onStyleChange(
                    when (selected) {
                        "Compact" ->
                            AppUiStyle.COMPACT

                        else ->
                            AppUiStyle.BENTO
                    }
                )
            }
        )
    }
}
