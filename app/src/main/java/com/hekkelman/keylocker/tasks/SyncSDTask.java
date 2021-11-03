package com.hekkelman.keylocker.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.datamodel.InvalidPasswordException;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.KeyDbException;
import com.hekkelman.keylocker.datamodel.Note;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.Executor;

public class SyncSDTask {
    private final Executor executor;
    private final Handler handler;

    public SyncSDTask(Context context, Executor executor, Handler handler) {
        this.executor = executor;
        this.handler = handler;
    }

    public void syncToSD(Context context, Uri backupDir, String backupPassword,
                         final TaskCallback<Void> callback) {
        executor.execute(() -> {
//            try {
                // TODO implement
//                KeyDb.synchronize(context, backupDir, backupPassword != null ? backupPassword.toCharArray() : null);
                notifyResult(new TaskResult.Success<>(null), callback);
//            } catch (KeyDbException exception) {
//                notifyResult(new TaskResult.Error<>(exception), callback);
//            }
        });
    }

    private void notifyResult(
            final TaskResult<Void> result,
            final TaskCallback<Void> callback) {
        handler.post(() -> callback.onComplete(result));
    }


//
//    private final Context context;
//    private final Uri backupDir;
//    private final char[] backupPassword;

//    @NonNull
//    protected Result doInBackground() {
//        try {
//            KeyDb.synchronize(this.context, this.backupDir, this.backupPassword);
//            return Result.success();
//        } catch (InvalidPasswordException e) {
//            return Result.failureNeedPassword();
//        } catch (Exception e) {
//            return Result.failure(e.getMessage());
//        }
//    }
//
//    public static class Result {
//        public final boolean synced;
//        public final boolean needPassword;
//        public final String errorMessage;
//
//        public Result(boolean synced, boolean needPassword, String errorMessage) {
//            this.synced = synced;
//            this.needPassword = needPassword;
//            this.errorMessage = errorMessage;
//        }
//
//        public static Result success() {
//            return new Result(true, false, "");
//        }
//
//        public static Result failureNeedPassword() {
//            return new Result(false, true, "");
//        }
//
//        public static Result failure(String errorMessage) {
//            return new Result(false, false, errorMessage);
//        }
//    }
//

}
