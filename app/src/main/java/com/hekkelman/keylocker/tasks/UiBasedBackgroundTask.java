package com.hekkelman.keylocker.tasks;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Encapsulates a background task that needs to communicate back to the UI (on the main thread) to
 * provide a result.
 */
public abstract class UiBasedBackgroundTask<Result> {

    private final Result mFailedResult;
    private final ExecutorService mExecutor;
    private final Handler mMainThreadHandler;

    private final Object mCallbackLock = new Object();
    @Nullable
    private UiCallback<Result> mCallback;
    @Nullable
    private Result mAwaitedResult;

    private volatile boolean mIsCanceled = false;

    /**
     * @param failedResult The result to return if the task fails (throws an exception or returns null).
     */
    public UiBasedBackgroundTask(@NonNull Result failedResult) {
        this.mFailedResult = failedResult;
        this.mExecutor = Executors.newSingleThreadExecutor();
        this.mMainThreadHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * @param callback If null, any results which may arrive from a currently executing task will
     *                 be stored until a new callback is set.
     */
    public void setCallback(@Nullable UiCallback<Result> callback) {
        synchronized (mCallbackLock) {
            // Don't bother doing anything if the task was canceled.
            if (isCanceled())
                return;

            this.mCallback = callback;

            // If we have an awaited result and are setting a new callback, publish the result immediately.
            if (mAwaitedResult != null && callback != null)
                emitResultOnMainThread(callback, mAwaitedResult);
        }
    }

    private void emitResultOnMainThread(@NonNull UiCallback<Result> callback, @NonNull Result result) {
        mMainThreadHandler.post(() -> callback.onResult(result));
        this.mCallback = null;
        this.mAwaitedResult = null;
    }

    /**
     * Executed the task on a background thread. Safe to call from the main thread.
     */
    @AnyThread
    public void execute() {
        mExecutor.execute(this::runTask);
    }

    private void runTask() {
        Result result = mFailedResult;
        try {
            result = doInBackground();
        } catch (Exception e) {
            Log.e("UiBasedBackgroundTask", "Problem running background task", e);
        }

        synchronized (mCallbackLock) {
            // Don't bother issuing callback or storing result if this task is canceled.
            if (isCanceled())
                return;

            if (mCallback != null) {
                emitResultOnMainThread(mCallback, result);
            } else {
                mAwaitedResult = result;
            }
        }
    }

    /**
     * Work to be done in a background thread.
     *
     * @return Return the result from this task's execution.
     * @throws Exception If an Exception is thrown from this task's execution, it will be logged
     *                   and the provided default Result will be returned.
     */
    @NonNull
    protected abstract Result doInBackground() throws Exception;

    @AnyThread
    public boolean isCanceled() {
        return mIsCanceled;
    }

    @AnyThread
    public void cancel() {
        mIsCanceled = true;
    }

    @FunctionalInterface
    public interface UiCallback<Result> {
        @MainThread
        void onResult(@NonNull Result result);
    }
}
