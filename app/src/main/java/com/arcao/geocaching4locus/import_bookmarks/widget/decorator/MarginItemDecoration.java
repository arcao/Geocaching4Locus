package com.arcao.geocaching4locus.import_bookmarks.widget.decorator;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.DimenRes;
import androidx.recyclerview.widget.RecyclerView;

public class MarginItemDecoration extends RecyclerView.ItemDecoration {
    private final int margin;

    public MarginItemDecoration(Context context, @DimenRes int dimen) {
        this.margin = context.getResources().getDimensionPixelSize(dimen);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = margin;
        outRect.right = margin;
        outRect.bottom = margin;

        // Add top margin only for the first item to avoid double margin between items
        if (parent.getChildAdapterPosition(view) == 0)
            outRect.top = margin;
    }
}