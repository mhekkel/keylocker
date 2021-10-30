package com.hekkelman.keylocker.tasks;

import android.os.Handler;

import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.KeyDbModel;

import java.util.concurrent.Executor;

public class UnlockTask {
    private final Executor executor;
    private final Handler handler;

    public UnlockTask(Executor executor, Handler handler) {
        this.executor = executor;
        this.handler = handler;
    }

    public void unlock(final KeyDbModel keyDb, final char[] plainPassword,
                       final TaskCallback<Void> callback) {
        executor.execute(() -> {
            if (keyDb.unlock(plainPassword))
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
