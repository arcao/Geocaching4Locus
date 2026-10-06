package com.arcao.geocaching4locus.data.api.util

object ImageUrl {
    // an original image on the Geocaching image server, e.g. https://img.geocaching.com/<guid>.jpg
    private val ORIGINAL_IMAGE_URL = Regex("""^(https?://img\.geocaching\.com/)([^/?#]+)$""", RegexOption.IGNORE_CASE)

    /**
     * Returns the URL of the downsized (about 640 px) version of an image from the Geocaching image
     * server. The original images can be very large (up to a few MB). Any other URL is returned
     * unchanged.
     */
    fun toLarge(url: String): String {
        return ORIGINAL_IMAGE_URL.replace(url.trim(), "$1large/$2")
    }
}
