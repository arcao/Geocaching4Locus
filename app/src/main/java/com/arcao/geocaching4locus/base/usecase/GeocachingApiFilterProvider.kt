package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching.api.data.coordinates.Coordinates
import com.arcao.geocaching.api.data.type.ContainerType
import com.arcao.geocaching.api.data.type.GeocacheType
import com.arcao.geocaching.api.filter.BookmarksExcludeFilter
import com.arcao.geocaching.api.filter.DifficultyFilter
import com.arcao.geocaching.api.filter.Filter
import com.arcao.geocaching.api.filter.GeocacheContainerSizeFilter
import com.arcao.geocaching.api.filter.GeocacheExclusionsFilter
import com.arcao.geocaching.api.filter.GeocacheTypeFilter
import com.arcao.geocaching.api.filter.NotFoundByUsersFilter
import com.arcao.geocaching.api.filter.NotHiddenByUsersFilter
import com.arcao.geocaching.api.filter.PointRadiusFilter
import com.arcao.geocaching.api.filter.TerrainFilter
import com.arcao.geocaching.api.filter.ViewportFilter
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.constants.AppConstants
import java.util.ArrayList

class GeocachingApiFilterProvider(
    private val accountManager: AccountManager
) {
    operator fun invoke(
        centerCoordinates: Coordinates,
        topLeftCoordinates: Coordinates,
        bottomRightCoordinates: Coordinates,
        disabledGeocaches: Boolean = false,
        foundGeocaches: Boolean = false,
        ownGeocaches: Boolean = false,
        geocacheTypes: Array<GeocacheType> = emptyArray(),
        containerTypes: Array<ContainerType> = emptyArray(),
        difficultyMin : Float = 1F,
        difficultyMax: Float = 5F,
        terrainMin : Float = 1F,
        terrainMax: Float = 5F,
        excludeIgnoreList : Boolean = true
    ): List<Filter> {
        val filters = ArrayList<Filter>(9)

        val userName = requireNotNull(accountManager.account).name
        val premiumMember = accountManager.isPremium

        filters += PointRadiusFilter(centerCoordinates, AppConstants.LIVEMAP_DISTANCE.toLong())
        filters += ViewportFilter(topLeftCoordinates, bottomRightCoordinates)

        filters += GeocacheExclusionsFilter(
            false,
            if (disabledGeocaches)
                null
            else
                true
            ,
            null,
            null,
            null,
            null
        )

        if (!foundGeocaches) {
            filters += NotFoundByUsersFilter(userName)
        }

        if (!ownGeocaches) {
            filters += NotHiddenByUsersFilter(userName)
        }

        if (premiumMember) {
            filters += GeocacheTypeFilter(*geocacheTypes)
            filters += GeocacheContainerSizeFilter(*containerTypes)

            if (difficultyMin > 1 || difficultyMax < 5) {
                filters += DifficultyFilter(difficultyMin, difficultyMax)
            }

            if (terrainMin > 1 || terrainMax < 5) {
                filters += TerrainFilter(terrainMin, terrainMax)
            }

            filters += BookmarksExcludeFilter(excludeIgnoreList)
        }

        return filters
    }
}