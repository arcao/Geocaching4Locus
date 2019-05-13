package com.arcao.geocaching4locus.import_bookmarks.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.usecase.entity.BookmarkListEntity
import com.arcao.geocaching4locus.databinding.ViewBookmarkListItemBinding

class BookmarkListAdapter(
    private val onClickListener: (bookmarkList: BookmarkListEntity, importAll: Boolean) -> Unit
) : ListAdapter<BookmarkListEntity, BookmarkListAdapter.ViewHolder>(DiffCallback) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int) = getItem(position).id.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(DataBindingUtil.inflate(inflater, R.layout.view_bookmark_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.apply {
            binding.item = item
            binding.button.setOnClickListener { onClickListener(item, true) }
            itemView.setOnClickListener { onClickListener(item, false) }
        }
    }

    class ViewHolder(val binding: ViewBookmarkListItemBinding) : RecyclerView.ViewHolder(binding.root)

    private object DiffCallback : DiffUtil.ItemCallback<BookmarkListEntity>() {
        override fun areItemsTheSame(oldItem: BookmarkListEntity, newItem: BookmarkListEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BookmarkListEntity, newItem: BookmarkListEntity): Boolean {
            return oldItem == newItem
        }
    }
}
