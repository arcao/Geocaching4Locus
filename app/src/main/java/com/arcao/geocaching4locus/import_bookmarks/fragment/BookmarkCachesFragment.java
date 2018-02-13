package com.arcao.geocaching4locus.import_bookmarks.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DividerItemDecoration;
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

import com.arcao.geocaching.api.data.bookmarks.Bookmark;
import com.arcao.geocaching.api.data.bookmarks.BookmarkList;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.import_bookmarks.adapter.BookmarkCachesRecyclerAdapter;
import com.arcao.geocaching4locus.import_bookmarks.task.BookmarkCachesRetrieveTask;
import com.arcao.geocaching4locus.import_bookmarks.widget.decorator.BottomMarginLastItemDecorator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class BookmarkCachesFragment extends Fragment implements BookmarkCachesRetrieveTask.TaskListener {
    public interface ListListener {
        void onTitleChanged(String title);

        void onBookmarksSelected(Bookmark[] bookmarksList);
    }

    private static final String STATE_BOOKMARKS = "BOOKMARKS";
    private static final String PARAM_TITLE = "TITLE";
    private static final String PARAM_GUID = "GUID";

    @BindView(R.id.list)
    RecyclerView recyclerView;
    @BindView(R.id.progressContainer)
    View progressContainer;
    @BindView(R.id.listContainer)
    View listContainer;
    @BindView(R.id.textEmpty)
    TextView textEmpty;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    private WeakReference<ListListener> listListenerRef;
    private final BookmarkCachesRecyclerAdapter adapter = new BookmarkCachesRecyclerAdapter();
    private BookmarkCachesRetrieveTask task;
    private Animation animation;
    private ArrayList<Bookmark> bookmarkList;
    private Unbinder unbinder;

    public static BookmarkCachesFragment newInstance(BookmarkList bookmarkList) {
        Bundle args = new Bundle();
        args.putString(PARAM_TITLE, bookmarkList.name());
        args.putString(PARAM_GUID, bookmarkList.guid());

        BookmarkCachesFragment fragment = new BookmarkCachesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            listListenerRef = new WeakReference<>((ListListener) activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ListListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bookmark_geocaches, container, false);
        unbinder = ButterKnife.bind(this, v);

        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.simple_grow);

        prepareRecyclerView();

        ListListener listener = listListenerRef.get();
        if (listener != null) {
            listener.onTitleChanged(getArguments().getString(PARAM_TITLE));
        }

        return v;
    }

    private void prepareRecyclerView() {
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.addItemDecoration(new BottomMarginLastItemDecorator(getActivity(), R.dimen.fab_outer_height));

        setEmptyText("");
        setListShown(adapter.getItemCount() > 0);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_BOOKMARKS)) {
            //noinspection unchecked
            bookmarkList = (ArrayList<Bookmark>) savedInstanceState.getSerializable(STATE_BOOKMARKS);
        }

        if (bookmarkList == null) {
            task = new BookmarkCachesRetrieveTask(getActivity(), this);
            task.execute(getArguments().getString(PARAM_GUID));
        } else {
            onTaskFinished(bookmarkList);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (bookmarkList != null) {
            outState.putSerializable(STATE_BOOKMARKS, bookmarkList);
        }
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
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (task != null) task.cancel(true);
    }

    @Override
    public void onTaskFinished(List<Bookmark> bookmarkList) {
        this.bookmarkList = new ArrayList<>(bookmarkList);
        adapter.setBookmarks(this.bookmarkList);
        setListShown(true);
    }

    @Override
    public void onTaskError(Intent intent) {
        startActivity(intent);
        getActivity().finish();
    }

    @OnClick(R.id.fab)
    public void onFabClicked() {
        ListListener listener = listListenerRef.get();

        if (listener != null && adapter.isAnyChecked()) {
            List<Bookmark> checkedBookmarks = adapter.getCheckedBookmarks();
            listener.onBookmarksSelected(checkedBookmarks.toArray(new Bookmark[checkedBookmarks.size()]));
        }
    }

    private void setListShown(boolean visible) {
        if (visible) {
            progressContainer.setVisibility(View.GONE);
            listContainer.setVisibility(View.VISIBLE);
            fab.startAnimation(animation);
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
