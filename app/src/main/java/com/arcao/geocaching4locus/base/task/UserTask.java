/*
 * Copyright (C) 2008 The Android Open Source Project, Romain Guy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modification:
 *  Martin Arcao Sloup (2011-08-11):
 *   - support for onException handler (runs on UI thread)
 *   - support for onFinish handler (runs on UI thread)
 *   - some methods are protected instead of public
 *
 */

package com.arcao.geocaching4locus.base.task;

import android.os.Handler;
import android.os.Message;
import android.os.Process;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import timber.log.Timber;

/**
 * <p>
 * UserTask enables proper and easy use of the UI thread. This class allows to
 * perform background operations and publish results on the UI thread without
 * having to manipulate threads and/or handlers.
 * </p>
 * <p>
 * <p>
 * A user task is defined by a computation that runs on a background thread and
 * whose result is published on the UI thread. A user task is defined by 3
 * generic types, called <code>Params</code>, <code>Progress</code> and
 * <code>Result</code>, and 4 steps, called <code>begin</code>,
 * <code>doInBackground</code>, <code>processProgress<code> and <code>end</code>
 * .
 * </p>
 * <p>
 * <h2>Usage</h2>
 * <p>
 * UserTask must be subclassed to be used. The subclass will override at least
 * one method ({@link #doInBackground(Object[])}), and most often will override
 * a second one ({@link #onPostExecute(Object)}.)
 * </p>
 * <p>
 * <p>
 * Here is an example of subclassing:
 * </p>
 * <p>
 * <pre>
 * private class DownloadFilesTask extends UserTask&lt;URL, Integer, Long&gt; {
 * 	protected File doInBackground(URL... urls) {
 * 		int count = urls.length;
 * 		long totalSize = 0;
 * 		for (int i = 0; i &lt; count; i++) {
 * 			totalSize += Downloader.downloadFile(urls[i]);
 * 			publishProgress((int) ((i / (float) count) * 100));
 *        }
 *    }
 *
 * 	protected void onProgressUpdate(Integer... progress) {
 * 		setProgressPercent(progress[0]);
 *    }
 *
 * 	protected void onPostExecute(Long result) {
 * 		showDialog(&quot;Downloaded &quot; + result + &quot; bytes&quot;);
 *    }
 * }
 * </pre>
 * <p>
 * <p>
 * Once created, a task is executed very simply:
 * </p>
 * <p>
 * <pre>
 * new DownloadFilesTask().execute(new URL[] { ... });
 * </pre>
 * <p>
 * <h2>User task's generic types</h2>
 * <p>
 * The three types used by a user task are the following:
 * </p>
 * <ol>
 * <li><code>Params</code>, the type of the parameters sent to the task upon
 * execution.</li>
 * <li><code>Progress</code>, the type of the progress units published during
 * the background computation.</li>
 * <li><code>Result</code>, the type of the result of the background
 * computation.</li>
 * </ol>
 * <p>
 * Not all types are always used by a user task. To mark a type as unused,
 * simply use the type {@link Void}:
 * </p>
 * <p>
 * <pre>
 * private class MyTask extends UserTask<Void, Void, Void) { ... }
 * </pre>
 * <p>
 * <h2>The 4 steps</h2>
 * <p>
 * When a user task is executed, the task goes through 4 steps:
 * </p>
 * <ol>
 * <li>{@link #onPreExecute()}, invoked on the UI thread immediately after the
 * task is executed. This step is normally used to setup the task, for instance
 * by showing a progress bar in the user interface.</li>
 * <li>{@link #doInBackground(Object[])}, invoked on the background thread
 * immediately after {@link # onPreExecute ()} finishes executing. This step is
 * used to perform background computation that can take a long time. The
 * parameters of the user task are passed to this step. The result of the
 * computation must be returned by this step and will be passed back to the last
 * step. This step can also use {@link #publishProgress(Object[])} to publish
 * one or more units of progress. These values are published on the UI thread,
 * in the {@link #onProgressUpdate(Object[])} step.</li>
 * <li>{@link # onProgressUpdate (Object[])}, invoked on the UI thread after a
 * call to {@link #publishProgress(Object[])}. The timing of the execution is
 * undefined. This method is used to display any form of progress in the user
 * interface while the background computation is still executing. For instance,
 * it can be used to animate a progress bar or show logs in a text field.</li>
 * <li>{@link # onPostExecute (Object)}, invoked on the UI thread after the
 * background computation finishes. The result of the background computation is
 * passed to this step as a parameter.</li>
 * </ol>
 * <p>
 * <h2>Threading rules</h2>
 * <p>
 * There are a few threading rules that must be followed for this class to work
 * properly:
 * </p>
 * <ul>
 * <li>The task instance must be created on the UI thread.</li>
 * <li>{@link #execute(Object[])} must be invoked on the UI thread.</li>
 * <li>Do not call {@link # onPreExecute ()}, {@link # onPostExecute (Object)},
 * {@link #doInBackground(Object[])}, {@link # onProgressUpdate (Object[])}
 * manually.</li>
 * <li>The task can be executed only once (an exception will be thrown if a
 * second execution is attempted.)</li>
 * </ul>
 *
 * @deprecated Use coroutine
 */
