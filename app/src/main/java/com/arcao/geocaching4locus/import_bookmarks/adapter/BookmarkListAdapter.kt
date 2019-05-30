package com.arcao.geocaching4locus.import_bookmarks.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.usecase.entity.GeocacheListEntity
import com.arcao.geocaching4locus.databinding.ViewBookmarkListItemBinding

class BookmarkListAdapter(
    private val onClickListener: (geocacheList: GeocacheListEntity, importAll: Boolean) -> Unit
) : PagedListAdapter<GeocacheListEntity, BookmarkListAdapter.ViewHolder>(DiffCallback) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int) = getItem(position)?.id ?: RecyclerView.NO_ID

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(DataBindingUtil.inflate(inflater, R.layout.view_bookmark_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = requireNotNull(getItem(position))
        holder.apply {
            binding.item = item
            binding.button.setOnClickListener { onClickListener(item, true) }
            itemView.setOnClickListener { onClickListener(item, false) }
        }
    }

    class ViewHolder(val binding: ViewBookmarkListItemBinding) : RecyclerView.ViewHolder(binding.root)

    private object DiffCallback : DiffUtil.ItemCallback<GeocacheListEntity>() {
        override fun areItemsTheSame(oldItem: GeocacheListEntity, newItem: GeocacheListEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GeocacheListEntity, newItem: GeocacheListEntity): Boolean {
            return oldItem == newItem
        }
    }
}
