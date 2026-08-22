package com.ngoctien.getmp3.settings

/*
 * Persisted UI-style preference.
 *
 * Đặt trong settings layer để repository/ViewModel không phụ thuộc
 * package UI hoặc Compose.
 *
 * Mapping sang ui.design.UiStyle nằm ở presentation layer.
 */
enum class AppUiStyle {
    BENTO,
    COMPACT
}
