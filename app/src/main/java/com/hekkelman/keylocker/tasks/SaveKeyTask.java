package com.hekkelman.keylocker.tasks;

import android.content.Context;

import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.datamodel.Key;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.KeyDbException;

import androidx.annotation.NonNull;

public class SaveKeyTask extends UiBasedBackgroundTask<SaveKeyTask.Result> {

    private final Key key;
    private final String name;
    private final String user;
    private final String password;
    private final String url;
    private final boolean finishOnSaved;

    public SaveKeyTask(Context context, Key key, String name, String user, String password, String url, boolean finishOnSaved) {
        super(Result.failure(context.getString(R.string.save_key_task_cancelled)));

        this.key = key;
        this.name = name;
        this.user = user;
        this.password = password;
        this.url = url;
        this.finishOnSaved = finishOnSaved;
    }

    //    @Override
    @NonNull
    protected Result doInBackground() {
        return saveKey();
    }

    @NonNull
    private Result saveKey() {
        try {
            KeyDb.setKey(key, this.name, this.user, this.password, this.url);
            return Result.success(this.finishOnSaved);
        } catch (KeyDbException exception) {
            return Result.failure(exception.getMessage());
        }
    }

    public static class Result {
        public final boolean saved;
        public final boolean finish;
        public final String errorMessage;

        public Result(boolean saved, boolean finish, String errorMessage) {
            this.saved = saved;
            this.finish = finish;
            this.errorMessage = errorMessage;
        }

        public static Result success(boolean finish) {
            return new Result(true, finish, "");
        }

        public static Result failure(String errorMessage) {
            return new Result(false, false, errorMessage);
        }
    }
}
