/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.twoyi.provider;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.provider.DocumentsProvider;
import android.webkit.MimeTypeMap;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import io.twoyi.R;
import io.twoyi.utils.RomUtil;

/**
 * @author weishu
 * @date 2022/2/16.
 */
public class TwoyiDocumentsProvider extends DocumentsProvider {

    private static final String ALL_MIME_TYPES = "*/*";

    private static final String DEFAULT_ROOT_ID = "0";

    private static final String[] DEFAULT_ROOT_PROJECTION = new String[]{
            Root.COLUMN_ROOT_ID,
            Root.COLUMN_MIME_TYPES,
            Root.COLUMN_FLAGS,
            Root.COLUMN_ICON,
            Root.COLUMN_TITLE,
            Root.COLUMN_SUMMARY,
            Root.COLUMN_DOCUMENT_ID,
            Root.COLUMN_AVAILABLE_BYTES
    };

    private static final String[] DEFAULT_DOCUMENT_PROJECTION = new String[]{
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_LAST_MODIFIED,
            Document.COLUMN_FLAGS,
            Document.COLUMN_SIZE
    };

    @Override
    public Cursor queryRoots(String[] projection) {

        File BASE_DIR = RomUtil.getRomSdcardDir(getContext());

        final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_ROOT_PROJECTION);
        final String applicationName = getContext().getString(R.string.app_name);

        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Root.COLUMN_ROOT_ID, DEFAULT_ROOT_ID);
        row.add(Root.COLUMN_DOCUMENT_ID, getDocId(BASE_DIR));
        row.add(Root.COLUMN_SUMMARY, getRootSummary());
        row.add(Root.COLUMN_FLAGS, Root.FLAG_SUPPORTS_CREATE | Root.FLAG_LOCAL_ONLY | Root.FLAG_SUPPORTS_IS_CHILD);
        row.add(Root.COLUMN_TITLE, applicationName);
        row.add(Root.COLUMN_MIME_TYPES, ALL_MIME_TYPES);
        row.add(Root.COLUMN_AVAILABLE_BYTES, BASE_DIR.getFreeSpace());
        row.add(Root.COLUMN_ICON, R.mipmap.ic_launcher);

        return result;
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);
        includeFile(result, documentId, null);
        return result;
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
        final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);
        final File parent = getFileById(parentDocumentId);
        File[] files = parent.listFiles();
        if (files != null) {
            for (File file : files) {
                includeFile(result, null, file);
            }
        }
        return result;
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, @Nullable CancellationSignal signal) throws FileNotFoundException {
        final File file = getFileById(documentId);
        final int accessMode = ParcelFileDescriptor.parseMode(mode);
        return ParcelFileDescriptor.open(file, accessMode);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String createDocument(String parentDocumentId, String mimeType, String displayName) throws FileNotFoundException {
        File newFile = new File(parentDocumentId, displayName);
        int noConflictId = 2;
        while (newFile.exists()) {
            newFile = new File(parentDocumentId, displayName + " (" + noConflictId++ + ")");
        }
        try {
            boolean succeeded;
            if (Document.MIME_TYPE_DIR.equals(mimeType)) {
                succeeded = newFile.mkdir();
            } else {
                succeeded = newFile.createNewFile();
            }
            if (!succeeded) {
                throw new FileNotFoundException("Failed to create document: " + newFile.getPath());
            }
        } catch (IOException e) {
            throw new FileNotFoundException("Failed to create document:" + newFile.getPath());
        }
        return newFile.getPath();
    }

    @Override
    public void deleteDocument(String documentId) throws FileNotFoundException {
        File file = getFileById(documentId);
        if (!file.delete()) {
            throw new FileNotFoundException("Failed to delete document:" + documentId);
        }
    }

    @Override
    public String getDocumentType(String documentId) throws FileNotFoundException {
        File file = getFileById(documentId);
        return getMimeType(file);
    }

    @Override
    public boolean isChildDocument(String parentDocumentId, String documentId) {
        if (parentDocumentId == null || documentId == null) {
            return false;
        }
        return documentId.startsWith(parentDocumentId);
    }

    private static String getDocId(File file) {
        return file.getAbsolutePath();
    }

    private static File getFileById(String docId) throws FileNotFoundException {
        final File f = new File(docId);
        if (!f.exists()) throw new FileNotFoundException(f.getAbsolutePath() + " not found");
        return f;
    }

    private static String getMimeType(File file) {
        if (file.isDirectory()) {
            return Document.MIME_TYPE_DIR;
        } else {
            final String name = file.getName();
            final int lastDot = name.lastIndexOf('.');
            if (lastDot >= 0) {
                final String extension = name.substring(lastDot + 1).toLowerCase();
                final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                if (mime != null) return mime;
            }
            return "application/octet-stream";
        }
    }

    private File getRootDir() {
        return RomUtil.getRomSdcardDir(getContext());
    }

    private String getRootSummary() {
        return getContext().getResources().getString(R.string.sdcard_of_twoyi);
    }

    private void includeFile(MatrixCursor result, String docId, File file)
            throws FileNotFoundException {
        if (docId == null) {
            docId = getDocId(file);
        } else {
            file = getFileById(docId);
        }

        int flags = 0;
        if (file.isDirectory()) {
            if (file.canWrite()) flags |= Document.FLAG_DIR_SUPPORTS_CREATE;
        } else if (file.canWrite()) {
            flags |= Document.FLAG_SUPPORTS_WRITE;
        }

        File parentFile = file.getParentFile();
        if (parentFile != null && parentFile.canWrite()) flags |= Document.FLAG_SUPPORTS_DELETE;

        boolean isRoot = file.equals(getRootDir());

        String displayName = isRoot ? getRootSummary() : file.getName();
        final String mimeType = getMimeType(file);
        if (mimeType.startsWith("image/")) flags |= Document.FLAG_SUPPORTS_THUMBNAIL;

        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, docId);
        row.add(Document.COLUMN_DISPLAY_NAME, displayName);
        row.add(Document.COLUMN_SIZE, file.length());
        row.add(Document.COLUMN_MIME_TYPE, mimeType);
        row.add(Document.COLUMN_LAST_MODIFIED, file.lastModified());
        row.add(Document.COLUMN_FLAGS, flags);
        row.add(Document.COLUMN_ICON, R.mipmap.ic_launcher);
    }
}
