package com.hekkelman.keylocker.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.hekkelman.keylocker.KeyLockerApp;
import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.databinding.ActivityInitBinding;
import com.hekkelman.keylocker.datamodel.KeyLockerFile;
import com.hekkelman.keylocker.utilities.AppContainer;
import com.hekkelman.keylocker.utilities.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.util.Objects;

public class InitActivity extends AppCompatActivity
        implements EditText.OnEditorActionListener, View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    private Settings mSettings;
    private AppContainer mAppContainer;
    private TextInputEditText mPassword1;
    private TextInputEditText mPassword2;
    private SwitchCompat mPINSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.mSettings = new Settings(this);

        super.onCreate(savedInstanceState);

        ActivityInitBinding binding = ActivityInitBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mAppContainer = ((KeyLockerApp) getApplication()).mAppContainer;

        mPassword1 = binding.passwordOne;
        mPassword2 = binding.passwordTwo;

        mPINSwitch = binding.numericCheckBox;

        mPINSwitch.setOnCheckedChangeListener(this);

        Button btn = binding.createBtn;
        btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String password_1 = Objects.requireNonNull(mPassword1.getText()).toString();
        String password_2 = Objects.requireNonNull(mPassword2.getText()).toString();

        if (password_1.length() < 5)
            mPassword1.setError(getString(R.string.password_too_short));
        else if (!password_1.equals(password_2))
            mPassword2.setError(getString(R.string.passwords_do_not_match));
        else {
            try {
                mAppContainer.keyDb = KeyLockerFile.initialize(this, password_1);
                mAppContainer.locked.setValue(false);
                mSettings.setUnlockKeyboard(mPINSwitch.isChecked() ? Settings.UnlockKeyboardMode.DIGITS : Settings.UnlockKeyboardMode.TEXT);
                finishWithResult(true);
            } catch (Exception e) {
                new AlertDialog.Builder(InitActivity.this)
                        .setTitle(R.string.dlog_creating_locker_failed)
                        .setMessage(getString(R.string.dlog_creating_locker_failed_body) + e.getMessage())
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        mPassword1.setText("");
        mPassword2.setText("");

        if (isChecked) {
            mPassword1.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
            mPassword2.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
        } else {
            mPassword1.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
            mPassword2.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
        }
    }

    @Override
    public void onBackPressed() {
        finishWithResult(false);
    }

    private void finishWithResult(boolean success) {
        Intent data = new Intent();
        if (success)
            setResult(RESULT_OK, data);
        finish();
    }
}
