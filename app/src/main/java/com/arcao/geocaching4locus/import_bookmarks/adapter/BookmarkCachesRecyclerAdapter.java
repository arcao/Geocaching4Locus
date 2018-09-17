package com.arcao.geocaching4locus.import_bookmarks.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.arcao.geocaching.api.data.bookmarks.Bookmark;
import com.arcao.geocaching4locus.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class BookmarkCachesRecyclerAdapter extends ListAdapter<Bookmark, BookmarkCachesRecyclerAdapter.ViewHolder> {
    private static final DiffUtil.ItemCallback<Bookmark> DIFF_CALLBACK = new DiffUtil.ItemCallback<Bookmark>() {
        @Override
        public boolean areItemsTheSame(Bookmark oldItem, Bookmark newItem) {
            return oldItem.cacheCode().equals(newItem.cacheCode());
        }

        @Override
        public boolean areContentsTheSame(Bookmark oldItem, Bookmark newItem) {
            return oldItem.equals(newItem);
        }
    };

    private boolean[] checked = new boolean[0];

    public BookmarkCachesRecyclerAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.view_bookmark_geocache_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), checked[position], view -> {
            checked[position] = !checked[position];
            notifyItemChanged(position);
        });
    }

    @Override
    public void submitList(List<Bookmark> list) {
        checked = new boolean[list.size()];
        super.submitList(list);
    }

    public List<Bookmark> getCheckedBookmarks() {
        List<Bookmark> result = new ArrayList<>(checkedCount());

        int count = getItemCount();
        for (int i = 0; i < count; i++) {
            if (checked[i]) {
                result.add(getItem(i));
            }
        }

        return result;
    }

    private int checkedCount() {
        int count = 0;
        for (boolean checkedItem : checked) {
            if (checkedItem) count++;
        }

        return count;
    }

    public void selectAll() {
        Arrays.fill(checked, true);
        notifyItemRangeChanged(0, getItemCount());
    }

    public void selectNone() {
        Arrays.fill(checked, false);
        notifyItemRangeChanged(0, getItemCount());
    }

    public boolean isAnyChecked() {
        for (boolean checkedItem : checked) {
            if (checkedItem) return true;
        }

        return false;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.subtitle)
        TextView subtitle;
        @BindView(R.id.checkbox)
        CheckBox checkbox;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(final Bookmark bookmark, boolean checked, View.OnClickListener clickListener) {
            title.setText(bookmark.cacheTitle());
            subtitle.setText(bookmark.cacheCode());
            checkbox.setChecked(checked);
            itemView.setOnClickListener(clickListener);
        }
    }
}
