package com.ngoctien.getmp3.ui.app

import com.ngoctien.getmp3.settings.AppUiStyle
import com.ngoctien.getmp3.ui.design.UiStyle

internal fun AppUiStyle.toUiStyle():
    UiStyle =
    when (this) {
        AppUiStyle.BENTO ->
            UiStyle.BENTO

        AppUiStyle.COMPACT ->
            UiStyle.COMPACT
    }
