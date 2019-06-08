package com.arcao.geocaching4locus.base.paging

sealed class DataSourceState {
    object LoadingInitial : DataSourceState()
    object LoadingNext : DataSourceState()
    object Done : DataSourceState()
    class Error(val e: Exception) : DataSourceState()
}
