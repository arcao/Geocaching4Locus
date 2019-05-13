package com.arcao.geocaching4locus.data

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.jakewharton.threetenabp.AndroidThreeTen

class GeocachingApiInitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        AndroidThreeTen.init(context)
        return true
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw NotImplementedError()
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        throw NotImplementedError()
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        throw NotImplementedError()
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw NotImplementedError()
    }

    override fun getType(uri: Uri): String? {
        throw NotImplementedError()
    }
}