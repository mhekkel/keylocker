package com.hekkelman.keylocker.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.hekkelman.keylocker.datamodel.KeyDbException;
import com.hekkelman.keylocker.datamodel.KeyNote;
import com.hekkelman.keylocker.utilities.AppContainer;
import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

public class SyncWebDAVTask {
    private final Executor executor;
    private final Handler handler;

    public SyncWebDAVTask(Executor executor, Handler handler) {
        this.executor = executor;
        this.handler = handler;
    }

    public void sync(final AppContainer appContainer,
                     final KeyNote.Key backupLocation,
                     final String password,
                     final boolean replacePassword, final TaskCallback<Void> callback) {
        executor.execute(() -> {
            try {
                Sardine sardine = new OkHttpSardine();

                sardine.setCredentials(backupLocation.getUser(), backupLocation.getPassword());

                if (sardine.exists(backupLocation.getUrl())) {
                    InputStream is = sardine.get(backupLocation.getUrl());

                    byte[] backup = appContainer.keyDb.synchronize(is, password, replacePassword);

                    if (backup != null)
                        sardine.put(backupLocation.getUrl(), backup, "text/plain");
                } else {
                    ByteArrayOutputStream backup = new ByteArrayOutputStream();
                    appContainer.keyDb.write(backup, null);
                    sardine.put(backupLocation.getUrl(), backup.toByteArray(), "text/plain");
                }

                notifyResult(new TaskResult.Success<>(null), callback);
            } catch (/*KeyDbException | */Exception exception) {
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
