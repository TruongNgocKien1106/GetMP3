package com.ngoctien.getmp3.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
internal fun AppExpandableCard(
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    haptic: Boolean = false,
    header: @Composable () -> Unit,
    expandedContent: @Composable () -> Unit
) {
    var expanded by
        remember {
            mutableStateOf(
                initiallyExpanded
            )
        }

    AppCard(
        modifier =
            modifier
                .animateContentSize(),

        onClick = {
            expanded =
                !expanded
        },

        haptic =
            haptic,

        contentPadding =
            PaddingValues()
    ) {
        Column {
            header()

            AnimatedVisibility(
                visible =
                    expanded,

                enter =
                    expandVertically() +
                        fadeIn(),

                exit =
                    shrinkVertically() +
                        fadeOut()
            ) {
                expandedContent()
            }
        }
    }
}
