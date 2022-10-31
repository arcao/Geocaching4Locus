package com.arcao.geocaching4locus.import_bookmarks.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.paging.handleErrors
import com.arcao.geocaching4locus.base.usecase.entity.GeocacheListEntity
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.base.util.withObserve
import com.arcao.geocaching4locus.databinding.FragmentBookmarkBinding
import com.arcao.geocaching4locus.error.hasPositiveAction
import com.arcao.geocaching4locus.import_bookmarks.ImportBookmarkViewModel
import com.arcao.geocaching4locus.import_bookmarks.adapter.BookmarkGeocachesAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class BookmarkFragment : BaseBookmarkFragment() {
    private val bookmarkList by lazy<GeocacheListEntity> {
        requireNotNull(arguments?.getParcelable(ARG_BOOKMARK_LIST))
    }
    private val viewModel by viewModel<BookmarkViewModel> {
        parametersOf(bookmarkList)
    }
    private val activityViewModel by sharedViewModel<ImportBookmarkViewModel>()

    private val adapter = BookmarkGeocachesAdapter()
    private val toolbar get() = (activity as? AppCompatActivity)?.supportActionBar

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.toolbar_select_deselect, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
            R.id.selectAll -> {
                adapter.selectAll()
                true
            }
            R.id.deselectAll -> {
                adapter.selectNone()
                true
            }
            else -> false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        toolbar?.subtitle = bookmarkList.name

        val binding = FragmentBookmarkBinding.inflate(
            inflater,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = viewModel
        binding.isLoading = true
        binding.list.apply {
            adapter = this@BookmarkFragment.adapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        adapter.tracker.addSelectionChangeListener { _: Int, _: Int ->
            viewModel.selection(adapter.selected)
        }

        var savedState = savedInstanceState

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pagerFlow.collectLatest { data ->
                adapter.submitData(data)

                if (savedState != null) {
                    adapter.tracker.onRestoreInstanceState(savedState)
                    savedState = null
                }
            }
            adapter.loadStateFlow.collect { state ->
                val isListEmpty = state.refresh is LoadState.NotLoading && adapter.itemCount == 0
                binding.isEmpty = isListEmpty
                binding.isLoading = state.source.refresh is LoadState.Loading
                state.handleErrors(viewModel::handleLoadError)
            }
        }

        viewModel.action.withObserve(viewLifecycleOwner, ::handleAction)
        viewModel.progress.withObserve(viewLifecycleOwner) { state ->
            activityViewModel.progress(state)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        adapter.tracker.onSaveInstanceState(outState)
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    fun handleAction(action: BookmarkAction) {
        when (action) {
            is BookmarkAction.Error -> {
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
            is BookmarkAction.Finish -> {
                startActivity(action.intent)
                requireActivity().apply {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
            BookmarkAction.Cancel -> {
                requireActivity().apply {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
            is BookmarkAction.LoadingError -> {
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
        private const val ARG_BOOKMARK_LIST = "bookmarkList"

        fun newInstance(geocacheList: GeocacheListEntity) = BookmarkFragment().apply {
            arguments = bundleOf(
                ARG_BOOKMARK_LIST to geocacheList
            )
        }
    }
}
