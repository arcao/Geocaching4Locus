package com.arcao.geocaching4locus.import_bookmarks.paging

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.paging.DataSourceState
import com.arcao.geocaching4locus.base.usecase.GetListGeocachesUseCase
import com.arcao.geocaching4locus.base.usecase.entity.ListGeocacheEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ListGeocachesDataSource(
    private val referenceCode: String,
    private val getListGeocaches: GetListGeocachesUseCase,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) : PageKeyedDataSource<Int, ListGeocacheEntity>(), CoroutineScope {
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + dispatcherProvider.computation

    val state = MutableLiveData<DataSourceState>().apply {
        postValue(DataSourceState.LoadingInitial)
    }

    init {
        addInvalidatedCallback(object : InvalidatedCallback {
            override fun onInvalidated() {
                removeInvalidatedCallback(this)
                job.cancel()
            }
        })
    }

    @WorkerThread
    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, ListGeocacheEntity>) {
        state.postValue(DataSourceState.LoadingInitial)

        job.cancelChildren()
        launch {
            try {
                val response = getListGeocaches(
                    referenceCode = referenceCode,
                    skip = 0,
                    take = params.requestedLoadSize
                )

                val itemCount = response.totalCount
                val hasNext = response.size < itemCount
                callback.onResult(
                    response,
                    0,
                    itemCount.toInt(),
                    null,
                    if (hasNext) response.size else null
                )

                state.postValue(DataSourceState.Done)
            } catch (e: Exception) {
                state.postValue(DataSourceState.Error(e))
            }
        }
    }

    @WorkerThread
    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, ListGeocacheEntity>) {
        state.postValue(DataSourceState.LoadingNext)

        job.cancelChildren()
        launch {
            try {
                val response = getListGeocaches(
                    referenceCode = referenceCode,
                    skip = params.key,
                    take = params.requestedLoadSize
                )

                val itemCount = response.totalCount
                val hasNext = (params.key + response.size) < itemCount

                callback.onResult(
                    response,
                    if (hasNext) params.key + response.size else null
                )
                state.postValue(DataSourceState.Done)
            } catch (e: Exception) {
                state.postValue(DataSourceState.Error(e))
            }
        }
    }

    @MainThread
    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, ListGeocacheEntity>) {
    }
}