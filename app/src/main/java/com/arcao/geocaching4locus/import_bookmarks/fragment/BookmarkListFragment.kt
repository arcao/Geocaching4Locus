package com.arcao.geocaching4locus.import_bookmarks.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.observe
import com.arcao.geocaching4locus.databinding.FragmentBookmarkListBinding
import com.arcao.geocaching4locus.error.hasPositiveAction
import com.arcao.geocaching4locus.import_bookmarks.ImportBookmarkViewModel
import com.arcao.geocaching4locus.import_bookmarks.adapter.BookmarkListAdapter
import com.arcao.geocaching4locus.import_bookmarks.widget.decorator.MarginItemDecoration
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class BookmarkListFragment : Fragment() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbar?.subtitle = null

        if (savedInstanceState == null) {
            viewModel.loadList()
        }

        viewModel.list.observe(this, adapter::submitList)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentBookmarkListBinding>(
            inflater,
            R.layout.fragment_bookmark_list,
            container,
            false
        )

        binding.vm = viewModel
        binding.list.apply {
            adapter = this.adapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(MarginItemDecoration(context, R.dimen.cardview_space))
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.action.observe(this, ::handleAction)
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    fun handleAction(action: BookmarkListAction) {
        when (action) {
            is BookmarkListAction.Error -> {
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
                activityViewModel.chooseBookmarks(action.bookmarkList)
            }
        }.exhaustive
    }

    companion object {
        fun newInstance() = BookmarkListFragment().apply {
            arguments = bundleOf()
        }
    }
}
