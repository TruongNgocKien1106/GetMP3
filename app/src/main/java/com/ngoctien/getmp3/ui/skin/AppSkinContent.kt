package com.ngoctien.getmp3.ui.skin

import androidx.compose.runtime.Composable
import com.ngoctien.getmp3.ui.design.ProvideAppDesign
import com.ngoctien.getmp3.ui.design.UiStyle

/*
 * Feature-level skin switch for independent presentations.
 *
 * Both Bento and Compact are required so a missing implementation
 * cannot silently fall back to another skin.
 *
 * Features with a single shared presentation do not pass through
 * this switch. They render directly inside the AppShell design scope.
 */
@Composable
internal fun AppSkinContent(
    style: UiStyle,
    bento: @Composable () -> Unit,
    compact: @Composable () -> Unit
) {
    ProvideAppDesign(
        style =
            style
    ) {
        when (style) {
            UiStyle.BENTO -> {
                bento()
            }

            UiStyle.COMPACT -> {
                compact()
            }
        }
    }
}
