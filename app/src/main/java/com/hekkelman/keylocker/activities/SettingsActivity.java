package com.hekkelman.keylocker.activities;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.InputType;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.utilities.Settings;

import java.util.Objects;

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

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private Settings settings;
        private ActivityResultLauncher<Intent> selectBackupDirResult;
        private ActivityResultLauncher<Intent> changeMainPasswordResult;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.prefs, rootKey);

            this.settings = new Settings(getActivity());

            changeMainPasswordResult = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(), this::onChangeMainPasswordResult);

            EditTextPreference numberPreference = findPreference(getString(R.string.settings_key_auth_inactivity_delay));
            if (numberPreference != null) {
                numberPreference.setOnBindEditTextListener(preference -> {
                    preference.setInputType(InputType.TYPE_CLASS_NUMBER);
                    preference.selectAll();
                });
            }
            numberPreference = findPreference(getString(R.string.settings_key_password_length));
            if (numberPreference != null) {
                numberPreference.setOnBindEditTextListener(preference -> {
                    preference.setInputType(InputType.TYPE_CLASS_NUMBER);
                    preference.selectAll();
                });
            }

            selectBackupDirResult = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(), this::onSelectBackupDirResult);

            // Main password
            Preference mainPassword = findPreference(getString(R.string.settings_key_main_password));
            if (mainPassword != null) {
                mainPassword.setOnPreferenceClickListener(preference -> {
                    requestNewMainPassword();
                    return true;
                });
            }

            // Backup location
            Preference backupLocation = findPreference(getString(R.string.settings_key_backup_dir));

            if (backupLocation != null) {
                if (!settings.getLocalBackupDir().isEmpty())
                    backupLocation.setSummary(R.string.settings_desc_backup_location_set);
                else
                    backupLocation.setSummary(R.string.settings_desc_backup_location_not_set);

                backupLocation.setOnPreferenceClickListener(preference -> {
                    requestBackupAccess();
                    return true;
                });
            }
        }

        private void requestNewMainPassword() {
            Intent intent = new Intent(getActivity(), ChangeMainPasswordActivity.class);
            changeMainPasswordResult.launch(intent);
        }

        private void onChangeMainPasswordResult(ActivityResult result) {
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
            assert backupLocation != null;
            backupLocation.setSummary(R.string.settings_desc_backup_location_not_set);

            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                assert data != null;
                Uri treeUri = data.getData();
                if (treeUri != null) {
                    final int takeFlags = data.getFlags()
                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    // Check for the freshest data.
                    requireActivity().getContentResolver().takePersistableUriPermission(treeUri, takeFlags);

                    settings.setLocalBackupDir(treeUri.toString());
                    backupLocation.setSummary(R.string.settings_desc_backup_location_set);
                }
            }
        }

    }
}
