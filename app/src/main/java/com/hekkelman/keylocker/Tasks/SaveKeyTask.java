package com.hekkelman.keylocker.Tasks;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.Utilities.Settings;
import com.hekkelman.keylocker.datamodel.Key;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.KeyDbException;

public class SaveKeyTask extends UiBasedBackgroundTask<SaveKeyTask.Result> {

    private final Settings settings;
    private final Key key;
    private final boolean finishOnSaved;

    public SaveKeyTask(Context context, Key key, boolean finishOnSaved) {
        super(Result.failure(context.getString(R.string.save_key_task_cancelled)));

        this.key = key;
        this.finishOnSaved = finishOnSaved;

        Context applicationContext = context.getApplicationContext();
        this.settings = new Settings(applicationContext);
    }

    //    @Override
    @NonNull
    protected Result doInBackground() {
        return saveKey();
    }

    @NonNull
    private Result saveKey() {
        try {
            KeyDb.setKey(this.key);
            return Result.success(this.finishOnSaved);
        } catch (KeyDbException exception) {
            return Result.failure(exception.getMessage());
        }
    }

    public static class Result {
        @Nullable
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
