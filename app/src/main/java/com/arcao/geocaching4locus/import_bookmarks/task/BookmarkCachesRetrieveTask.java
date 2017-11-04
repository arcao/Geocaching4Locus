package com.arcao.geocaching4locus.import_bookmarks.task;

import android.content.Context;
import android.content.Intent;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.GeocachingApiFactory;
import com.arcao.geocaching.api.data.bookmarks.Bookmark;
import com.arcao.geocaching4locus.authentication.task.GeocachingApiLoginTask;
import com.arcao.geocaching4locus.base.task.UserTask;
import com.arcao.geocaching4locus.error.handler.ExceptionHandler;

import java.lang.ref.WeakReference;
import java.util.List;

import timber.log.Timber;

public class BookmarkCachesRetrieveTask extends UserTask<String, Void, List<Bookmark>> {
	public interface TaskListener {
		void onTaskFinished(List<Bookmark> bookmarks);
		void onTaskFailed(Intent errorIntent);
	}

	private final Context mContext;
	private final WeakReference<TaskListener> mTaskListenerRef;

	public BookmarkCachesRetrieveTask(Context context, TaskListener listener) {
		mContext = context.getApplicationContext();
		mTaskListenerRef = new WeakReference<>(listener);
	}

	@Override
	protected List<Bookmark> doInBackground(String... params) throws Exception {
		GeocachingApi api = GeocachingApiFactory.create();
		GeocachingApiLoginTask.create(mContext, api).perform();
		return api.getBookmarkListByGuid(params[0]);
	}

	@Override
	protected void onPostExecute(List<Bookmark> result) {
		super.onPostExecute(result);

		TaskListener listener = mTaskListenerRef.get();
		if (listener != null) {
			listener.onTaskFinished(result);
		}
	}

	@Override
	protected void onException(Throwable t) {
		super.onException(t);

		if (isCancelled())
			return;

		Timber.e(t, t.getMessage());

		Intent intent = new ExceptionHandler(mContext).handle(t);

		TaskListener listener = mTaskListenerRef.get();
		if (listener != null) {
			listener.onTaskFailed(intent);
		}
	}
}