@Deprecated
public abstract class UserTask<Params, Progress, Result> {
    private static final int CORE_POOL_SIZE = 1;
    private static final int MAXIMUM_POOL_SIZE = 10;
    private static final int KEEP_ALIVE = 10;

    private static final BlockingQueue<Runnable> WORK_QUEUE =
            new LinkedBlockingQueue<>();

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger count = new AtomicInteger(1);

        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "UserTask #" + count.getAndIncrement());
        }
    };

    private static final Executor EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE,
            MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, WORK_QUEUE, THREAD_FACTORY);

    private static final int MESSAGE_POST_RESULT = 0x1;
    private static final int MESSAGE_POST_PROGRESS = 0x2;
    private static final int MESSAGE_POST_CANCEL = 0x3;
    private static final int MESSAGE_POST_EXCEPTION = 0x4;

    @SuppressWarnings({"WeakerAccess"})
    static final Handler HANDLER = new InternalHandler();

    private final WorkerRunnable<Params, Result> worker;
    private final FutureTask<Result> future;

    private volatile Status status = Status.PENDING;

    /**
     * Indicates the current status of the task. Each status will be set only once
     * during the lifetime of a task.
     */
    public enum Status {
        /**
         * Indicates that the task has not been executed yet.
         */
        PENDING,
        /**
         * Indicates that the task is running.
         */
        RUNNING,
        /**
         * Indicates that {@link UserTask#onPostExecute(Object)} has finished.
         */
        FINISHED,
    }

    /**
     * Creates a new user task. This constructor must be invoked on the UI thread.
     */
    public UserTask() {
        worker = new WorkerRunnable<Params, Result>() {
            @Override
            public Result call() throws Exception {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                return doInBackground(params);
            }
        };

        future = new FutureTask<Result>(worker) {
            @Override
            protected void done() {
                Message message;
                Result result = null;

                try {
                    result = get();
                } catch (InterruptedException e) {
                    Timber.w(e);
                } catch (ExecutionException e) {
                    Timber.e(e.getCause(), "An error occurred while executing doInBackground()");
                    message = HANDLER.obtainMessage(MESSAGE_POST_EXCEPTION,
                            new UserTaskResult<>(UserTask.this, e.getCause(), (Result[]) null));
                    message.sendToTarget();
                    return;
                } catch (CancellationException e) {
                    message = HANDLER.obtainMessage(MESSAGE_POST_CANCEL,
                            new UserTaskResult<>(UserTask.this, null, (Result[]) null));
                    message.sendToTarget();
                    return;
                } catch (Throwable t) {
                    throw new RuntimeException("An error occurred while executing "
                            + "doInBackground()", t);
                }

                message = HANDLER.obtainMessage(MESSAGE_POST_RESULT,
                        new UserTaskResult<>(UserTask.this, null, result));
                message.sendToTarget();
            }
        };
    }

    /**
     * Returns the current status of this task.
     *
     * @return The current status.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public final Status getStatus() {
        return status;
    }

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to
     * {@link #execute(Object[])} by the caller of this task.
     * <p>
     * This method can call {@link #publishProgress(Object[])} to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @throws Exception If error occurs in this method exceptions can be handled in
     *                   {@link #onException(Throwable)}.
     * @see #onPreExecute()
     * @see #onPostExecute(Object)
     * @see #onFinally()
     * @see #publishProgress(Object[])
     * @see #onException(Throwable)
     */
    @SuppressWarnings("unchecked")
    protected abstract Result doInBackground(Params... params) throws Exception;

    /**
     * Runs on the UI thread before {@link #doInBackground(Object[])}.
     *
     * @see #onPostExecute(Object)
     * @see #doInBackground(Object[])
     */
    protected void onPreExecute() {
        // override me
    }

    /**
     * Runs on the UI thread after {@link #doInBackground(Object[])}. The
     * specified result is the value returned by {@link #doInBackground(Object[])}
     * or null if the task was cancelled or an exception occurred.
     *
     * @param result The result of the operation computed by
     *               {@link #doInBackground(Object[])}.
     * @see #onPreExecute()
     * @see #doInBackground(Object[])
     */
    protected void onPostExecute(Result result) {
        // override me
    }

    /**
     * Runs on the UI thread after {@link #publishProgress(Object[])} is invoked.
     * The specified values are the values passed to
     * {@link #publishProgress(Object[])}.
     *
     * @param values The values indicating progress.
     * @see #publishProgress(Object[])
     * @see #doInBackground(Object[])
     */
    @SuppressWarnings("unchecked")
    protected void onProgressUpdate(Progress... values) {
        // override me
    }

    /**
     * Runs on the UI thread after {@link #cancel(boolean)} is invoked.
     *
     * @see #cancel(boolean)
     * @see #isCancelled()
     */
    protected void onCancelled() {
        // override me
    }

    /**
     * Returns <tt>true</tt> if this task was cancelled before it completed
     * normally.
     *
     * @return <tt>true</tt> if task was cancelled before it completed
     * @see #cancel(boolean)
     */
    public final boolean isCancelled() {
        return future.isCancelled();
    }

    /**
     * Attempts to cancel execution of this task. This attempt will fail if the
     * task has already completed, already been cancelled, or could not be
     * cancelled for some other reason. If successful, and this task has not
     * started when <tt>cancel</tt> is called, this task should never run. If the
     * task has already started, then the <tt>mayInterruptIfRunning</tt> parameter
     * determines whether the thread executing this task should be interrupted in
     * an attempt to stop the task.
     *
     * @param mayInterruptIfRunning <tt>true</tt> if the thread executing this task should be
     *                              interrupted; otherwise, in-progress tasks are allowed to complete.
     * @return <tt>false</tt> if the task could not be cancelled, typically
     * because it has already completed normally; <tt>true</tt> otherwise
     * @see #isCancelled()
     * @see #onCancelled()
     */
    public final boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    /**
     * Runs on the UI thread after {@link #onPostExecute(Object)} or
     * {@link #onCancelled()};
     *
     * @see #onPostExecute(Object)
     * @see #onCancelled()
     */
    @SuppressWarnings("WeakerAccess")
    protected void onFinally() {
        // override me
    }

    /**
     * Runs on the UI thread when errors occurs in
     * {@link #doInBackground(Object...)} method.
     *
     * @param e Exception
     * @see #doInBackground(Object...)
     */
    protected void onException(Throwable e) {
        // override me
    }

    /**
     * Waits if necessary for the computation to complete, and then retrieves its
     * result.
     *
     * @return The computed result.
     * @throws CancellationException If the computation was cancelled.
     * @throws ExecutionException    If the computation threw an exception.
     * @throws InterruptedException  If the current thread was interrupted while waiting.
     */
    public final Result get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    /**
     * Waits if necessary for at most the given time for the computation to
     * complete, and then retrieves its result.
     *
     * @param timeout Time to wait before cancelling the operation.
     * @param unit    The time unit for the timeout.
     * @return The computed result.
     * @throws CancellationException If the computation was cancelled.
     * @throws ExecutionException    If the computation threw an exception.
     * @throws InterruptedException  If the current thread was interrupted while waiting.
     * @throws TimeoutException      If the wait timed out.
     */
    public final Result get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }

    /**
     * Executes the task with the specified parameters. The task returns itself
     * (this) so that the caller can keep a reference to it.
     * <p>
     * This method must be invoked on the UI thread.
     *
     * @param params The parameters of the task.
     * @return This instance of UserTask.
     * @throws IllegalStateException If {@link #getStatus()} returns either
     *                               {@link UserTask.Status#RUNNING} or
     *                               {@link UserTask.Status#FINISHED}.
     */
    @SafeVarargs
    public final UserTask<Params, Progress, Result> execute(Params... params) {
        if (status != Status.PENDING) {
            switch (status) {
                case RUNNING:
                    throw new IllegalStateException("Cannot execute task:"
                            + " the task is already running.");
                case FINISHED:
                    throw new IllegalStateException("Cannot execute task:"
                            + " the task has already been executed "
                            + "(a task can be executed only once)");
                default:
            }
        }

        status = Status.RUNNING;

        onPreExecute();

        worker.params = params;
        EXECUTOR.execute(future);

        return this;
    }

    /**
     * This method can be invoked from {@link #doInBackground(Object[])} to
     * publish updates on the UI thread while the background computation is still
     * running. Each call to this method will trigger the execution of
     * {@link #onProgressUpdate(Object[])} on the UI thread.
     *
     * @param values The progress values to update the UI with.
     * @see #onProgressUpdate(Object[])
     * @see #doInBackground(Object[])
     */
    @SafeVarargs
    protected final void publishProgress(Progress... values) {
        HANDLER.obtainMessage(MESSAGE_POST_PROGRESS,
                new UserTaskResult<>(this, null, values)).sendToTarget();
    }

    void finish(Result result) {
        onPostExecute(result);
        status = Status.FINISHED;
    }

    void exception(Throwable e) {
        onException(e);
        status = Status.FINISHED;
    }

    void cancel() {
        onCancelled();
        status = Status.FINISHED;
    }

    private static class InternalHandler<Params, Progress, Result> extends Handler {
        InternalHandler() {
        }

        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            UserTaskResult<UserTask<Params, Progress, Result>, Object> result = (UserTaskResult<UserTask<Params, Progress, Result>, Object>) msg.obj;
            switch (msg.what) {
                case MESSAGE_POST_RESULT:
                    // There is only one result
                    result.task.finish((Result) result.data[0]);
                    result.task.onFinally();
                    break;
                case MESSAGE_POST_PROGRESS:
                    result.task.onProgressUpdate((Progress[]) result.data);
                    break;
                case MESSAGE_POST_CANCEL:
                    result.task.cancel();
                    result.task.onFinally();
                    break;
                case MESSAGE_POST_EXCEPTION:
                    result.task.exception(result.exception);
                    result.task.onFinally();
                    break;
            }
        }
    }

    private static abstract class WorkerRunnable<Params, Result> implements Callable<Result> {
        Params[] params;

        WorkerRunnable() {
        }
    }

    private static class UserTaskResult<Task, Data> {
        final Task task;
        final Data[] data;
        final Throwable exception;

        @SafeVarargs
        UserTaskResult(Task task, Throwable exception, Data... data) {
            this.task = task;
            this.data = data;
            this.exception = exception;
        }
    }
}
