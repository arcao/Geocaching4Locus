package com.arcao.geocaching4locus.import_bookmarks.widget.decorator;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.DimenRes;
import androidx.recyclerview.widget.RecyclerView;

public class BottomMarginLastItemDecorator extends RecyclerView.ItemDecoration {
    private final int bottomMargin;

    public BottomMarginLastItemDecorator(Context context, @DimenRes int dimen) {
        this.bottomMargin = context.getResources().getDimensionPixelSize(dimen);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        // Add bottom margin to last item
        if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1)
            outRect.bottom = bottomMargin;
    }
}