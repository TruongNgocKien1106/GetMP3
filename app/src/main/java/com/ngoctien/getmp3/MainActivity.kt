package com.ngoctien.getmp3

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ngoctien.getmp3.storage.MusicFolderProvisioner
import com.ngoctien.getmp3.ui.app.GetMp3Route
import com.ngoctien.getmp3.ui.theme.GetMP3Theme
import com.ngoctien.getmp3.viewmodel.CompareViewModel
import com.ngoctien.getmp3.viewmodel.DownloadViewModel
import com.ngoctien.getmp3.viewmodel.LibraryViewModel
import com.ngoctien.getmp3.viewmodel.SettingsViewModel
import com.ngoctien.getmp3.viewmodel.LyricsViewModel
import com.ngoctien.getmp3.viewmodel.MetadataRepairViewModel
import com.ngoctien.getmp3.viewmodel.TagEditorViewModel
import com.ngoctien.getmp3.viewmodel.YouTubeSearchViewModel

class MainActivity : ComponentActivity() {
    private val downloadViewModel:
        DownloadViewModel by viewModels()

    private val tagEditorViewModel:
        TagEditorViewModel by viewModels()

    private val compareViewModel:
        CompareViewModel by viewModels()

    private val metadataRepairViewModel:
        MetadataRepairViewModel by viewModels()

    private val settingsViewModel:
        SettingsViewModel by viewModels()

    private val libraryViewModel:
        LibraryViewModel by viewModels()

    private val lyricsViewModel:
        LyricsViewModel by viewModels()
    private val youtubeSearchViewModel:
        YouTubeSearchViewModel by viewModels()

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts
                .RequestPermission()
        ) {
            // App vẫn tải nếu người dùng từ chối.
        }

    private val musicFolderPicker =
        registerForActivityResult(
            ActivityResultContracts
                .OpenDocumentTree()
        ) { uri ->
            if (uri == null) {
                return@registerForActivityResult
            }

            persistFolderPermission(uri)

            runCatching {
                val folders =
                    MusicFolderProvisioner(
                        this
                    ).ensure(
                        uri
                    )

                val rootName =
                    resolveFolderName(
                        uri
                    )

                settingsViewModel
                    .setInboxFolder(
                        treeUri =
                            folders.inboxUri
                                .toString(),

                        displayName =
                            "$rootName/Inbox"
                    )

                settingsViewModel
                    .setLibraryFolder(
                        treeUri =
                            folders.libraryUri
                                .toString(),

                        displayName =
                            "$rootName/Library"
                    )

                tagEditorViewModel.refresh()

                Toast.makeText(
                    this,
                    "Đã dùng $rootName/Inbox và $rootName/Library",
                    Toast.LENGTH_SHORT
                ).show()
            }.onFailure {
                    exception ->

                Toast.makeText(
                    this,
                    exception.message
                        ?: "Không thể chuẩn bị Inbox và Library",
                    Toast.LENGTH_LONG
                ).show()
            }
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
                GetMp3Route(
                    downloadViewModel =
                        downloadViewModel,

                    tagEditorViewModel =
                        tagEditorViewModel,

                    compareViewModel =
                        compareViewModel,

                    metadataRepairViewModel =
                        metadataRepairViewModel,

                    settingsViewModel =
                        settingsViewModel,

                    libraryViewModel =
                        libraryViewModel,

                    lyricsViewModel =
                        lyricsViewModel,
                    youtubeSearchViewModel =
                        youtubeSearchViewModel,
                    onRequestNotificationPermission = {
                        requestNotificationPermission()
                    },

                    onChooseInboxFolder = {
                        musicFolderPicker.launch(
                            null
                        )
                    },

                    onChooseLibraryFolder = {
                        musicFolderPicker.launch(
                            null
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