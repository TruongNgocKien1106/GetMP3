package com.ngoctien.getmp3

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ngoctien.getmp3.ui.GetMp3Screen
import com.ngoctien.getmp3.ui.theme.GetMP3Theme
import com.ngoctien.getmp3.viewmodel.DownloadViewModel
import com.ngoctien.getmp3.viewmodel.SettingsViewModel
import com.ngoctien.getmp3.viewmodel.SongNoteViewModel
import com.ngoctien.getmp3.viewmodel.TagEditorViewModel

class MainActivity : ComponentActivity() {
    private val downloadViewModel:
        DownloadViewModel by viewModels()

    private val tagEditorViewModel:
        TagEditorViewModel by viewModels()

    private val settingsViewModel:
        SettingsViewModel by viewModels()
    private val songNoteViewModel:
        SongNoteViewModel by viewModels()

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts
                .RequestPermission()
        ) {
            // App vẫn tải nếu người dùng từ chối.
        }

    private val downloadFolderPicker =
        registerForActivityResult(
            ActivityResultContracts
                .OpenDocumentTree()
        ) { uri ->
            if (uri == null) {
                return@registerForActivityResult
            }

            persistFolderPermission(uri)

            settingsViewModel
                .setDownloadFolder(
                    treeUri =
                        uri.toString(),

                    displayName =
                        resolveFolderName(uri)
                )

            tagEditorViewModel.refresh()
        }

    private val compareFolderPicker =
        registerForActivityResult(
            ActivityResultContracts
                .OpenDocumentTree()
        ) { uri ->
            if (uri == null) {
                return@registerForActivityResult
            }

            persistFolderPermission(uri)

            settingsViewModel
                .setCompareFolder(
                    treeUri =
                        uri.toString(),

                    displayName =
                        resolveFolderName(uri)
                )
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val settings =
                settingsViewModel.uiState
                    .collectAsStateWithLifecycle()
                    .value

            GetMP3Theme(
                themeMode =
                    settings.themeMode
            ) {
                GetMp3Screen(
                    downloadViewModel =
                        downloadViewModel,

                    tagEditorViewModel =
                        tagEditorViewModel,

                    settingsViewModel =
                        settingsViewModel,

                    songNoteViewModel =
                        songNoteViewModel,
                    onRequestNotificationPermission = {
                        requestNotificationPermission()
                    },

                    onChooseDownloadFolder = {
                        downloadFolderPicker.launch(
                            settings.downloadTreeUri
                                ?.let(Uri::parse)
                        )
                    },

                    onChooseCompareFolder = {
                        compareFolderPicker.launch(
                            settings.compareTreeUri
                                ?.let(Uri::parse)
                        )
                    }
                )
            }
        }
    }

    private fun requestNotificationPermission() {
        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.TIRAMISU
        ) {
            return
        }

        val granted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission
                    .POST_NOTIFICATIONS
            ) == PackageManager
                .PERMISSION_GRANTED

        if (!granted) {
            notificationPermissionLauncher
                .launch(
                    Manifest.permission
                        .POST_NOTIFICATIONS
                )
        }
    }

    private fun persistFolderPermission(
        uri: Uri
    ) {
        val flags =
            Intent
                .FLAG_GRANT_READ_URI_PERMISSION or
                Intent
                    .FLAG_GRANT_WRITE_URI_PERMISSION

        runCatching {
            contentResolver
                .takePersistableUriPermission(
                    uri,
                    flags
                )
        }
    }

    private fun resolveFolderName(
        treeUri: Uri
    ): String {
        return runCatching {
            val documentId =
                DocumentsContract
                    .getTreeDocumentId(
                        treeUri
                    )

            val documentUri =
                DocumentsContract
                    .buildDocumentUriUsingTree(
                        treeUri,
                        documentId
                    )

            contentResolver.query(
                documentUri,
                arrayOf(
                    DocumentsContract
                        .Document
                        .COLUMN_DISPLAY_NAME
                ),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(0)
                } else {
                    null
                }
            }
        }.getOrNull()
            ?.takeIf {
                it.isNotBlank()
            }
            ?: "Thư mục đã chọn"
    }
}