package locus.api.mapper

import androidx.annotation.Nullable
import com.arcao.geocaching4locus.data.api.model.Image
import locus.api.objects.geocaching.GeocachingImage

class ImageDataConverter {
    @Nullable
    fun createLocusGeocachingImage(@Nullable imageData: Image?): GeocachingImage? {
        if (imageData == null)
            return null

        return GeocachingImage().apply {
            name = imageData.description
            description = imageData.description
            thumbUrl = imageData.thumbnailUrl
            url = imageData.url
        }
    }
}
