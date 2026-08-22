package com.ngoctien.getmp3.storage

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract

data class MusicFolders(
    val inboxUri: Uri,
    val libraryUri: Uri
)

class MusicFolderProvisioner(
    context: Context
) {
    private val resolver =
        context.applicationContext
            .contentResolver

    fun ensure(
        rootTreeUri: Uri
    ): MusicFolders {
        require(
            DocumentsContract.isTreeUri(
                rootTreeUri
            )
        ) {
            "Music không phải SAF Tree URI hợp lệ"
        }

        val rootDocumentId =
            DocumentsContract
                .getTreeDocumentId(
                    rootTreeUri
                )

        return MusicFolders(
            inboxUri =
                findOrCreateDirectory(
                    rootTreeUri =
                        rootTreeUri,
                    parentDocumentId =
                        rootDocumentId,
                    displayName =
                        "Inbox"
                ),
            libraryUri =
                findOrCreateDirectory(
                    rootTreeUri =
                        rootTreeUri,
                    parentDocumentId =
                        rootDocumentId,
                    displayName =
                        "Library"
                )
        )
    }

    private fun findOrCreateDirectory(
        rootTreeUri: Uri,
        parentDocumentId: String,
        displayName: String
    ): Uri {
        val childrenUri =
            DocumentsContract
                .buildChildDocumentsUriUsingTree(
                    rootTreeUri,
                    parentDocumentId
                )

        val projection =
            arrayOf(
                DocumentsContract
                    .Document
                    .COLUMN_DOCUMENT_ID,
                DocumentsContract
                    .Document
                    .COLUMN_DISPLAY_NAME,
                DocumentsContract
                    .Document
                    .COLUMN_MIME_TYPE
            )

        val cursor =
            resolver.query(
                childrenUri,
                projection,
                null,
                null,
                null
            )

        if (cursor != null) {
            try {
                val idIndex =
                    cursor.getColumnIndexOrThrow(
                        DocumentsContract
                            .Document
                            .COLUMN_DOCUMENT_ID
                    )

                val nameIndex =
                    cursor.getColumnIndexOrThrow(
                        DocumentsContract
                            .Document
                            .COLUMN_DISPLAY_NAME
                    )

                val mimeIndex =
                    cursor.getColumnIndexOrThrow(
                        DocumentsContract
                            .Document
                            .COLUMN_MIME_TYPE
                    )

                while (cursor.moveToNext()) {
                    val childName =
                        cursor.getString(
                            nameIndex
                        ).orEmpty()

                    if (
                        !childName.equals(
                            displayName,
                            ignoreCase = true
                        )
                    ) {
                        continue
                    }

                    val mimeType =
                        cursor.getString(
                            mimeIndex
                        ).orEmpty()

                    if (
                        mimeType !=
                        DocumentsContract
                            .Document
                            .MIME_TYPE_DIR
                    ) {
                        throw IllegalStateException(
                            "$displayName đã tồn tại nhưng không phải thư mục"
                        )
                    }

                    val documentId =
                        cursor.getString(
                            idIndex
                        )

                    return DocumentsContract
                        .buildDocumentUriUsingTree(
                            rootTreeUri,
                            documentId
                        )
                }
            } finally {
                cursor.close()
            }
        }

        val parentUri =
            DocumentsContract
                .buildDocumentUriUsingTree(
                    rootTreeUri,
                    parentDocumentId
                )

        return DocumentsContract
            .createDocument(
                resolver,
                parentUri,
                DocumentsContract
                    .Document
                    .MIME_TYPE_DIR,
                displayName
            )
            ?: throw IllegalStateException(
                "Không thể tạo thư mục $displayName"
            )
    }
}

/*
 * A child SAF URI built from a selected tree keeps the original
 * /tree/<root> segment. getTreeDocumentId() therefore still points
 * at Music. For a child document URI we must use its document ID
 * so callers operate inside Music/Inbox or Music/Library.
 *
 * Existing settings may still contain a plain tree URI, so fall
 * back to getTreeDocumentId() when no document ID is available.
 */
fun resolveSafDirectoryDocumentId(
    uri: Uri
): String {
    require(
        DocumentsContract.isTreeUri(
            uri
        )
    ) {
        "URI thư mục SAF không hợp lệ"
    }

    return runCatching {
        DocumentsContract
            .getDocumentId(
                uri
            )
    }
        .getOrElse {
            DocumentsContract
                .getTreeDocumentId(
                    uri
                )
        }
}
