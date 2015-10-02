package com.arcao.geocaching4locus.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.arcao.geocaching.api.data.bookmarks.BookmarkList;
import com.arcao.geocaching4locus.R;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BookmarkListRecyclerAdapter extends RecyclerView.Adapter<BookmarkListRecyclerAdapter.ViewHolder> {
  public interface OnItemClickListener {
    void onItemClick(BookmarkList bookmarkList, boolean importAll);
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

  public void setBookmarkLists(Collection<BookmarkList> bookmarkLists) {
    items.clear();
    items.addAll(bookmarkLists);
    notifyDataSetChanged();
  }

  protected class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @Bind(R.id.title) TextView title;
    @Bind(R.id.description) TextView description;
		@Bind(R.id.count) TextView count;
    @Bind(R.id.button) Button button;

    private BookmarkList bookmarkList;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    public void bind(final BookmarkList bookmarkList) {
      this.bookmarkList = bookmarkList;

      title.setText(bookmarkList.getName());
			count.setText(itemView.getResources().getQuantityString(R.plurals.plurals_cache,
          bookmarkList.getItemCount(), bookmarkList.getItemCount()));
      description.setText(bookmarkList.getDescription());
			description.setVisibility(
          StringUtils.isEmpty(bookmarkList.getDescription()) ? View.GONE : View.VISIBLE);

      button.setOnClickListener(this);
      itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
      if (onItemClickListener != null)
        onItemClickListener.onItemClick(bookmarkList, v instanceof Button);
    }
  }
}
