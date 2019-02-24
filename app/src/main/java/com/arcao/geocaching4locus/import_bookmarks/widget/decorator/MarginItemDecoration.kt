package com.arcao.geocaching4locus.import_bookmarks.widget.decorator

import android.content.Context
import android.graphics.Rect
import android.view.View

import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView

class MarginItemDecoration(context: Context, @DimenRes dimen: Int) : RecyclerView.ItemDecoration() {
    private val margin = context.resources.getDimensionPixelSize(dimen)

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.left = margin
        outRect.right = margin
        outRect.bottom = margin

        // Add top margin only for the first item to avoid double margin between items
        if (parent.getChildAdapterPosition(view) == 0)
            outRect.top = margin
    }
}