package com.hekkelman.keylocker.Activities;

import android.app.AlertDialog;
import android.content.Intent;

import androidx.appcompat.widget.SwitchCompat;

import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.datamodel.KeyDb;

import java.io.File;

public class InitActivity extends BaseActivity
        implements EditText.OnEditorActionListener, View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    private TextInputEditText mPassword1;
    private TextInputEditText mPassword2;
    private SwitchCompat mPINSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        mPassword1 = findViewById(R.id.password_1);
        mPassword2 = findViewById(R.id.password_2);

        mPINSwitch = findViewById(R.id.numeric_cb);

        mPINSwitch.setOnCheckedChangeListener(this);

        Button btn = (Button) findViewById(R.id.create_btn);
        btn.setOnClickListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        KeyDb.release();
    }

    @Override
    public void onClick(View view) {
        String password_1 = mPassword1.getText().toString();
        String password_2 = mPassword2.getText().toString();

        if (password_1.length() < 5)
            mPassword1.setError("Password is too short");
        else if (password_1.equals(password_2) == false)
            mPassword2.setError("Passwords do not match");
        else {
            try {
                char[] password = password_1.toCharArray();

                KeyDb keyDb = new KeyDb(password, new File(getFilesDir(), KeyDb.KEY_DB_NAME));

                // write the empty keylocker file
                keyDb.write();

                KeyDb.setInstance(keyDb);

                settings.setUsePin(mPINSwitch.isChecked());

                finishWithResult(true, password);
            } catch (Exception e) {
                new AlertDialog.Builder(InitActivity.this)
                        .setTitle("Creating a Locker Failed")
                        .setMessage("Somehow, KeyLocker failed to create a new Locker file, the error is: " + e.getMessage())
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

    private void finishWithResult(boolean success, char[] encryptionKey) {
        Intent data = new Intent();
        if (encryptionKey != null)
            data.putExtra(UnlockActivity.EXTRA_AUTH_PASSWORD_KEY, encryptionKey);
        if (success)
            setResult(RESULT_OK, data);
        finish();
    }
}
