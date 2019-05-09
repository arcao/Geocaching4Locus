package com.arcao.geocaching4locus.import_bookmarks.fragment

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GetBookmarkUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointsFromGeocacheCodesUseCase
import com.arcao.geocaching4locus.base.usecase.WritePointToPackPointsFileUseCase
import com.arcao.geocaching4locus.base.usecase.entity.BookmarkEntity
import com.arcao.geocaching4locus.base.usecase.entity.BookmarkListEntity
import com.arcao.geocaching4locus.base.util.AnalyticsUtil
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
class BookmarkViewModel(
        private val bookmarkList: BookmarkListEntity,
        private val context: Context,
        private val exceptionHandler: ExceptionHandler,
        private val getBookmark: GetBookmarkUseCase,
        private val getPointsFromGeocacheCodes: GetPointsFromGeocacheCodesUseCase,
        private val writePointToPackPointsFile: WritePointToPackPointsFileUseCase,
        private val filterPreferenceManager: FilterPreferenceManager,
        private val locusMapManager: LocusMapManager,
        dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider) {
    val loading = MutableLiveData<Boolean>().apply {
        value = true
    }
    val list = MutableLiveData<List<BookmarkEntity>>().apply {
        value = emptyList()
    }
    val selection = MutableLiveData<List<BookmarkEntity>>().apply {
        value = emptyList()
    }
    val action = Command<BookmarkAction>()

    fun loadList() = mainLaunch {
        loading(true)

        try {
            list(getBookmark(bookmarkList.guid))
        } catch (e: Exception) {
            action(BookmarkAction.Error(exceptionHandler(e)))
        } finally {
            loading(false)
        }
    }

    fun import() = mainLaunch {
        val selection = selection.value ?: return@mainLaunch
        AnalyticsUtil.actionImportBookmarks(selection.size, false)

        computationLaunch {
            val geocacheCodes = selection.map { it.code }.toTypedArray()
            Timber.d("source: import_from_bookmark;gccodes=%s", geocacheCodes)

            val importIntent = locusMapManager.createSendPointsIntent(
                    callImport = true,
                    center = true
            )

            var receivedGeocaches = 0

            try {
                showProgress(R.string.progress_download_geocaches, maxProgress = geocacheCodes.size) {
                    val channel = getPointsFromGeocacheCodes(
                            this,
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
                    writePointToPackPointsFile(channel)
                }
            } catch (e: Exception) {
                mainContext {
                    action(
                            BookmarkAction.Error(
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
                action(BookmarkAction.Finish(importIntent))
            }
        }
    }
}