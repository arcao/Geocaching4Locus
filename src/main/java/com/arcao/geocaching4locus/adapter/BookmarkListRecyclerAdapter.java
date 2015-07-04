package com.arcao.geocaching4locus.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arcao.geocaching.api.data.bookmarks.BookmarkList;
import com.arcao.geocaching4locus.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BookmarkListRecyclerAdapter extends RecyclerView.Adapter<BookmarkListRecyclerAdapter.ViewHolder> {
  public interface OnItemClickListener {
    void onItemClick(BookmarkList bookmarkList);
  }

  private final List<BookmarkList> items = new ArrayList<>();
  private OnItemClickListener onItemClickListener;

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_bookmark_list_item, parent, false));
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    holder.bind(items.get(position));
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  public void setBookmarkLists(List<BookmarkList> bookmarkLists) {
    items.clear();
    items.addAll(bookmarkLists);
    notifyDataSetChanged();
  }

  protected class ViewHolder extends RecyclerView.ViewHolder {
    @Bind(R.id.title) TextView title;
    @Bind(R.id.description) TextView description;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    public void bind(final BookmarkList bookmarkList) {
      title.setText(bookmarkList.getName());
      description.setText(bookmarkList.getDescription());

      itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (onItemClickListener != null)
            onItemClickListener.onItemClick(bookmarkList);
        }
      });
    }
  }
}
