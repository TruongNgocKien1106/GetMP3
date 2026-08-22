package com.ngoctien.getmp3.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class PreviewAppOption(
    val packageName: String,
    val activityName: String,
    val label: String
)

private data class SavedPreviewApp(
    val packageName: String,
    val activityName: String,
    val label: String
)

class PreviewAppManager(
    context: Context
) {
    companion object {
        private const val PREFERENCES_NAME =
            "getmp3_preview_app"

        private const val KEY_PACKAGE =
            "package"

        private const val KEY_ACTIVITY =
            "activity"

        private const val KEY_LABEL =
            "label"
    }

    private val applicationContext =
        context.applicationContext

    private val packageManager =
        applicationContext.packageManager

    private val preferences =
        applicationContext
            .getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )

    fun queryApps(
        url: String
    ): List<PreviewAppOption> {
        return queryActivities(
            createPreviewIntent(url)
        )
            .mapNotNull { resolveInfo ->
                val activityInfo =
                    resolveInfo.activityInfo
                        ?: return@mapNotNull null

                val packageName =
                    activityInfo.packageName
                        ?.trim()
                        .orEmpty()

                val activityName =
                    activityInfo.name
                        ?.trim()
                        .orEmpty()

                if (
                    packageName.isBlank() ||
                    activityName.isBlank() ||
                    packageName ==
                    applicationContext.packageName
                ) {
                    return@mapNotNull null
                }

                val label =
                    resolveInfo
                        .loadLabel(
                            packageManager
                        )
                        ?.toString()
                        ?.trim()
                        .orEmpty()
                        .ifBlank {
                            packageName
                        }

                PreviewAppOption(
                    packageName =
                        packageName,

                    activityName =
                        activityName,

                    label =
                        label
                )
            }
            .distinctBy {
                it.packageName
            }
            .sortedWith(
                compareBy<
                    PreviewAppOption
                    > {
                    previewPriority(it)
                }.thenBy {
                    it.label.lowercase()
                }
            )
    }

    fun openSaved(
        url: String
    ): Boolean {
        val saved =
            getSavedApp()
                ?: return false

        val option =
            PreviewAppOption(
                packageName =
                    saved.packageName,

                activityName =
                    saved.activityName,

                label =
                    saved.label
            )

        val opened =
            open(
                option =
                    option,

                url =
                    url,

                rememberChoice =
                    false
            )

        if (!opened) {
            clearSaved()
        }

        return opened
    }

    fun open(
        option: PreviewAppOption,
        url: String,
        rememberChoice: Boolean
    ): Boolean {
        val intent =
            createPreviewIntent(url)
                .setClassName(
                    option.packageName,
                    option.activityName
                )
                .addFlags(
                    Intent
                        .FLAG_ACTIVITY_NEW_TASK
                )

        return try {
            applicationContext
                .startActivity(intent)

            if (rememberChoice) {
                preferences.edit()
                    .putString(
                        KEY_PACKAGE,
                        option.packageName
                    )
                    .putString(
                        KEY_ACTIVITY,
                        option.activityName
                    )
                    .putString(
                        KEY_LABEL,
                        option.label
                    )
                    .apply()
            }

            true
        } catch (
            _: ActivityNotFoundException
        ) {
            false
        } catch (
            _: SecurityException
        ) {
            false
        }
    }

    fun clearSaved() {
        preferences.edit()
            .clear()
            .apply()
    }

    fun openSystemChooser(
        url: String
    ) {
        val chooser =
            Intent.createChooser(
                createPreviewIntent(url),
                "Chọn ứng dụng nghe thử"
            ).apply {
                addFlags(
                    Intent
                        .FLAG_ACTIVITY_NEW_TASK
                )
            }

        runCatching {
            applicationContext
                .startActivity(chooser)
        }
    }

    private fun getSavedApp():
        SavedPreviewApp? {

        val packageName =
            preferences.getString(
                KEY_PACKAGE,
                null
            )
                ?.takeIf(
                    String::isNotBlank
                )
                ?: return null

        val activityName =
            preferences.getString(
                KEY_ACTIVITY,
                null
            )
                ?.takeIf(
                    String::isNotBlank
                )
                ?: return null

        return SavedPreviewApp(
            packageName =
                packageName,

            activityName =
                activityName,

            label =
                preferences.getString(
                    KEY_LABEL,
                    packageName
                )
                    ?: packageName
        )
    }

    private fun createPreviewIntent(
        url: String
    ): Intent {
        return Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        ).apply {
            addCategory(
                Intent.CATEGORY_BROWSABLE
            )
        }
    }

    private fun queryActivities(
        intent: Intent
    ): List<ResolveInfo> {
        return if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {
            packageManager
                .queryIntentActivities(
                    intent,

                    PackageManager
                        .ResolveInfoFlags
                        .of(
                            PackageManager
                                .MATCH_ALL
                                .toLong()
                        )
                )
        } else {
            @Suppress("DEPRECATION")
            packageManager
                .queryIntentActivities(
                    intent,
                    PackageManager.MATCH_ALL
                )
        }
    }

    private fun previewPriority(
        option: PreviewAppOption
    ): Int {
        val searchable =
            (
                option.label +
                    " " +
                    option.packageName
                )
                .lowercase()

        return when {
            "pure tuber" in searchable ||
                "puretuber" in searchable -> 0

            "youtube" in searchable -> 1

            "music" in searchable ||
                "video" in searchable -> 2

            else -> 3
        }
    }
}

