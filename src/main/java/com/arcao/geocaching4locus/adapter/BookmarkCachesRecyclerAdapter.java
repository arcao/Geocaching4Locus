package com.arcao.geocaching4locus.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.arcao.geocaching.api.data.bookmarks.Bookmark;
import com.arcao.geocaching4locus.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookmarkCachesRecyclerAdapter
    extends RecyclerView.Adapter<BookmarkCachesRecyclerAdapter.ViewHolder> {

  private final List<Bookmark> items = new ArrayList<>();
  private boolean[] checked = new boolean[0];

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

  public void setBookmarks(List<Bookmark> bookmarks) {
    items.clear();
    items.addAll(bookmarks);
    checked = new boolean[items.size()];
    notifyDataSetChanged();
  }

  public List<Bookmark> getCheckedBookmarks() {
    List <Bookmark> result = new ArrayList<>();

    int count = items.size();
    for (int i = 0; i < count; i++)
      if (checked[i])
        result.add(items.get(i));

    return result;
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

  protected class ViewHolder extends RecyclerView.ViewHolder {
    @Bind(R.id.title) TextView title;
    @Bind(R.id.subtitle) TextView subtitle;
		@Bind(R.id.checkbox) CheckBox checkbox;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    public void bind(final Bookmark bookmark) {
      title.setText(bookmark.getCacheTitle());
      subtitle.setText(bookmark.getCacheCode());
      checkbox.setChecked(checked[getAdapterPosition()]);

      itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          int pos = getAdapterPosition();
          checked[pos] = !checked[pos];
          notifyItemChanged(pos);
        }
      });
    }
  }
}
