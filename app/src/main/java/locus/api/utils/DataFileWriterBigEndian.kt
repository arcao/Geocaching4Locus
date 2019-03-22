package locus.api.utils

import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.channels.FileChannel

internal class DataFileWriterBigEndian(out: FileOutputStream) : DataWriterBigEndian(0) {
    private var storedPosition: Long = 0

    private val out: OutputStream
    private val channel: FileChannel

    val position: Int
        @Synchronized get() {
            try {
                out.flush()
                return channel.position().toInt()
            } catch (e: IOException) {
                throw DataFileWriterException(e)
            }
        }

    init {
        this.out = BufferedOutputStream(out)
        channel = out.channel
    }

    @Synchronized
    override fun write(b: Int) {
        try {
            out.write(b)
        } catch (e: IOException) {
            throw DataFileWriterException(e)
        }
    }

    @Synchronized
    override fun write(b: ByteArray, off: Int, len: Int) {
        try {
            out.write(b, off, len)
        } catch (e: IOException) {
            throw DataFileWriterException(e)
        }
    }

    override fun storePosition() {
        try {
            out.flush()
            storedPosition = channel.position()
        } catch (e: IOException) {
            throw DataFileWriterException(e)
        }
    }

    override fun restorePosition() {
        try {
            out.flush()
            channel.position(storedPosition)
        } catch (e: IOException) {
            throw DataFileWriterException(e)
        }
    }

    override fun moveTo(index: Int) {
        try {
            out.flush()

            // check index
            if (index < 0 || index > channel.size()) {
                throw IllegalArgumentException(
                        "Invalid move index:" + index + ", count:" + channel.size())
            }

            channel.position(index.toLong())
        } catch (e: IOException) {
            throw DataFileWriterException(e)
        }
    }

    @Synchronized
    override fun writeTo(out: OutputStream) {
        throw UnsupportedOperationException("Not supported")
    }

    @Synchronized
    override fun toByteArray(): ByteArray {
        throw UnsupportedOperationException("Not supported")
    }

    @Synchronized
    override fun size(): Int {
        try {
            out.flush()
            return channel.size().toInt()
        } catch (e: IOException) {
            throw DataFileWriterException(e)
        }
    }

    @Synchronized
    @Throws(IOException::class)
    fun flush() {
        out.flush()
    }

    @Synchronized
    @Throws(IOException::class)
    fun close() {
        out.close()
    }

    internal class DataFileWriterException(throwable: IOException) : RuntimeException(throwable) {
        override val cause: IOException
            get() = super.cause as IOException

        companion object {
            private const val serialVersionUID = 2678019269077897465L
        }
    }
}
