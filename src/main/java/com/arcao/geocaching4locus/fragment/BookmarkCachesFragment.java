package com.arcao.geocaching4locus.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.arcao.geocaching.api.data.bookmarks.Bookmark;
import com.arcao.geocaching.api.data.bookmarks.BookmarkList;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.adapter.BookmarkCachesRecyclerAdapter;
import com.arcao.geocaching4locus.task.BookmarkCachesRetrieveTask;
import com.arcao.geocaching4locus.widget.decorator.DividerItemDecoration;
import java.lang.ref.WeakReference;
import java.util.List;

public class BookmarkCachesFragment extends Fragment implements BookmarkCachesRetrieveTask.TaskListener {

	public interface ListListener {
		void onTitleChanged(String title);
		void onBookmarksSelected(Bookmark[] bookmarksList);
	}

	private static final String PARAM_TITLE = "TITLE";
	private static final String PARAM_GUID = "GUID";

	@Bind(R.id.list) RecyclerView recyclerView;
	@Bind(R.id.progressContainer) View progressContainer;
	@Bind(R.id.listContainer) View listContainer;
	@Bind(R.id.textEmpty) TextView textEmpty;
	@Bind(R.id.fab) FloatingActionButton fab;

	private WeakReference<ListListener> mListListenerRef;
	private BookmarkCachesRecyclerAdapter adapter = new BookmarkCachesRecyclerAdapter();
	private BookmarkCachesRetrieveTask mTask;
	private Animation mAnimation;

	public static BookmarkCachesFragment newInstance(BookmarkList bookmarkList) {
		Bundle args = new Bundle();
		args.putString(PARAM_TITLE, bookmarkList.getName());
		args.putString(PARAM_GUID, bookmarkList.getGuid());

		BookmarkCachesFragment fragment = new BookmarkCachesFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mListListenerRef = new WeakReference<>((ListListener) activity);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement ListListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setHasOptionsMenu(true);

		if (adapter.getItemCount() == 0) {
			mTask = new BookmarkCachesRetrieveTask(getActivity(), this);
			mTask.execute(getArguments().getString(PARAM_GUID));
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_bookmark_geocaches, container, false);
		ButterKnife.bind(this, v);

		mAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.simple_grow);

		prepareRecyclerView();

		ListListener listener = mListListenerRef.get();
		if (listener != null) {
			listener.onTitleChanged(getArguments().getString(PARAM_TITLE));
		}

		return  v;
	}

	private void prepareRecyclerView() {
		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.addItemDecoration(
				new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

		setEmptyText("");
		setListShown(adapter.getItemCount() > 0);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.toolbar_select_deselect, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.selectAll:
				adapter.selectAll();
				return true;
			case R.id.deselectAll:
				adapter.selectNone();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.unbind(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mTask != null)
			mTask.cancel(true);
	}

	@Override
	public void onTaskFinished(List<Bookmark> bookmarks) {
		adapter.setBookmarks(bookmarks);
		setListShown(true);
	}

	@Override
	public void onTaskFailed(Intent errorIntent) {
		startActivity(errorIntent);
		getActivity().finish();
	}

	@OnClick(R.id.fab)
	public void onFabClicked() {
		ListListener listener = mListListenerRef.get();
		if (listener != null && adapter.isAnyChecked()) {
			List<Bookmark> checkedBookmarks = adapter.getCheckedBookmarks();
			listener.onBookmarksSelected(checkedBookmarks.toArray(new Bookmark[checkedBookmarks.size()]));
		}
	}

	private void setListShown(boolean visible) {
		if (visible) {
			progressContainer.setVisibility(View.GONE);
			listContainer.setVisibility(View.VISIBLE);
			fab.startAnimation(mAnimation);
		} else {
			progressContainer.setVisibility(View.VISIBLE);
			listContainer.setVisibility(View.GONE);
		}

		textEmpty.setVisibility(adapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
		fab.setVisibility(adapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
	}

	private void setEmptyText(CharSequence text) {
		textEmpty.setText(text);
	}
}
