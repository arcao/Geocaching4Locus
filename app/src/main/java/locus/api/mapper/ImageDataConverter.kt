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
            name = imageData.description.orEmpty()
            description = imageData.description.orEmpty()
            thumbUrl = imageData.thumbnailUrl.orEmpty()
            url = imageData.url
        }
    }
}
