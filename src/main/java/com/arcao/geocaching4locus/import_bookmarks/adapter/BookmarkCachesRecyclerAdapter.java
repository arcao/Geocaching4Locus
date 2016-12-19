package com.arcao.geocaching4locus.import_bookmarks.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.arcao.geocaching.api.data.bookmarks.Bookmark;
import com.arcao.geocaching4locus.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookmarkCachesRecyclerAdapter
        extends RecyclerView.Adapter<BookmarkCachesRecyclerAdapter.ViewHolder> {

  private final List<Bookmark> items = new ArrayList<>();
  boolean[] checked = new boolean[0];

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_bookmark_geocache_item, parent, false));
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    holder.bind(items.get(position));
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  public void setBookmarks(Collection<Bookmark> bookmarks) {
    items.clear();
    items.addAll(bookmarks);
    checked = new boolean[items.size()];
    notifyDataSetChanged();
  }

  public List<Bookmark> getCheckedBookmarks() {
    List <Bookmark> result = new ArrayList<>(checkedCount());

    int count = items.size();
    for (int i = 0; i < count; i++)
      if (checked[i])
        result.add(items.get(i));

    return result;
  }

  private int checkedCount() {
    int count = 0;
    for (boolean item : checked) {
      if (item) count++;
    }

    return count;
  }

  public void selectAll() {
    Arrays.fill(checked, true);
    notifyItemRangeChanged(0, items.size());
  }

  public void selectNone() {
    Arrays.fill(checked, false);
    notifyItemRangeChanged(0, items.size());
  }

  public boolean isAnyChecked() {
    for (boolean item : checked) {
      if (item)
        return true;
    }

    return false;
  }

  class ViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.title) TextView title;
    @BindView(R.id.subtitle) TextView subtitle;
    @BindView(R.id.checkbox) CheckBox checkbox;

    ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    public void bind(final Bookmark bookmark) {
      final int pos = getAdapterPosition();

      title.setText(bookmark.cacheTitle());
      subtitle.setText(bookmark.cacheCode());
      checkbox.setChecked(checked[pos]);

      itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          checked[pos] = !checked[pos];
          notifyItemChanged(pos);
        }
      });
    }
  }
}
