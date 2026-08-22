package com.ngoctien.getmp3.ui

import com.ngoctien.getmp3.settings.AppThemeMode
import com.ngoctien.getmp3.settings.AppUiStyle

internal sealed interface SettingsAction {

    data class BitrateChanged(
        val value: Int
    ) : SettingsAction

    data class ThemeChanged(
        val value: AppThemeMode
    ) : SettingsAction

    data class UiStyleChanged(
        val value: AppUiStyle
    ) : SettingsAction

    object ChooseInboxFolder :
        SettingsAction

    object UseDefaultInboxFolder :
        SettingsAction

    object ChooseLibraryFolder :
        SettingsAction

    object ClearLibraryFolder :
        SettingsAction

    object RebuildLibraryIndex :
        SettingsAction

    data class SaveFilters(
        val terms: List<String>,
        val symbols: String
    ) : SettingsAction

    object RetryFfmpeg :
        SettingsAction
}
