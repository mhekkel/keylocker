package com.hekkelman.keylocker.tasks;

import android.content.Context;
import android.os.Handler;

import com.hekkelman.keylocker.datamodel.KeyLockerFile;
import com.hekkelman.keylocker.utilities.AppContainer;

import java.util.concurrent.Executor;

public class UnlockTask {
    private final Executor executor;
    private final Handler handler;

    public UnlockTask(Executor executor, Handler handler) {
        this.executor = executor;
        this.handler = handler;
    }

    public void unlock(final Context context,
                       final AppContainer appContainer, final String plainPassword,
                       final TaskCallback<Void> callback) {
        executor.execute(() -> {
            if (appContainer.keyDb == null) {
                try {
                    appContainer.keyDb = KeyLockerFile.create(context, plainPassword);
                    notifyResult(new TaskResult.Success<>(null), callback);
                } catch (Exception e) {
                    notifyResult(new TaskResult.Error<>(e), callback);
                }
            } else {
                if (appContainer.keyDb.checkPassword(plainPassword))
                    notifyResult(new TaskResult.Success<>(null), callback);
                else
                    notifyResult(new TaskResult.Error<>(null), callback);
            }
        });
    }

    private void notifyResult(
            final TaskResult<Void> result,
            final TaskCallback<Void> callback) {
        handler.post(() -> callback.onComplete(result));
    }
}
