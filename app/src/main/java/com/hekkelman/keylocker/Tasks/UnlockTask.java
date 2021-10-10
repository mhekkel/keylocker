package com.hekkelman.keylocker.Tasks;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hekkelman.keylocker.Utilities.EncryptionHelper;
import com.hekkelman.keylocker.Utilities.Settings;
import com.hekkelman.keylocker.Tasks.UnlockTask.Result;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.KeyDbException;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class UnlockTask extends UiBasedBackgroundTask<Result> {

    private final Settings settings;
    private final File keyDbFile;
    private final char[] plainPassword;

    public UnlockTask(Context context, File keyDbFile, String plainPassword) {
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
