package com.hekkelman.keylocker.Tasks;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.Utilities.Settings;
import com.hekkelman.keylocker.Utilities.Synchronize;
import com.hekkelman.keylocker.datamodel.Key;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.KeyDbException;

import java.io.File;

public class SyncSDTask extends UiBasedBackgroundTask<SyncSDTask.Result> {

    private final Settings settings;
    private final Context context;
    private final Key key;
    private final boolean finishOnSaved;

    public SyncSDTask(Context context, Key key, boolean finishOnSaved) {
        super(Result.failure(context.getString(R.string.save_key_task_cancelled)));

        this.context = context;
        this.key = key;
        this.finishOnSaved = finishOnSaved;

        Context applicationContext = context.getApplicationContext();
        this.settings = new Settings(applicationContext);
    }

    //    @Override
    @NonNull
    protected Result doInBackground() {

//        File dir = ;

//        if (dir.isDirectory() == false && dir.mkdirs() == false)
//            return Synchronize.SyncResult.MKDIR_FAILED;
//
//        File file = new File(dir, KeyDb.KEY_DB_NAME);
//
////                if (password.length > 0)
////                    KeyDb.getInstance().synchronize(file, password[0].toCharArray());
////                else
////                    KeyDb.getInstance().synchronize(file);
//
//        return Synchronize.SyncResult.SUCCESS;
////            } catch (InvalidPasswordException e) {
////                return SyncResult.NEED_PASSWORD;
//    } catch (Exception e) {
//        this.error = e.getMessage();
//        return Synchronize.SyncResult.FAILED;
//    }
        return Result.failure("fout");

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
