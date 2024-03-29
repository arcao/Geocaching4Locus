package com.arcao.geocaching4locus.import_bookmarks.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.selection.SelectionAdapter
import com.arcao.geocaching4locus.base.selection.SelectionTracker
import com.arcao.geocaching4locus.base.usecase.entity.ListGeocacheEntity
import com.arcao.geocaching4locus.databinding.ViewBookmarkItemBinding

class BookmarkGeocachesAdapter :
    PagingDataAdapter<ListGeocacheEntity, BookmarkGeocachesAdapter.ViewHolder>(DiffCallback) {

    val tracker by lazy {
        val selectionAdapter = object : SelectionAdapter<ListGeocacheEntity> {
            override val itemCount: Int
                get() = snapshot().size

            override fun registerAdapterDataObserver(adapterDataObserver: RecyclerView.AdapterDataObserver) =
                this@BookmarkGeocachesAdapter.registerAdapterDataObserver(adapterDataObserver)

            override fun findPosition(value: ListGeocacheEntity): Int = snapshot().indexOf(value)

            override fun getItem(position: Int) = snapshot()[position]
        }

        SelectionTracker(selectionAdapter).apply {
            addSelectionChangeListener { startPosition: Int, count: Int ->
                notifyItemRangeChanged(startPosition, count)
            }
        }
    }

    val selected: List<ListGeocacheEntity>
        get() = tracker.selectedValues

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            DataBindingUtil.inflate(
                inflater,
                R.layout.view_bookmark_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = requireNotNull(getItem(position))
        holder.binding.item = item
        holder.binding.checkbox.isChecked = tracker.isSelected(position)
        holder.binding.root.setOnClickListener {
            tracker.onClick(position, item)
        }
    }

    fun selectAll() {
        tracker.selectAll()
    }

    fun selectNone() {
        tracker.selectNone()
    }

    class ViewHolder(val binding: ViewBookmarkItemBinding) : RecyclerView.ViewHolder(binding.root)

    private object DiffCallback : DiffUtil.ItemCallback<ListGeocacheEntity>() {
        override fun areItemsTheSame(
            oldItem: ListGeocacheEntity,
            newItem: ListGeocacheEntity
        ): Boolean {
            return oldItem.referenceCode == newItem.referenceCode
        }

        override fun areContentsTheSame(
            oldItem: ListGeocacheEntity,
            newItem: ListGeocacheEntity
        ): Boolean {
            return oldItem == newItem
        }
    }
}
