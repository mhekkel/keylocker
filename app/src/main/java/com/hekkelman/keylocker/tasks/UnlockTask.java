package com.hekkelman.keylocker.tasks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hekkelman.keylocker.tasks.UnlockTask.Result;
import com.hekkelman.keylocker.datamodel.KeyDb;

import java.io.File;

public class UnlockTask extends UiBasedBackgroundTask<Result> {

    private final File keyDbFile;
    private final char[] plainPassword;

    public UnlockTask(File keyDbFile, String plainPassword) {
        super(Result.failure());

        this.keyDbFile = keyDbFile;
        this.plainPassword = plainPassword.toCharArray();
    }

    //    @Override
    @NonNull
    protected Result doInBackground() {
        return confirmAuthentication();
    }

    @NonNull
    private Result confirmAuthentication() {
        if (KeyDb.isValidPassword(plainPassword, keyDbFile))
            return Result.success(plainPassword);
        else
            return Result.failure();
    }

    public static class Result {
        @Nullable
        public final char[] encryptionKey;
        public final boolean requestReset;

        public Result(@Nullable char[] encryptionKey, boolean requestReset) {
            this.encryptionKey = encryptionKey;
            this.requestReset = requestReset;
        }

        public static Result success(char[] encryptionKey) {
            return new Result(encryptionKey, false);
        }

        public static Result failure() {
            return new Result(null, false);
        }
    }
}
