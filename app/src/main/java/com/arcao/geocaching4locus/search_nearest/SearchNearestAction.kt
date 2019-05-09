package com.arcao.geocaching4locus.search_nearest

import android.content.Intent

sealed class SearchNearestAction {
    object SignIn : SearchNearestAction()
    class Error(val intent: Intent) : SearchNearestAction()
    class Finish(val intent: Intent? = null) : SearchNearestAction()
    object LocusMapNotInstalled : SearchNearestAction()
    object RequestGpsLocationPermission : SearchNearestAction()
    object RequestWifiLocationPermission : SearchNearestAction()
    object WrongCoordinatesFormat : SearchNearestAction()
    object ShowFilters : SearchNearestAction()
}