@Composable
fun PreviewAppChooserDialog(
    url: String,
    onDismiss: () -> Unit
) {
    val context =
        LocalContext.current

    val manager =
        remember(context) {
            PreviewAppManager(context)
        }

    val applications =
        remember(
            context,
            url
        ) {
            manager.queryApps(url)
        }

    var rememberChoice by
        rememberSaveable {
            mutableStateOf(true)
        }

    AlertDialog(
        onDismissRequest =
            onDismiss,

        icon = {
            Icon(
                imageVector =
                    Icons.Rounded.PlayArrow,

                contentDescription =
                    null
            )
        },

        title = {
            Text(
                "Chọn ứng dụng nghe thử"
            )
        },

        text = {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text =
                        "Chọn Pure Tuber, YouTube hoặc ứng dụng khác có thể mở liên kết.",

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bouncyClickable {
                            rememberChoice =
                                !rememberChoice
                        },

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked =
                            rememberChoice,

                        onCheckedChange = {
                            rememberChoice =
                                it
                        }
                    )

                    Text(
                        "Luôn dùng ứng dụng này"
                    )
                }

                if (applications.isEmpty()) {
                    Surface(
                        modifier =
                            Modifier.fillMaxWidth(),

                        shape =
                            RoundedCornerShape(16.dp),

                        color =
                            MaterialTheme
                                .colorScheme
                                .surfaceVariant,

                        contentColor =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    ) {
                        Text(
                            text =
                                "Không tìm thấy ứng dụng tương thích.",

                            modifier =
                                Modifier.padding(14.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier =
                            Modifier.heightIn(
                                max = 360.dp
                            )
                    ) {
                        items(
                            items =
                                applications,

                            key = {
                                "${it.packageName}/${it.activityName}"
                            }
                        ) { application ->
                            PreviewApplicationRow(
                                application =
                                    application,

                                onClick = {
                                    val opened =
                                        manager.open(
                                            option =
                                                application,

                                            url =
                                                url,

                                            rememberChoice =
                                                rememberChoice
                                        )

                                    onDismiss()

                                    if (!opened) {
                                        manager
                                            .openSystemChooser(
                                                url
                                            )
                                    }
                                }
                            )

                            HorizontalDivider(
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .outline
                                        .copy(
                                            alpha = 0.25f
                                        )
                            )
                        }
                    }
                }
            }
        },

        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()

                    manager.openSystemChooser(
                        url
                    )
                }
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.OpenInNew,

                    contentDescription =
                        null,

                    modifier =
                        Modifier.size(18.dp)
                )

                Spacer(
                    modifier =
                        Modifier.width(6.dp)
                )

                Text("Trình chọn Android")
            }
        },

        dismissButton = {
            TextButton(
                onClick =
                    onDismiss
            ) {
                Text("Hủy")
            }
        }
    )
}

@Composable
private fun PreviewApplicationRow(
    application: PreviewAppOption,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable(
                onClick = onClick
            )
            .padding(
                horizontal = 4.dp,
                vertical = 13.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Surface(
            modifier =
                Modifier.size(42.dp),

            shape =
                RoundedCornerShape(16.dp),

            color =
                MaterialTheme
                    .colorScheme
                    .primaryContainer,

            contentColor =
                MaterialTheme
                    .colorScheme
                    .onPrimaryContainer,

            border =
                BorderStroke(
                    width = 1.dp,

                    color =
                        MaterialTheme
                            .colorScheme
                            .outline
                            .copy(
                                alpha = 0.22f
                            )
                )
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.PlayArrow,

                contentDescription =
                    null,

                modifier =
                    Modifier.padding(9.dp)
            )
        }

        Spacer(
            modifier =
                Modifier.width(12.dp)
        )

        Column(
            modifier =
                Modifier.weight(1f)
        ) {
            Text(
                text =
                    application.label,

                maxLines = 1,

                overflow =
                    TextOverflow.Ellipsis,

                fontWeight =
                    FontWeight.SemiBold
            )

            Text(
                text =
                    application.packageName,

                maxLines = 1,

                overflow =
                    TextOverflow.Ellipsis,

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}
