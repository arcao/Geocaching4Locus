package com.arcao.geocaching4locus.import_bookmarks.paging

import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GetUserListsUseCase
import com.arcao.geocaching4locus.base.usecase.entity.GeocacheListEntity

class GeocacheListsDataSourceFactory(
    private val getUserLists: GetUserListsUseCase,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) : DataSource.Factory<Int, GeocacheListEntity>() {
    val dataSource = MutableLiveData<GeocacheListsDataSource>()

    @WorkerThread
    override fun create(): DataSource<Int, GeocacheListEntity> {
        return GeocacheListsDataSource(getUserLists, dispatcherProvider).also {
            dataSource.postValue(it)
        }
    }
}