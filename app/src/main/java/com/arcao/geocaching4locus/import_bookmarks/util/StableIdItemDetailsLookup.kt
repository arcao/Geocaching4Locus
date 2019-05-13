package com.arcao.geocaching4locus.import_bookmarks.util

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

class StableIdItemDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)
        if (view != null) {
            val position = recyclerView.getChildAdapterPosition(view)
            val itemId = recyclerView.adapter?.getItemId(position)

            return object : ItemDetails<Long>() {
                override fun getSelectionKey() = itemId
                override fun getPosition() = position
            }
        }
        return null
    }
}