package locus.api.mapper;

import androidx.annotation.Nullable;

import com.arcao.geocaching.api.data.ImageData;

import locus.api.objects.geocaching.GeocachingImage;

final class ImageDataConverter {
    @Nullable
    GeocachingImage createLocusGeocachingImage(@Nullable ImageData imageData) {
        if (imageData == null)
            return null;

        GeocachingImage image = new GeocachingImage();
        image.setName(imageData.name());
        image.setDescription(imageData.description());
        image.setThumbUrl(imageData.thumbUrl());
        image.setUrl(imageData.url());

        return image;
    }
}
