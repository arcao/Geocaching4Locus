package com.arcao.geocaching4locus.import_bookmarks.fragment

import android.app.Activity
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.usecase.entity.BookmarkListEntity
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.base.util.observe
import com.arcao.geocaching4locus.databinding.FragmentBookmarkBinding
import com.arcao.geocaching4locus.error.hasPositiveAction
import com.arcao.geocaching4locus.import_bookmarks.adapter.BookmarkGeocachesAdapter
import com.arcao.geocaching4locus.import_bookmarks.util.StableIdItemDetailsLookup
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class BookmarkFragment : Fragment() {
    private val bookmarkList by lazy<BookmarkListEntity> {
        requireNotNull(arguments?.getParcelable(ARG_BOOKMARK_LIST))
    }
    private val viewModel by viewModel<BookmarkViewModel> {
        parametersOf(bookmarkList)
    }

    private val adapter = BookmarkGeocachesAdapter()
    private lateinit var tracker: SelectionTracker<Long>
    private val toolbar get() = (activity as? AppCompatActivity)?.supportActionBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
        toolbar?.subtitle = bookmarkList.name

        if (savedInstanceState == null) {
            viewModel.loadList()
        }

        viewModel.list.observe(this, adapter::submitList)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentBookmarkBinding>(
            inflater,
            R.layout.fragment_bookmark,
            container,
            false
        )

        binding.vm = viewModel
        binding.list.apply {
            adapter = this.adapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        tracker = SelectionTracker.Builder<Long>(
            "bookmark_geocaches",
            binding.list,
            StableIdKeyProvider(binding.list),
            StableIdItemDetailsLookup(binding.list),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build().apply {
            adapter.tracker = this
            addObserver(object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    viewModel.selection(adapter.selection)
                }
            })
        }

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tracker.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tracker.onRestoreInstanceState(savedInstanceState)

        viewModel.action.observe(this, ::handleAction)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_select_deselect, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.selectAll -> {
            adapter.selectAll()
            true
        }
        R.id.deselectAll -> {
            adapter.selectNone()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    fun handleAction(action: BookmarkAction) {
        when (action) {
            is BookmarkAction.Error -> {
                startActivity(action.intent)
                requireActivity().apply {
                    setResult(
                        if (intent.hasPositiveAction())
                            Activity.RESULT_OK
                        else
                            Activity.RESULT_CANCELED
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
        }.exhaustive
    }

    companion object {
        private const val ARG_BOOKMARK_LIST = "bookmarkList"

        fun newInstance(bookmarkList: BookmarkListEntity) = BookmarkListFragment().apply {
            arguments = bundleOf(
                ARG_BOOKMARK_LIST to bookmarkList
            )
        }
    }
}

