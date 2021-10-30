package com.hekkelman.keylocker.tasks;

import android.os.Handler;

import com.hekkelman.keylocker.datamodel.Key;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.KeyDbDao;
import com.hekkelman.keylocker.datamodel.KeyDbException;

import java.util.concurrent.Executor;

public class SaveKeyTask {

    private final KeyDbDao keyDb;
    private final Executor executor;
    private final Handler handler;

    public SaveKeyTask(KeyDbDao keyDb, Executor executor, Handler handler) {
        this.keyDb = keyDb;
        this.executor = executor;
        this.handler = handler;
    }

    public void saveKey(final Key key, final String name, final String user, final String password, final String url, final boolean finishOnSaved,
                        final TaskCallback<Boolean> callback) {
        executor.execute(() -> {
            try {
                keyDb.updateKey(key, name, user, password, url);
                notifyResult(new TaskResult.Success<>(finishOnSaved), callback);
            } catch (KeyDbException exception) {
                notifyResult(new TaskResult.Error<>(exception), callback);
            }
        });
    }

    private void notifyResult(
            final TaskResult<Boolean> result,
            final TaskCallback<Boolean> callback) {
        handler.post(() -> callback.onComplete(result));
    }

}
