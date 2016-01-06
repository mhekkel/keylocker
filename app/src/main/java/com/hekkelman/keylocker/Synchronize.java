package com.hekkelman.keylocker;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.hekkelman.keylocker.datamodel.InvalidPasswordException;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.onedrive.sdk.authentication.ADALAuthenticator;
import com.onedrive.sdk.authentication.MSAAuthenticator;
import com.onedrive.sdk.concurrency.ICallback;
import com.onedrive.sdk.core.ClientException;
import com.onedrive.sdk.core.DefaultClientConfig;
import com.onedrive.sdk.core.IClientConfig;
import com.onedrive.sdk.extensions.Drive;
import com.onedrive.sdk.extensions.IOneDriveClient;
import com.onedrive.sdk.extensions.OneDriveClient;

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
        if (sSyncTask == null) {
            sSyncTask = new SDSyncTask(handler);
            sSyncTask.execute();
        }
    }

    static void syncWithSDCard(OnSyncTaskResult handler, String password) {
        if (sSyncTask == null) {
            sSyncTask = new SDSyncTask(handler);
            sSyncTask.execute(password);
        }
    }

    static void syncWithOneDrive(OnSyncTaskResult handler) {
        if (sSyncTask == null) {
            sSyncTask = new OneDriveSyncTask(handler);
            sSyncTask.execute();
        }
    }

    static void syncWithOneDrive(OnSyncTaskResult handler, String password) {
        if (sSyncTask == null) {
            sSyncTask = new OneDriveSyncTask(handler);
            sSyncTask.execute(password);
        }
    }

    public interface OnSyncTaskResult {
        void syncResult(SyncResult result, String message, final OnSyncTaskResult handler);
        Activity getActivity();
    }

    private static abstract class SyncTask extends AsyncTask<String, Void, SyncResult> {
        protected String error;
        protected OnSyncTaskResult handler;

        public SyncTask(OnSyncTaskResult handler) {
            this.handler = handler;
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

    private static class SDSyncTask extends SyncTask {

        public SDSyncTask(OnSyncTaskResult handler) {
            super(handler);
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
    }

//    final ADALAuthenticator adalAuthenticator = new ADALAuthenticator() {
//        @Override
//        public String getClientId() {
//            return "<adal-client-id>";
//        }
//
//        @Override
//        protected String getRedirectUrl() {
//            return "https://localhost";
//        }
//    }

    private static class OneDriveSyncTask extends SyncTask {

        public OneDriveSyncTask(OnSyncTaskResult handler) {
            super(handler);

        }

        @Override
        protected SyncResult doInBackground(String... params) {
            return SyncResult.FAILED;
        }
    }

}
