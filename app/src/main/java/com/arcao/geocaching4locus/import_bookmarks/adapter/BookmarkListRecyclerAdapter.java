package com.arcao.geocaching4locus.import_bookmarks.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.arcao.geocaching.api.data.bookmarks.BookmarkList;
import com.arcao.geocaching4locus.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookmarkListRecyclerAdapter extends ListAdapter<BookmarkList, BookmarkListRecyclerAdapter.ViewHolder> {
    private static final DiffUtil.ItemCallback<BookmarkList> DIFF_CALLBACK = new DiffUtil.ItemCallback<BookmarkList>() {
        @Override
        public boolean areItemsTheSame(BookmarkList oldItem, BookmarkList newItem) {
            return oldItem.id() == newItem.id();
        }

        @Override
        public boolean areContentsTheSame(BookmarkList oldItem, BookmarkList newItem) {
            return oldItem.equals(newItem);
        }
    };

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(BookmarkList bookmarkList, boolean importAll);
    }

    public BookmarkListRecyclerAdapter(@NonNull OnItemClickListener onItemClickListener) {
        super(DIFF_CALLBACK);
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.view_bookmark_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), onItemClickListener);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.description)
        TextView description;
        @BindView(R.id.count)
        TextView count;
        @BindView(R.id.button)
        Button button;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(final BookmarkList bookmarkList, OnItemClickListener onItemClickListener) {
            title.setText(bookmarkList.name());
            count.setText(itemView.getResources().getQuantityString(R.plurals.plurals_geocache,
                    bookmarkList.itemCount(), bookmarkList.itemCount()));
            description.setText(bookmarkList.description());
            description.setVisibility(TextUtils.isEmpty(bookmarkList.description()) ? View.GONE : View.VISIBLE);
            button.setOnClickListener(view -> onItemClickListener.onItemClick(bookmarkList, true));
            itemView.setOnClickListener(view -> onItemClickListener.onItemClick(bookmarkList, false));
        }
    }
}
