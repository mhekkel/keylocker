package com.hekkelman.keylocker.activities;


import static android.provider.Settings.*;
import static androidx.biometric.BiometricManager.*;
import static androidx.biometric.BiometricManager.Authenticators.*;

import android.annotation.SuppressLint;
import android.app.backup.BackupManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.InputType;
import android.text.TextUtils;

import com.hekkelman.keylocker.KeyLockerApp;
import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.datamodel.KeyNote;
import com.hekkelman.keylocker.utilities.AppContainer;
import com.hekkelman.keylocker.utilities.Settings;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import androidx.biometric.BiometricManager;


import java.util.Optional;

public class SettingsActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        BackupManager backupManager = new BackupManager(this);
        backupManager.dataChanged();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private Settings settings;
        private ActivityResultLauncher<Intent> selectBackupDirResult;
        private ActivityResultLauncher<Intent> selectBackupWebDAVKeyIDResult;
        private ActivityResultLauncher<Intent> changeMainPasswordResult;
        private ActivityResultLauncher<Intent> enrollFingerprint;

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

            // Main password
            Preference mainPassword = findPreference(getString(R.string.settings_key_main_password));
            if (mainPassword != null) {
                mainPassword.setOnPreferenceClickListener(preference -> {
                    requestNewMainPassword();
                    return true;
                });
            }

            // Biometrics
            enrollFingerprint = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onFingerprintEnrolled);

            Preference biometricLogin = findPreference(getString(R.string.settings_key_biometric_login));
            if (biometricLogin != null) {
                BiometricManager biometricManager = from(getContext());

                switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
                    case BIOMETRIC_SUCCESS:
                        biometricLogin.setOnPreferenceChangeListener((preference, newValue) -> true);
                        break;

                    case BIOMETRIC_ERROR_NO_HARDWARE:
                        settings.setUseBiometricLogin(false);
                        biometricLogin.setEnabled(false);
                        biometricLogin.setSummary(R.string.settings_desc_biometric_no_hardware);
                        break;

                    case BIOMETRIC_ERROR_NONE_ENROLLED:
                        settings.setUseBiometricLogin(false);

                        biometricLogin.setOnPreferenceChangeListener((preference, newValue) -> {
                            if ((Boolean) newValue == true) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                                    startFingerprintEnrollment();
                                else
                                    biometricLogin.setSummary(R.string.settings_desc_biometric_no_fingerprints);
                                return false;
                            } else
                                return true;
                        });

                        break;

                    case BIOMETRIC_ERROR_HW_UNAVAILABLE:
                    default:
                        settings.setUseBiometricLogin(false);
                        biometricLogin.setEnabled(false);
                        biometricLogin.setSummary(R.string.settings_desc_biometric_not_available);
                        break;

                }
            }

            selectBackupDirResult = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(), this::onSelectBackupDirResult);

            // Backup location
            Preference backupLocation = findPreference(getString(R.string.settings_key_backup_dir));
            assert backupLocation != null;

            if (!settings.getLocalBackupDir().isEmpty())
                backupLocation.setSummary(R.string.settings_desc_backup_location_set);
            else
                backupLocation.setSummary(R.string.settings_desc_backup_location_not_set);

            backupLocation.setOnPreferenceClickListener(preference -> {
                requestBackupAccess();
                return true;
            });

            selectBackupWebDAVKeyIDResult = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(), this::onSelectBackupWebDAVKeyIDResult);

            Preference webdavBackupLocation = findPreference(getString(R.string.settings_key_backup_webdav));
            assert webdavBackupLocation != null;

            // find the webdav key, if it exists...
            AppContainer appContainer = ((KeyLockerApp) getActivity().getApplication()).mAppContainer;
            String webdavKeyID = settings.getWebDAVBackupKeyID();

            if (!TextUtils.isEmpty(webdavKeyID)) {
                Optional<KeyNote.Key> key = appContainer.keyDb.getKey(webdavKeyID);
                if (key.isEmpty() || key.get().isDeleted())
                    webdavKeyID = null;
            }

            if (TextUtils.isEmpty(webdavKeyID)) {
                String defaultWebDAVKeyID = getString(R.string.webdav_key_id);
                Optional<KeyNote.Key> key = appContainer.keyDb.getAllKeys()
                        .stream()
                        .filter(k -> k.getName().equals(defaultWebDAVKeyID))
                        .findFirst();
                if (key.isPresent() && !key.get().isDeleted())
                    webdavKeyID = defaultWebDAVKeyID;
            }

            webdavBackupLocation.setSummary(TextUtils.isEmpty(webdavKeyID) ?
                    R.string.settings_desc_backup_location_not_set :
                    R.string.settings_desc_backup_location_set);

            String finalWebdavKeyID = webdavKeyID;
            webdavBackupLocation.setOnPreferenceClickListener(preference -> {
                requestBackupKey(finalWebdavKeyID);
                return true;
            });

        }

        private void onFingerprintEnrolled(ActivityResult activityResult) {
            BiometricManager biometricManager = from(getContext());
            if (biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS) {
                settings.setUseBiometricLogin(true);

                Preference biometricLogin = findPreference(getString(R.string.settings_key_biometric_login));
                biometricLogin.setEnabled(true);
                biometricLogin.setOnPreferenceChangeListener((preference, newValue) -> true);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.R)
        private void startFingerprintEnrollment() {
            final Intent enrollIntent = new Intent(ACTION_BIOMETRIC_ENROLL);
            enrollIntent.putExtra(EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BIOMETRIC_STRONG);
            enrollFingerprint.launch(enrollIntent);
        }

        protected void onSelectBackupWebDAVKeyIDResult(ActivityResult result) {
            Preference webdavKey = findPreference(getString(R.string.settings_key_backup_webdav));
            assert webdavKey != null;
            webdavKey.setSummary(R.string.settings_desc_backup_location_not_set);

            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                assert data != null;
                String keyID = data.getStringExtra(KeyDetailActivity.RESULT_KEY_ID);
                if (keyID != null) {
                    settings.setWebDAVBackupKeyID(keyID);
                    webdavKey.setSummary(R.string.settings_desc_backup_location_set);
                }
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

        public void requestBackupKey(String webdavKeyID) {
            Intent intent = new Intent(getActivity(), KeyDetailActivity.class);
            if (!TextUtils.isEmpty(webdavKeyID))
                intent.putExtra("key-id", webdavKeyID);
            intent.putExtra("webdav-key", true);
            selectBackupWebDAVKeyIDResult.launch(intent);
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
