package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.data.api.model.Coordinates
import com.arcao.geocaching4locus.data.api.model.enum.MembershipType
import com.arcao.geocaching4locus.data.api.model.request.query.filter.BoundingBoxFilter
import com.arcao.geocaching4locus.data.api.model.request.query.filter.DifficultyFilter
import com.arcao.geocaching4locus.data.api.model.request.query.filter.DistanceUnit
import com.arcao.geocaching4locus.data.api.model.request.query.filter.Filter
import com.arcao.geocaching4locus.data.api.model.request.query.filter.FoundByFilter
import com.arcao.geocaching4locus.data.api.model.request.query.filter.GeocacheSizeFilter
import com.arcao.geocaching4locus.data.api.model.request.query.filter.GeocacheTypeFilter
import com.arcao.geocaching4locus.data.api.model.request.query.filter.HiddenByFilter
import com.arcao.geocaching4locus.data.api.model.request.query.filter.IsActiveFilter
import com.arcao.geocaching4locus.data.api.model.request.query.filter.IsPublishedFilter
import com.arcao.geocaching4locus.data.api.model.request.query.filter.LevelFilter
import com.arcao.geocaching4locus.data.api.model.request.query.filter.LocationFilter
import com.arcao.geocaching4locus.data.api.model.request.query.filter.RadiusFilter
import com.arcao.geocaching4locus.data.api.model.request.query.filter.TerrainFilter

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
        geocacheTypes: IntArray = intArrayOf(),
        containerTypes: IntArray = intArrayOf(),
        difficultyMin: Float = 1F,
        difficultyMax: Float = 5F,
        terrainMin: Float = 1F,
        terrainMax: Float = 5F,
        excludeIgnoreList: Boolean = true
    ): List<Filter> {
        val filters = mutableListOf<Filter>()

        val account = requireNotNull(accountManager.account)
        val userName = requireNotNull(account.userName)
        val premiumMember = account.isPremium()

        filters += LocationFilter(centerCoordinates)
        filters += RadiusFilter(AppConstants.LIVEMAP_DISTANCE.toFloat(), DistanceUnit.METER)
        filters += BoundingBoxFilter(topLeftCoordinates, bottomRightCoordinates)

        filters += IsPublishedFilter(true)

        if (!disabledGeocaches) {
            filters += IsActiveFilter(true)
        }

        if (!foundGeocaches) {
            filters += FoundByFilter(userName).not()
        }

        if (!ownGeocaches) {
            filters += HiddenByFilter(userName).not()
        }

        if (!premiumMember) {
            filters += LevelFilter(MembershipType.BASIC)
        }

        if (premiumMember) {
            filters += GeocacheTypeFilter(*geocacheTypes)
            filters += GeocacheSizeFilter(*containerTypes)

            if (difficultyMin > 1 || difficultyMax < 5) {
                filters += DifficultyFilter(difficultyMin, difficultyMax)
            }

            if (terrainMin > 1 || terrainMax < 5) {
                filters += TerrainFilter(terrainMin, terrainMax)
            }

            // not supported?
            //filters += BookmarksExcludeFilter(excludeIgnoreList)
        }

        return filters
    }

    operator fun invoke(
        coordinates: Coordinates,
        distanceMeters: Int,
        disabledGeocaches: Boolean = false,
        foundGeocaches: Boolean = false,
        ownGeocaches: Boolean = false,
        geocacheTypes: IntArray = intArrayOf(),
        containerTypes: IntArray = intArrayOf(),
        difficultyMin: Float = 1F,
        difficultyMax: Float = 5F,
        terrainMin: Float = 1F,
        terrainMax: Float = 5F,
        excludeIgnoreList: Boolean = true
    ): List<Filter> {
        val filters = mutableListOf<Filter>()

        val account = requireNotNull(accountManager.account)
        val userName = requireNotNull(account.userName)
        val premiumMember = account.isPremium()

        filters += LocationFilter(coordinates)
        filters += RadiusFilter(distanceMeters.toFloat(), DistanceUnit.METER)

        filters += IsPublishedFilter(true)

        if (!disabledGeocaches) {
            filters += IsActiveFilter(true)
        }

        if (!foundGeocaches) {
            filters += FoundByFilter(userName).not()
        }

        if (!ownGeocaches) {
            filters += HiddenByFilter(userName).not()
        }

        if (!premiumMember) {
            filters += LevelFilter(MembershipType.BASIC)
        }

        if (premiumMember) {
            filters += GeocacheTypeFilter(*geocacheTypes)
            filters += GeocacheSizeFilter(*containerTypes)

            if (difficultyMin > 1 || difficultyMax < 5) {
                filters += DifficultyFilter(difficultyMin, difficultyMax)
            }

            if (terrainMin > 1 || terrainMax < 5) {
                filters += TerrainFilter(terrainMin, terrainMax)
            }

            // not supported?
            //filters += BookmarksExcludeFilter(excludeIgnoreList)
        }

        return filters
    }
}