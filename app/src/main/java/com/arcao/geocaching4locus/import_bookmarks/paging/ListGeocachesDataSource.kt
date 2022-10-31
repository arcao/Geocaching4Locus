package com.arcao.geocaching4locus.import_bookmarks.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.arcao.geocaching4locus.base.usecase.GetListGeocachesUseCase
import com.arcao.geocaching4locus.base.usecase.entity.ListGeocacheEntity
import kotlinx.coroutines.CancellationException

class ListGeocachesDataSource(
    private val referenceCode: String,
    private val getListGeocaches: GetListGeocachesUseCase,
    private val pagingConfig: PagingConfig
) : PagingSource<Int, ListGeocacheEntity>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListGeocacheEntity> {
        return try {
            val page = params.key ?: 0
            val skip = page * params.loadSize
            val response = getListGeocaches(
                referenceCode = referenceCode,
                skip = skip,
                take = params.loadSize
            )

            val currentCount = skip + response.size
            val totalCount = response.totalCount

            LoadResult.Page(
                data = response,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (currentCount < totalCount) page + (params.loadSize / pagingConfig.pageSize) else null

            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListGeocacheEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}