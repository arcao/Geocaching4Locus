package com.arcao.geocaching4locus.import_bookmarks.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arcao.geocaching.api.data.bookmarks.BookmarkList;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.import_bookmarks.adapter.BookmarkListRecyclerAdapter;
import com.arcao.geocaching4locus.import_bookmarks.task.BookmarkListRetrieveTask;
import com.arcao.geocaching4locus.import_bookmarks.widget.decorator.MarginItemDecoration;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class BookmarkListFragment extends Fragment implements BookmarkListRetrieveTask.TaskListener {
    private static final String STATE_BOOKMARK_LISTS = "BOOKMARK_LISTS";

    public interface ListListener {
        void onTitleChanged(String title);

        void onBookmarkListSelected(BookmarkList bookmarkList, boolean selectAll);
    }

    @BindView(R.id.list) RecyclerView recyclerView;
    @BindView(R.id.progressContainer) View progressContainer;
    @BindView(R.id.listContainer) View listContainer;
    @BindView(R.id.textEmpty) TextView textEmpty;

    private BookmarkListRecyclerAdapter adapter;
    private WeakReference<ListListener> listListenerRef;
    @Nullable private BookmarkListRetrieveTask task;
    private ArrayList<BookmarkList> bookmarkLists;
    private Unbinder unbinder;

    public static BookmarkListFragment newInstance() {
        Bundle args = new Bundle();

        BookmarkListFragment fragment = new BookmarkListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = requireActivity();

        try {
            listListenerRef = new WeakReference<>((ListListener) activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ListListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bookmark_list, container, false);
        unbinder = ButterKnife.bind(this, v);

        prepareRecyclerView();

        ListListener listener = listListenerRef.get();
        if (listener != null) {
            listener.onTitleChanged("");
        }

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_BOOKMARK_LISTS)) {
            //noinspection unchecked
            bookmarkLists = (ArrayList<BookmarkList>) savedInstanceState.getSerializable(STATE_BOOKMARK_LISTS);
        }

        if (bookmarkLists == null) {
            task = new BookmarkListRetrieveTask(getActivity(), this);
            task.execute();
        } else {
            onTaskFinish(bookmarkLists);
        }
    }

    private void prepareRecyclerView() {
        adapter = new BookmarkListRecyclerAdapter((bookmarkList, selectAll) -> {
            ListListener listener = listListenerRef.get();
            if (listener != null)
                listener.onBookmarkListSelected(bookmarkList, selectAll);
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new MarginItemDecoration(getActivity(), R.dimen.cardview_space));

        setEmptyText("");
        setListShown(adapter.getItemCount() > 0);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(STATE_BOOKMARK_LISTS, bookmarkLists);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (task != null) {
            task.cancel(true);
            task = null;
        }
    }

    @Override
    public void onTaskFinish(List<BookmarkList> bookmarkLists) {
        this.bookmarkLists = new ArrayList<>(bookmarkLists);
        adapter.submitList(this.bookmarkLists);
        setListShown(true);
    }

    @Override
    public void onTaskError(Intent intent) {
        getActivity().startActivity(intent);
        getActivity().finish();
    }


    private void setListShown(boolean visible) {
        if (visible) {
            progressContainer.setVisibility(View.GONE);
            listContainer.setVisibility(View.VISIBLE);
        } else {
            progressContainer.setVisibility(View.VISIBLE);
            listContainer.setVisibility(View.GONE);
        }
        textEmpty.setVisibility(adapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
    }

    private void setEmptyText(CharSequence text) {
        textEmpty.setText(text);
    }

}
