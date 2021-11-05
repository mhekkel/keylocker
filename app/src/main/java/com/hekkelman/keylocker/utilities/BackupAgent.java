package com.hekkelman.keylocker.utilities;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.hekkelman.keylocker.datamodel.KeyLockerFile;

import java.io.IOException;

public class BackupAgent extends BackupAgentHelper {
    static final String SETTINGS_BACKUP_KEY = "settings";
    static final String FILE_BACKUP_KEY = "file";

    // PreferenceManager.getDefaultSharedPreferencesName is only available in API > 24, this is its implementation
    String getDefaultSharedPreferencesName() {
        return getPackageName() + "_preferences";
    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
        Settings settings = new Settings(this);
        if (settings.getAndroidBackupServiceEnabled()) super.onBackup(oldState, data, newState);
    }

    @Override
    public void onCreate() {
        String prefs = getDefaultSharedPreferencesName();

        SharedPreferencesBackupHelper sharedPreferencesBackupHelper = new SharedPreferencesBackupHelper(this, prefs);
        addHelper(SETTINGS_BACKUP_KEY, sharedPreferencesBackupHelper);

        FileBackupHelper fileBackupHelper = new FileBackupHelper(this, KeyLockerFile.KEY_DB_NAME);
        addHelper(FILE_BACKUP_KEY, fileBackupHelper);
    }
}
