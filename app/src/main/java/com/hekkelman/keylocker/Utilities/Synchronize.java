package com.hekkelman.keylocker.Utilities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;

//import android.support.v4.content.ContextCompat;

import androidx.core.content.ContextCompat;

import com.hekkelman.keylocker.Activities.BaseActivity;
import com.hekkelman.keylocker.datamodel.InvalidPasswordException;
import com.hekkelman.keylocker.datamodel.KeyDb;
//import com.onedrive.sdk.extensions.IOneDriveClient;
//import com.onedrive.sdk.extensions.Item;

import java.io.File;

//import static android.support.v7.widget.StaggeredGridLayoutManager.TAG;

/**
 * Created by maarten on 27-11-15.
 */
public class Synchronize {

    public enum SyncResult { SUCCESS, FAILED, CANCELLED, NEED_PASSWORD, PERMISSION_DENIED, MEDIA_NOT_MOUNTED, MKDIR_FAILED}

    private static SyncTask sSyncTask;

    static SyncTask instance() {
        return sSyncTask;
    }

    static void syncWithSDCard(OnSyncTaskResult handler, BaseActivity app, String... password) {
        if (sSyncTask == null) {
            sSyncTask = new SDSyncTask(handler, app);
            sSyncTask.execute(password);
        }
    }

//    static void syncWithOneDrive(OnSyncTaskResult handler, BaseApplication app, String... password) {
//        if (sSyncTask == null) {
//            sSyncTask = new OneDriveSyncTask(handler, app);
//            sSyncTask.execute(password);
//        }
//    }

    public interface OnSyncTaskResult {
        void syncResult(SyncResult result, String message, final SyncTask task);
        Activity getActivity();
    }

    public static abstract class SyncTask extends AsyncTask<String, Void, SyncResult> {
        protected String error;
        protected OnSyncTaskResult handler;

        public SyncTask(OnSyncTaskResult handler) {
            this.handler = handler;
        }

        @Override
        protected void onPostExecute(final SyncResult result) {
            sSyncTask = null;

            try {
                handler.syncResult(result, error, this);
            }
            catch (Exception ignored) {
            }
        }

        @Override
        protected void onCancelled() {
            sSyncTask = null;
            handler.syncResult(SyncResult.CANCELLED, null, this);
        }

        public abstract void retryWithPassword(String password);
    }

    private static class SDSyncTask extends SyncTask {

        private final BaseActivity app;

        public SDSyncTask(OnSyncTaskResult handler, BaseActivity app) {
            super(handler);
            this.app = app;
        }

        @Override
        public void retryWithPassword(String password) {
            Synchronize.syncWithSDCard(handler, this.app, password);
        }

        @Override
        protected SyncResult doInBackground(String... password) {
            try {
                Context context = app.getApplicationContext();

                final boolean extStoragePermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED;

                if (extStoragePermission == false)
                    return SyncResult.PERMISSION_DENIED;

                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) == false)
                    return SyncResult.MEDIA_NOT_MOUNTED;

                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "KeyLocker");

                if (dir.isDirectory() == false && dir.mkdirs() == false)
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

//    private static class OneDriveSyncTask extends SyncTask {
//
//        private final BaseApplication app;
//        private final ProgressDialog dlog;
//
//        public OneDriveSyncTask(OnSyncTaskResult handler, BaseApplication app) {
//            super(handler);
//
//            this.app = app;
//            this.dlog = ProgressDialog.show(handler.getActivity(),
//                    app.getString(R.string.oneDriveSyncProgressTitle),
//                    app.getString(R.string.oneDriveSyncProgressMessageInit),
//                    false, true,
//                    new DialogInterface.OnCancelListener() {
//                        @Override
//                        public void onCancel(DialogInterface dialog) {
//                            OneDriveSyncTask.this.onStopped();
//                        }
//                    });
//        }
//
//        private void onStopped() {
//            cancel(false);
//        }
//
//        private void setProgressMessage(final CharSequence message) {
//            handler.getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    dlog.setMessage(message);
//                }
//            });
//        }
//
//        private void dismissProgressDialog() {
//            handler.getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    dlog.dismiss();
//                }
//            });
//        }
//
//        @Override
//        protected SyncResult doInBackground(String... password) {
//            try {
//                IOneDriveClient oneDriveClient = app.getOneDriveClient();
//
//                setProgressMessage(app.getString(R.string.oneDriveSyncProgressMessage2));
//
//                Item dir = oneDriveClient
//                        .getDrive()
//                        .getSpecial("approot")
//                        .buildRequest()
//                        .expand("children")
//                        .select("id,name")
//                        .get();
//
//                if (isCancelled())
//                    return SyncResult.CANCELLED;
//
//                if (dir.children != null) {
//                    for (Item i: dir.children.getCurrentPage()) {
//                        Log.d("INFO", "item: " + i.name);
//
//                        if (i.name.equals("keys.xml"))
//                        {
//                            setProgressMessage(app.getString(R.string.oneDriveSyncProgressMessage3));
//
//                            InputStream file = oneDriveClient
//                                    .getDrive()
//                                    .getItems(i.id)
//                                    .getContent()
//                                    .buildRequest()
//                                    .get();
//
//                            if (isCancelled())
//                                return SyncResult.CANCELLED;
//
//                            setProgressMessage(app.getString(R.string.oneDriveSyncProgressMessage4));
//                            if (password.length > 0)
//                                KeyDb.getInstance().synchronize(file, password[0].toCharArray());
//                            else
//                                KeyDb.getInstance().synchronize(file);
//                        }
//                    }
//                }
//
//                if (isCancelled())
//                    return SyncResult.CANCELLED;
//
//                setProgressMessage(app.getString(R.string.oneDriveSyncProgressMessage5));
//
//                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//                KeyDb.getInstance().write(outputStream);
//
//                if (isCancelled())
//                    return SyncResult.CANCELLED;
//
//                oneDriveClient
//                        .getDrive()
//                        .getItems(dir.id)
//                        .getChildren()
//                        .byId("keys.xml")
//                        .getContent()
//                        .buildRequest()
//                        .put(outputStream.toByteArray());
//
//                return SyncResult.SUCCESS;
//            } catch (InvalidPasswordException e) {
//                return SyncResult.NEED_PASSWORD;
//            } catch (Exception e) {
//                this.error = e.getMessage();
//                return SyncResult.FAILED;
//            }
//            finally {
//                dismissProgressDialog();
//            }
//        }
//
//        @Override
//        public void retryWithPassword(String password) {
//            Synchronize.syncWithOneDrive(handler, app, password);
//        }
//    }
}
