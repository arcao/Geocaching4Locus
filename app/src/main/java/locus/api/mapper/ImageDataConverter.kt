package locus.api.mapper

import androidx.annotation.Nullable
import com.arcao.geocaching.api.data.ImageData
import locus.api.objects.geocaching.GeocachingImage

class ImageDataConverter {
    @Nullable
    fun createLocusGeocachingImage(@Nullable imageData: ImageData?): GeocachingImage? {
        if (imageData == null)
            return null

        return GeocachingImage().apply {
            name = imageData.name()
            description = imageData.description()
            thumbUrl = imageData.thumbUrl()
            url = imageData.url()
        }
    }
}
