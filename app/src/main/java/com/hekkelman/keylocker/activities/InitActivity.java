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
import com.hekkelman.keylocker.datamodel.KeyDbFactory;
import com.hekkelman.keylocker.utilities.AppContainer;
import com.hekkelman.keylocker.utilities.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

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
        setContentView(R.layout.activity_init);

        mAppContainer = ((KeyLockerApp) getApplication()).mAppContainer;

        mPassword1 = findViewById(R.id.password_1);
        mPassword2 = findViewById(R.id.password_2);

        mPINSwitch = findViewById(R.id.numeric_cb);

        mPINSwitch.setOnCheckedChangeListener(this);

        Button btn = findViewById(R.id.create_btn);
        btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String password_1 = mPassword1.getText().toString();
        String password_2 = mPassword2.getText().toString();

        if (password_1.length() < 5)
            mPassword1.setError(getString(R.string.password_too_short));
        else if (!password_1.equals(password_2))
            mPassword2.setError(getString(R.string.passwords_do_not_match));
        else {
            try {
                mAppContainer.keyDb = mAppContainer.keyDbFactory.initialize(password_1);
                mAppContainer.locked.setValue(false);
                mSettings.setUsePin(mPINSwitch.isChecked());
                finishWithResult();
            } catch (Exception e) {
                new AlertDialog.Builder(InitActivity.this)
                        .setTitle(R.string.dlog_creating_locker_failed)
                        .setMessage(getString(R.string.dlog_creating_locker_failed_body) + e.getMessage())
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
        finishWithResult();
    }

    private void finishWithResult() {
        Intent data = new Intent();
        setResult(RESULT_OK, data);
        finish();
    }
}
