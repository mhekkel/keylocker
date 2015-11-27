package com.hekkelman.keylocker;

import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.hekkelman.keylocker.datamodel.InvalidPasswordException;
import com.hekkelman.keylocker.datamodel.KeyDb;

import java.io.File;

/**
 * Created by maarten on 27-11-15.
 */
public class Synchronize {

    enum SyncResult { SUCCESS, FAILED, CANCELLED, NEED_PASSWORD, MKDIR_FAILED}

    private static SyncTask sSyncTask;

    static SyncTask instance() {
        return sSyncTask;
    }

    static void syncWithSDCard(OnSyncTaskResult handler) {
        if (sSyncTask != null) {
            sSyncTask = new SyncTask(handler);
            sSyncTask.execute();
        }
    }

    static void syncWithSDCard(OnSyncTaskResult handler, String password) {
        if (sSyncTask != null) {
            sSyncTask = new SyncTask(handler);
            sSyncTask.execute(password);
        }
    }

    public interface OnSyncTaskResult {
        void syncResult(SyncResult result, String message, final OnSyncTaskResult handler);
    }

    private static class SyncTask extends AsyncTask<String, Void, SyncResult> {
        private String error;
        private OnSyncTaskResult handler;

        public SyncTask(OnSyncTaskResult handler) {
            this.handler = handler;
        }

        @Override
        protected SyncResult doInBackground(String... password) {
            try {
                File dir = new File(Environment.getExternalStorageDirectory(), "KeyLocker");
                if (dir.isDirectory() == false && dir.mkdir() == false)
                    return SyncResult.MKDIR_FAILED;

                File file = new File(dir, KeyDb.KEY_DB_NAME);

                if (password.length > 0)
                    KeyDb.getInstance().synchronize(file, password[0].toCharArray());
                else
                    KeyDb.getInstance().synchronize(file);

                return SyncResult.SUCCESS;
            } catch (InvalidPasswordException e) {
                return SyncResult.NEED_PASSWORD;
            } catch (Exception e) {
                this.error = e.getMessage();
                return SyncResult.FAILED;
            }
        }

        @Override
        protected void onPostExecute(final SyncResult result) {
            sSyncTask = null;

            try {
                handler.syncResult(result, error, handler);
            }
            catch (Exception e) {
            }
        }

        @Override
        protected void onCancelled() {
            sSyncTask = null;
            handler.syncResult(SyncResult.CANCELLED, null, handler);
        }
    }
}
