package locus.api.utils

import locus.api.objects.Storable
import java.io.Closeable
import java.io.FileOutputStream
import java.io.IOException

class StoreableWriter @Throws(IOException::class)
constructor(out: FileOutputStream) : Closeable {
    private var closed: Boolean = false
    /**
     * Return count of written Storeable objects
     *
     * @return count of written Storeable objects
     */
    var size: Int = 0
        private set

    private val counterPosition: Int

    private val writer: DataFileWriterBigEndian

    init {
        try {
            size = 0
            writer = DataFileWriterBigEndian(out)
            counterPosition = writer.position
            writer.writeInt(size)
        } catch (e: DataFileWriterBigEndian.DataFileWriterException) {
            throw e.cause
        }
    }

    @Synchronized
    @Throws(IOException::class)
    fun write(obj: Storable) {
        if (closed)
            throw IOException("Output stream closed.")

        try {
            obj.write(writer)
            size++
        } catch (e: DataFileWriterBigEndian.DataFileWriterException) {
            throw e.cause
        }
    }

    @Synchronized
    @Throws(IOException::class)
    override fun close() {
        try {
            if (closed)
                throw IOException("Output stream already closed.")

            closed = true

            val lastPosition = writer.position

            writer.apply {
                moveTo(counterPosition)
                writeInt(size)
                moveTo(lastPosition)

                flush()
                close()
            }
        } catch (e: DataFileWriterBigEndian.DataFileWriterException) {
            throw e.cause
        }
    }
}
