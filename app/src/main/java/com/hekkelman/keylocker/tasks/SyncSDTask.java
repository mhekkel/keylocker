package com.hekkelman.keylocker.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import com.hekkelman.keylocker.datamodel.KeyDbException;
import com.hekkelman.keylocker.utilities.AppContainer;

import java.util.concurrent.Executor;

public class SyncSDTask {
    private final Executor executor;
    private final Handler handler;

    public SyncSDTask(Executor executor, Handler handler) {
        this.executor = executor;
        this.handler = handler;
    }

    public void syncToSD(final Context context,
                         final AppContainer appContainer,
                         final Uri backupDir,
                         final String password,
                         final TaskCallback<Void> callback) {
        executor.execute(() -> {
            try {
                appContainer.keyDb.synchronize(context, backupDir, password);
                notifyResult(new TaskResult.Success<>(null), callback);
            } catch (KeyDbException exception) {
                notifyResult(new TaskResult.Error<>(exception), callback);
            }
        });
    }

    private void notifyResult(
            final TaskResult<Void> result,
            final TaskCallback<Void> callback) {
        handler.post(() -> callback.onComplete(result));
    }
}
