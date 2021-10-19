package com.hekkelman.keylocker.Tasks;

import android.content.Context;
import android.net.Uri;

import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.datamodel.InvalidPasswordException;
import com.hekkelman.keylocker.datamodel.KeyDb;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SyncSDTask extends UiBasedBackgroundTask<SyncSDTask.Result> {

    private final Context context;
    private final Uri backupDir;
    private final char[] backupPassword;

    public SyncSDTask(Context context, Uri backupDir, String backupPassword) {
        super(Result.failure(context.getString(R.string.sync_task_cancelled)));

        this.context = context;
        this.backupPassword = backupPassword != null ? backupPassword.toCharArray() : null;
        this.backupDir = backupDir;
    }

    @NonNull
    protected Result doInBackground() {
        try {
            KeyDb.synchronize(this.context, this.backupDir, this.backupPassword);
            return Result.success();
        } catch (InvalidPasswordException e) {
            return Result.failureNeedPassword();
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public static class Result {
        @Nullable
        public final boolean synced;
        public final boolean needPassword;
        public final String errorMessage;

        public Result(boolean synced, boolean needPassword, String errorMessage) {
            this.synced = synced;
            this.needPassword = needPassword;
            this.errorMessage = errorMessage;
        }

        public static Result success() {
            return new Result(true, false, "");
        }

        public static Result failureNeedPassword() {
            return new Result(false, true, "");
        }

        public static Result failure(String errorMessage) {
            return new Result(false, false, errorMessage);
        }
    }


}
