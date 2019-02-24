package com.arcao.geocaching4locus.import_bookmarks.fragment

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GetBookmarkUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointsFromGeocacheCodesUseCase
import com.arcao.geocaching4locus.base.usecase.GetUserBookmarkListsUseCase
import com.arcao.geocaching4locus.base.usecase.WritePointToPackPointsFileUseCase
import com.arcao.geocaching4locus.base.usecase.entity.BookmarkListEntity
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.error.exception.IntendedException
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import com.arcao.geocaching4locus.settings.manager.FilterPreferenceManager
import com.arcao.geocaching4locus.update.UpdateActivity
import kotlinx.coroutines.channels.map
import locus.api.manager.LocusMapManager
import timber.log.Timber

@Suppress("EXPERIMENTAL_API_USAGE")
class BookmarkListViewModel(
    private val context: Context,
    private val exceptionHandler: ExceptionHandler,
    private val getUserBookmarkListsUseCase: GetUserBookmarkListsUseCase,
    private val getBookmarkUseCase: GetBookmarkUseCase,
    private val getPointsFromGeocacheCodesUseCase: GetPointsFromGeocacheCodesUseCase,
    private val writePointToPackPointsFileUseCase: WritePointToPackPointsFileUseCase,
    private val filterPreferenceManager: FilterPreferenceManager,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider) {
    val loading = MutableLiveData<Boolean>().apply {
        value = true
    }
    val list = MutableLiveData<List<BookmarkListEntity>>().apply {
        value = emptyList()
    }
    val action = Command<BookmarkListAction>()

    fun loadList() = mainLaunch {
        loading.postValue(true)

        computationContext {
            try {
                list.postValue(getUserBookmarkListsUseCase())
            } catch (e: Exception) {
                action.postValue(BookmarkListAction.Error(exceptionHandler(e)))
            } finally {
                loading.postValue(false)
            }
        }
    }

    fun importAll(bookmarkList: BookmarkListEntity) = computationLaunch {
        val importIntent = LocusMapManager.createSendPointsIntent(
            callImport = true,
            center = true
        )

        var receivedGeocaches = 0

        try {
            showProgress(R.string.progress_download_geocaches, maxProgress = 1) {
                Timber.d("source: import_from_bookmark;guid=%s", bookmarkList.guid)
                val bookmark = getBookmarkUseCase(bookmarkList.guid)

                val geocacheCodes = bookmark.map { it.code }.toTypedArray()
                Timber.d("source: import_from_bookmark;gccodes=%s", geocacheCodes)

                val channel = getPointsFromGeocacheCodesUseCase(
                    geocacheCodes,
                    filterPreferenceManager.simpleCacheData,
                    filterPreferenceManager.geocacheLogsCount
                ).map { list ->
                    receivedGeocaches += list.size
                    updateProgress(progress = receivedGeocaches)

                    // apply additional downloading full geocache if required
                    if (filterPreferenceManager.simpleCacheData) {
                        list.forEach { point ->
                            point.setExtraOnDisplay(
                                context.packageName,
                                UpdateActivity::class.java.name,
                                UpdateActivity.PARAM_SIMPLE_CACHE_ID,
                                point.gcData.cacheID
                            )
                        }
                    }
                    list
                }
                writePointToPackPointsFileUseCase(channel)
            }
        } catch (e: Exception) {
            mainContext {
                action(
                    BookmarkListAction.Error(
                        if (receivedGeocaches > 0) {
                            exceptionHandler(IntendedException(e, importIntent))
                        } else {
                            exceptionHandler(e)
                        }
                    )
                )
            }
            return@computationLaunch
        }

        mainContext {
            action(BookmarkListAction.Finish(importIntent))
        }
    }

    fun chooseBookmarks(bookmarkList: BookmarkListEntity) {
        action(BookmarkListAction.ChooseBookmarks(bookmarkList))
    }
}