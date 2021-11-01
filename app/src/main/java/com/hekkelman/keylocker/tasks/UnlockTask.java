package com.hekkelman.keylocker.tasks;

import android.os.Handler;

import com.hekkelman.keylocker.datamodel.KeyDb;

import java.io.File;
import java.util.concurrent.Executor;

public class UnlockTask {
    private final Executor executor;
    private final Handler handler;

    public UnlockTask(Executor executor, Handler handler) {
        this.executor = executor;
        this.handler = handler;
    }

    public void unlock(final File keyDbFile, final char[] plainPassword,
                       final TaskCallback<Void> callback) {
        executor.execute(() -> {
            if (KeyDb.isValidPassword(plainPassword, keyDbFile))
                notifyResult(new TaskResult.Success<>(null), callback);
            else
                notifyResult(new TaskResult.Error<>(null), callback);
        });
    }

    private void notifyResult(
            final TaskResult<Void> result,
            final TaskCallback<Void> callback) {
        handler.post(() -> callback.onComplete(result));
    }
}
