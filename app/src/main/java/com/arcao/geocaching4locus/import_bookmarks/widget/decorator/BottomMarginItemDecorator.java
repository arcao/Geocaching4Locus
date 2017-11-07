package com.arcao.geocaching4locus.import_bookmarks.widget.decorator;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class BottomMarginItemDecorator extends RecyclerView.ItemDecoration {
    private final int bottomMargin;

    public BottomMarginItemDecorator(Context context, @DimenRes int bottomMarginRes) {
        this.bottomMargin = context.getResources().getDimensionPixelSize(bottomMarginRes);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        // Add bottom margin to last item
        if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1)
            outRect.bottom = bottomMargin;
    }
}