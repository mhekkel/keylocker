package com.hekkelman.keylocker.Tasks;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hekkelman.keylocker.Tasks.InitTask.Result;
import com.hekkelman.keylocker.Utilities.Settings;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.KeyDbException;

import java.io.File;

public class InitTask extends UiBasedBackgroundTask<Result> {

    private final Settings settings;
    private final File keyDbFile;
    private final char[] plainPassword;

    public InitTask(Context context, File keyDbFile, String plainPassword) {
        super(Result.failure());
        Context applicationContext = context.getApplicationContext();
        this.settings = new Settings(applicationContext);

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
        try {
            Log.e("UnlockTask", "Unlock attempt");
            KeyDb keyDb = new KeyDb(plainPassword, keyDbFile);
            return Result.success(plainPassword);
        } catch (IllegalArgumentException | KeyDbException e) {
            Log.e("UnlockTask", "Problem decoding password", e);
            return Result.failure();
        }
    }

    public static class Result {
        @Nullable
        public final char[] encryptionKey;

        public Result(@Nullable char[] encryptionKey) {
            this.encryptionKey = encryptionKey;
        }

        public static Result success(char[] encryptionKey) {
            return new Result(encryptionKey);
        }
        public static Result failure() {
            return new Result(null);
        }
    }
}
