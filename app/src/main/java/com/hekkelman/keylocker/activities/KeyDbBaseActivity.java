package com.hekkelman.keylocker.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.hekkelman.keylocker.KeyLockerApp;
import com.hekkelman.keylocker.datamodel.KeyDbViewModel;
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
    protected ActivityResultLauncher<Intent> mInitResult;
    protected ActivityResultLauncher<Intent> mUnlockResult;
    private CountDownTimer mCountDownTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(KeyDbViewModel.class);

        mViewModel.locked.observe(this, this::onLockedChanged);

        mSettings = new Settings(this);

        mInitResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::onUnlockedResult);

        mUnlockResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::onUnlockedResult);
    }

    private void onLockedChanged(Boolean locked) {
        if (locked) {
            Intent authIntent = new Intent(this, UnlockActivity.class);
            mUnlockResult.launch(authIntent);
        }
        else
        {
            if (mViewModel.keyDb == null) {
                AppContainer appContainer = ((KeyLockerApp)getApplication()).mAppContainer;
                mViewModel.keyDb = appContainer.keyDb;
            }
            loadData();
        }
    }

    protected abstract void loadData();

    public void onUnlockedResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_CANCELED)
            finish();
        else if (result.getResultCode() == Activity.RESULT_FIRST_USER) {
            Intent intent = new Intent(this, InitActivity.class);
            mUnlockResult.launch(intent);
        }
        else if (result.getResultCode() == Activity.RESULT_OK) {
            AppContainer appContainer = ((KeyLockerApp)getApplication()).mAppContainer;
            mViewModel.keyDb = appContainer.keyDb;
            mViewModel.locked.setValue(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (setCountDownTimerNow())
            mCountDownTimer.start();
    }

    @Override
    protected void onPause() {
        if (mCountDownTimer != null)
            mCountDownTimer.cancel();

        super.onPause();
    }

    private boolean setCountDownTimerNow() {
        try {
            int secondsToBlackout = 1000 * mSettings.getAuthInactivityDelay();

            if (!mSettings.getAuthInactivity() || secondsToBlackout == 0)
                return false;

            mCountDownTimer = new CountDownTimer(secondsToBlackout, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    mViewModel.locked.setValue(true);
                    this.cancel();
                }
            };

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
