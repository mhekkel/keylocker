package com.hekkelman.keylocker.Activities;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.Utilities.Settings;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == android.R.id.home) {
//            finish();
//        }
//        return super.onOptionsItemSelected(item);
//    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private Settings settings;
        private ActivityResultLauncher<Intent> selectBackupDirResult;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.prefs, rootKey);

            this.settings = new Settings(getActivity());

            selectBackupDirResult = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(), this::onSelectBackupDirResult);

            // Backup location
            Preference backupLocation = findPreference(getString(R.string.settings_key_backup_dir));

            if (! settings.getLocalBackupDir().isEmpty()) {
                backupLocation.setSummary(R.string.settings_desc_backup_location_set);
            } else {
                backupLocation.setSummary(R.string.settings_desc_backup_location_not_set);
            }

            backupLocation.setOnPreferenceClickListener(preference -> {
                requestBackupAccess();
                return true;
            });

        }

        public void requestBackupAccess() {
            String uri = settings.getLocalBackupDir();

            // Choose a directory using the system's file picker.
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker when it loads.
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(uri));

            selectBackupDirResult.launch(intent);
        }

        @SuppressLint("WrongConstant")
        protected void onSelectBackupDirResult(ActivityResult result) {
            Preference backupLocation = findPreference(getString(R.string.settings_key_backup_dir));
            backupLocation.setSummary(R.string.settings_desc_backup_location_not_set);

            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                Uri treeUri = data.getData();
                if (treeUri != null) {
                    final int takeFlags = data.getFlags()
                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    // Check for the freshest data.
                    getActivity().getContentResolver().takePersistableUriPermission(treeUri, takeFlags);

                    settings.setLocalBackupDir(treeUri.toString());
                    backupLocation.setSummary(R.string.settings_desc_backup_location_set);
                }
            }
        }

    }
}
