package com.arcao.geocaching4locus.import_bookmarks.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.usecase.entity.BookmarkEntity
import com.arcao.geocaching4locus.databinding.ViewBookmarkItemBinding

class BookmarkGeocachesAdapter : ListAdapter<BookmarkEntity, BookmarkGeocachesAdapter.ViewHolder>(DiffCallback) {
    var tracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    val selection: List<BookmarkEntity>
        get() {
            val result = mutableListOf<BookmarkEntity>()
            val tracker = tracker?: return result

            val count = itemCount
            for (i in 0 until count) {
                if (tracker.isSelected(i.toLong())) {
                    result.add(getItem(i))
                }
            }

            return result
        }

    val isAnySelected: Boolean get() = tracker?.hasSelection() ?: false

    override fun getItemId(position: Int) = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(DataBindingUtil.inflate(inflater, R.layout.view_bookmark_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.item = getItem(position)
        holder.binding.checkbox.isChecked = tracker?.isSelected(position.toLong()) ?: false
    }

    fun selectAll() {
        tracker?.setItemsSelected(0L until itemCount, true)
    }

    fun selectNone() {
        tracker?.clearSelection()
    }

    class ViewHolder(val binding: ViewBookmarkItemBinding) : RecyclerView.ViewHolder(binding.root)

    private object DiffCallback : DiffUtil.ItemCallback<BookmarkEntity>() {
        override fun areItemsTheSame(oldItem: BookmarkEntity, newItem: BookmarkEntity): Boolean {
            return oldItem.code == newItem.code
        }

        override fun areContentsTheSame(oldItem: BookmarkEntity, newItem: BookmarkEntity): Boolean {
            return oldItem == newItem
        }
    }
}
