package com.arcao.geocaching4locus.import_bookmarks.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.paging.handleErrors
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.base.util.withObserve
import com.arcao.geocaching4locus.databinding.FragmentBookmarkListBinding
import com.arcao.geocaching4locus.error.hasPositiveAction
import com.arcao.geocaching4locus.import_bookmarks.ImportBookmarkViewModel
import com.arcao.geocaching4locus.import_bookmarks.adapter.BookmarkListAdapter
import com.arcao.geocaching4locus.import_bookmarks.widget.decorator.MarginItemDecoration
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class BookmarkListFragment : BaseBookmarkFragment() {
    private val viewModel by viewModel<BookmarkListViewModel>()
    private val activityViewModel by sharedViewModel<ImportBookmarkViewModel>()
    private val toolbar get() = (activity as? AppCompatActivity)?.supportActionBar

    private val adapter = BookmarkListAdapter { bookmarkList, importAll ->
        if (importAll) {
            viewModel.importAll(bookmarkList)
        } else {
            viewModel.chooseBookmarks(bookmarkList)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        toolbar?.subtitle = null

        val binding = DataBindingUtil.inflate<FragmentBookmarkListBinding>(
            inflater,
            R.layout.fragment_bookmark_list,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = viewModel
        binding.isLoading = true
        binding.list.apply {
            adapter = this@BookmarkListFragment.adapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(MarginItemDecoration(context, R.dimen.cardview_space))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.pagerFlow.collectLatest { data ->
                    adapter.submitData(data)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                adapter.loadStateFlow.collect { state ->
                    val isListEmpty =
                        state.refresh is LoadState.NotLoading && adapter.itemCount == 0
                    binding.isEmpty = isListEmpty
                    binding.isLoading = state.source.refresh is LoadState.Loading
                    state.handleErrors(viewModel::handleLoadError)
                }
            }
        }

        viewModel.action.withObserve(viewLifecycleOwner, ::handleAction)
        viewModel.progress.withObserve(viewLifecycleOwner) { state ->
            activityViewModel.progress(state)
        }

        return binding.root
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    fun handleAction(action: BookmarkListAction) {
        when (action) {
            is BookmarkListAction.Error -> {
                startActivity(action.intent)
                requireActivity().apply {
                    setResult(
                        if (action.intent.hasPositiveAction()) {
                            Activity.RESULT_OK
                        } else {
                            Activity.RESULT_CANCELED
                        }
                    )
                    finish()
                }
            }
            is BookmarkListAction.Finish -> {
                startActivity(action.intent)
                requireActivity().apply {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
            BookmarkListAction.Cancel -> {
                requireActivity().apply {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
            is BookmarkListAction.ChooseBookmarks -> {
                activityViewModel.chooseBookmarks(action.geocacheList)
            }
            is BookmarkListAction.LoadingError -> {
                startActivity(action.intent)
                requireActivity().apply {
                    if (adapter.itemCount == 0) {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                }
            }
        }.exhaustive
    }

    override fun onProgressCancel(requestId: Int) {
        viewModel.cancelProgress()
    }

    companion object {
        fun newInstance() = BookmarkListFragment().apply {
            arguments = bundleOf()
        }
    }
}
