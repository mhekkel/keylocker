package com.hekkelman.keylocker.tasks;

import android.content.Context;
import android.os.Handler;

import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.KeyDbException;
import com.hekkelman.keylocker.datamodel.Note;

import java.util.concurrent.Executor;

public class SaveNoteTask {

    private final Executor executor;
    private final Handler handler;

    public SaveNoteTask(Context context, Executor executor, Handler handler) {
        this.executor = executor;
        this.handler = handler;
    }

    public void saveNote(final Note note, final String name, final String text, final boolean finishOnSaved,
                         final TaskCallback<Boolean> callback) {
        executor.execute(() -> {
            try {
                KeyDb.setNote(note, name, text);
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
