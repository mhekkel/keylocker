package com.hekkelman.keylocker.activities;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputEditText;
import com.hekkelman.keylocker.KeyLockerApp;
import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.databinding.ActivityChangeMainPasswordBinding;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.utilities.AppContainer;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

public class ChangeMainPasswordActivity extends AppCompatActivity {

    private TextInputEditText pw1;
    private TextInputEditText pw2;
    private AppContainer mAppContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAppContainer = ((KeyLockerApp) getApplication()).mAppContainer;

        ActivityChangeMainPasswordBinding binding = ActivityChangeMainPasswordBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null)
            supportActionBar.setDisplayHomeAsUpEnabled(true);

        pw1 = binding.passwordOne;
        pw2 = binding.passwordTwo;

        final SwitchCompat sw = binding.numericCheckBox;

        sw.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    pw1.setText("");
                    pw2.setText("");

                    if (isChecked) {
                        pw1.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
                        pw2.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
                    } else {
                        pw1.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                        pw2.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                    }
                }
        );

        Button btn = binding.changePwBtn;
        btn.setOnClickListener(this::onClickChangePassword);
    }

    private void onClickChangePassword(View v) {
        String password_1 = pw1.getText().toString();
        String password_2 = pw2.getText().toString();

        if (password_1.length() < 5)
            pw1.setError(getString(R.string.password_too_short));
        else if (!password_1.equals(password_2))
            pw2.setError(getString(R.string.passwords_do_not_match));
        else {
            try {
                mAppContainer.keyDb.changePassword(password_1);
                finish();
            } catch (Exception e) {
                new AlertDialog.Builder(ChangeMainPasswordActivity.this)
                        .setTitle(R.string.change_password_failed_title)
                        .setMessage(getString(R.string.change_password_failed_message) + e.getMessage())
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
    }
}
