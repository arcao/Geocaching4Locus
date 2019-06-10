package com.arcao.geocaching4locus.import_bookmarks.paging

import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GetListGeocachesUseCase
import com.arcao.geocaching4locus.base.usecase.entity.ListGeocacheEntity

class ListGeocachesDataSourceFactory(
    private val getListGeocaches: GetListGeocachesUseCase,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) : DataSource.Factory<Int, ListGeocacheEntity>() {
    val dataSource = MutableLiveData<ListGeocachesDataSource>()

    var referenceCode: String? = null

    @WorkerThread
    override fun create(): DataSource<Int, ListGeocacheEntity> {
        return ListGeocachesDataSource(requireNotNull(referenceCode), getListGeocaches, dispatcherProvider).also {
            dataSource.postValue(it)
        }
    }
}
