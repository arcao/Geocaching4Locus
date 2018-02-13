package com.arcao.geocaching4locus.import_bookmarks.task;

import android.content.Context;
import android.content.Intent;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.GeocachingApiFactory;
import com.arcao.geocaching.api.data.bookmarks.BookmarkList;
import com.arcao.geocaching4locus.authentication.task.GeocachingApiLoginTask;
import com.arcao.geocaching4locus.base.task.UserTask;
import com.arcao.geocaching4locus.error.handler.ExceptionHandler;

import java.lang.ref.WeakReference;
import java.util.List;

public class BookmarkListRetrieveTask extends UserTask<Void, Void, List<BookmarkList>> {
    public interface TaskListener {
        void onTaskFinish(List<BookmarkList> bookmarkLists);

        void onTaskError(Intent intent);
    }

    private final Context context;
    private final WeakReference<TaskListener> taskListenerRef;

    public BookmarkListRetrieveTask(Context context, TaskListener listener) {
        this.context = context.getApplicationContext();
        taskListenerRef = new WeakReference<>(listener);
    }


    @Override
    protected List<BookmarkList> doInBackground(Void... params) throws Exception {
        GeocachingApi api = GeocachingApiFactory.create();
        GeocachingApiLoginTask.create(context, api).perform();
        return api.getBookmarkListsForUser();
    }

    @Override
    protected void onPostExecute(List<BookmarkList> result) {
        super.onPostExecute(result);

        TaskListener listener = taskListenerRef.get();
        if (listener != null) listener.onTaskFinish(result);
    }

    @Override
    protected void onException(Throwable t) {
        super.onException(t);

        if (isCancelled())
            return;

        Intent intent = new ExceptionHandler(context).handle(t);

        TaskListener listener = taskListenerRef.get();
        if (listener != null) listener.onTaskError(intent);
    }
}
