package com.arcao.wherigoservice.api

import java.io.IOException

class WherigoServiceException @JvmOverloads constructor(val code: Int, message: String?, t: Throwable? = null) :
    IOException(message) {
    init {
        initCause(t)
    }

    override fun toString(): String {
        return super.toString() + " (" + code + ")"
    }

    companion object {
        private const val serialVersionUID = -1298236380965518822L

        const val ERROR_OK = 0
        const val ERROR_INVALID_CREDENTIALS = 1
        const val ERROR_INVALID_SESSION = 2
        const val ERROR_CARTRIDGE_NOT_FOUND = 10
        const val ERROR_CACHE_NOT_FOUND = 11
        const val ERROR_API_ERROR = 500
        const val ERROR_CONNECTION_ERROR = 501
    }
}
