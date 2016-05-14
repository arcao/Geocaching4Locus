package com.arcao.geocaching4locus.import_bookmarks.task;

import android.content.Context;
import android.content.Intent;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.GeocachingApiFactory;
import com.arcao.geocaching.api.data.bookmarks.BookmarkList;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.authentication.util.AccountManager;
import com.arcao.geocaching4locus.error.handler.ExceptionHandler;
import com.arcao.geocaching4locus.base.task.UserTask;

import java.lang.ref.WeakReference;
import java.util.List;

import timber.log.Timber;

public class BookmarkListRetrieveTask extends UserTask<Void, Void, List<BookmarkList>> {
	public interface TaskListener {
		void onTaskFinished(List<BookmarkList> bookmarkLists);
		void onTaskFailed(Intent errorIntent);
	}

	private final Context mContext;
	private final WeakReference<TaskListener> mTaskListenerRef;

	public BookmarkListRetrieveTask(Context context, TaskListener listener) {
		mContext = context.getApplicationContext();
		mTaskListenerRef = new WeakReference<>(listener);
	}


	@Override
	protected List<BookmarkList> doInBackground(Void... params) throws Exception {
		AccountManager accountManager = App.get(mContext).getAccountManager();

		if (!accountManager.hasAccount())
			throw new InvalidCredentialsException("Account not found.");

		GeocachingApi api = GeocachingApiFactory.create();

		login(api);
		return api.getBookmarkListsForUser();
	}

	@Override
	protected void onPostExecute(List<BookmarkList> result) {
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

	private void login(GeocachingApi api) throws GeocachingApiException {
		AccountManager accountManager = App.get(mContext).getAccountManager();

		String token = accountManager.getOAuthToken();
		if (token == null) {
			accountManager.removeAccount();
			throw new InvalidCredentialsException("Account not found.");
		}

		api.openSession(token);
	}
}
