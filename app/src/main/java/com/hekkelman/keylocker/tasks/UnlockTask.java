package com.hekkelman.keylocker.tasks;

import android.os.Handler;

import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.KeyDbFactory;
import com.hekkelman.keylocker.utilities.AppContainer;

import java.io.File;
import java.util.concurrent.Executor;

import androidx.appcompat.app.AppCompatActivity;

public class UnlockTask {
    private final Executor executor;
    private final Handler handler;

    public UnlockTask(Executor executor, Handler handler) {
        this.executor = executor;
        this.handler = handler;
    }

    public void unlock(final AppContainer appContainer, final String plainPassword,
                       final TaskCallback<Void> callback) {
        executor.execute(() -> {
            if (appContainer.keyDb == null) {
                appContainer.keyDb = appContainer.keyDbFactory.create(plainPassword);
                if (appContainer.keyDb != null)
                    notifyResult(new TaskResult.Success<>(null), callback);
                else
                    notifyResult(new TaskResult.Error<>(null), callback);
            }
            else {
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
