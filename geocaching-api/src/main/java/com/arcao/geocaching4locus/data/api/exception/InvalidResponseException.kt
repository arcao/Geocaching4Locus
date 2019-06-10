package com.arcao.geocaching4locus.data.api.exception

import java.io.IOException

class InvalidResponseException(message: String?, cause: Throwable) : IOException(message, cause)
