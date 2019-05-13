package com.arcao.geocaching4locus.error.exception

import android.content.Intent
import com.arcao.geocaching4locus.base.util.marshall
import com.arcao.geocaching4locus.base.util.unmarshall
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class IntendedException(throwable: Throwable, intent: Intent) : Exception(throwable) {
    @Transient
    var intent: Intent = intent
        private set

    @Throws(IOException::class)
    private fun writeObject(out: ObjectOutputStream) {
        out.defaultWriteObject()

        val data = intent.marshall()
        out.writeInt(data.size)
        out.write(data)
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(inputStream: ObjectInputStream) {
        inputStream.defaultReadObject()

        val len = inputStream.readInt()
        val data = ByteArray(len)

        if (inputStream.read(data) != len) throw IOException("Corrupted Parcelable data")
        intent = data.unmarshall(Intent.CREATOR)
    }

    companion object {
        private const val serialVersionUID = -6278705769679870918L
    }
}
