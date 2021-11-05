package com.hekkelman.keylocker.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.hekkelman.keylocker.KeyLockerApp;
import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.datamodel.KeyDbViewModel;
import com.hekkelman.keylocker.datamodel.KeyLockerFile;
import com.hekkelman.keylocker.utilities.AppContainer;
import com.hekkelman.keylocker.utilities.Settings;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public abstract class KeyDbBaseActivity extends AppCompatActivity {

    protected Settings mSettings;
    protected KeyDbViewModel mViewModel;
    protected ActivityResultLauncher<Intent> mUnlockResult;
    protected Handler mHandler;
    protected Runnable mRunnable = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(KeyDbViewModel.class);
        mViewModel.locked.observe(this, this::onLockedChanged);

        mSettings = new Settings(this);

        mUnlockResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::onUnlockedResult);

        mHandler = new Handler();
        mRunnable = () -> mViewModel.locked.setValue(true);
    }

    private void onLockedChanged(Boolean locked) {
        if (locked) {
            if (!KeyLockerFile.exists()) {
                Intent intent = new Intent(this, InitActivity.class);
                mUnlockResult.launch(intent);
            } else {
                Intent authIntent = new Intent(this, UnlockActivity.class);
                mUnlockResult.launch(authIntent);
            }
        } else {
            if (mViewModel.keyDb == null) {
                AppContainer appContainer = ((KeyLockerApp) getApplication()).mAppContainer;
                mViewModel.keyDb = appContainer.keyDb;
            }
            loadData();
        }
    }

    protected abstract void loadData();

    public void onUnlockedResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_CANCELED)
            finish();
        else if (result.getResultCode() == UnlockActivity.RESET_KEY_LOCKER_FILE_RESULT) {
            Intent intent = new Intent(this, InitActivity.class);
            mUnlockResult.launch(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        startHandler();
    }

    @Override
    protected void onPause() {
        stopHandler();

        super.onPause();
    }

    @Override
    public void onUserInteraction() {
        stopHandler();
        startHandler();
        super.onUserInteraction();
    }

    protected void handleKeyDbException(String title, Exception e) {
        new AlertDialog.Builder(KeyDbBaseActivity.this)
                .setTitle(title)
                .setMessage(getString(R.string.sync_failed_msg) + e.getMessage())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void startHandler() {
        int secondsToBlackout = 1000 * mSettings.getAuthInactivityDelay();
        if (mSettings.getAuthInactivity() && secondsToBlackout != 0)
            mHandler.postDelayed(mRunnable, secondsToBlackout);
    }

    private void stopHandler() {
        mHandler.removeCallbacks(mRunnable);
    }
}
