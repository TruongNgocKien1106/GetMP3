package com.ngoctien.getmp3.ui

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * TextField giữ cursor, vùng chọn và composing state ở trong Compose.
 *
 * ViewModel vẫn sử dụng String, nhưng thao tác kéo con trỏ không bị gửi
 * lên ViewModel rồi ghi đè lại trong lần recomposition tiếp theo.
 */
@Composable
internal fun StableOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int =
        if (singleLine) {
            1
        } else {
            Int.MAX_VALUE
        },
    shape: Shape =
        OutlinedTextFieldDefaults.shape,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions =
        KeyboardOptions.Default,
    keyboardActions: KeyboardActions =
        KeyboardActions.Default,
    colors: TextFieldColors =
        OutlinedTextFieldDefaults.colors()
) {
    var localValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection =
                    TextRange(value.length)
            )
        )
    }

    /*
     * Chỉ đồng bộ khi nội dung thật sự được thay đổi từ bên ngoài,
     * chẳng hạn nút Xóa, Format nhanh hoặc chuyển sang bài khác.
     *
     * Khi người dùng chỉ kéo cursor/selection, localValue được giữ nguyên.
     */
    LaunchedEffect(value) {
        if (localValue.text != value) {
            localValue =
                TextFieldValue(
                    text = value,
                    selection =
                        TextRange(value.length)
                )
        }
    }

    OutlinedTextField(
        value = localValue,

        onValueChange = { nextValue ->
            val textChanged =
                nextValue.text !=
                    localValue.text

            localValue =
                nextValue

            if (textChanged) {
                onValueChange(
                    nextValue.text
                )
            }
        },

        modifier = modifier,
        readOnly = readOnly,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        shape = shape,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = supportingText,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        colors = colors
    )
}
