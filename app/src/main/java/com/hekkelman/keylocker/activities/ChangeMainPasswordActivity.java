//package com.hekkelman.keylocker.activities;
//
//import android.app.AlertDialog;
//import android.os.Bundle;
//import android.text.InputType;
//import android.widget.Button;
//import android.widget.EditText;
//
//import com.hekkelman.keylocker.R;
//import com.hekkelman.keylocker.datamodel.KeyDb;
//
//import androidx.appcompat.app.ActionBar;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.SwitchCompat;
//import androidx.appcompat.widget.Toolbar;
//
//public class ChangeMainPasswordActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_change_main_password);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        ActionBar supportActionBar = getSupportActionBar();
//        if (supportActionBar != null)
//            supportActionBar.setDisplayHomeAsUpEnabled(true);
//
//        final EditText pw1 = findViewById(R.id.password_1);
//        final EditText pw2 = findViewById(R.id.password_2);
//
//        final SwitchCompat sw = findViewById(R.id.numeric_cb);
//
//        sw.setOnCheckedChangeListener(
//                (buttonView, isChecked) -> {
//                    pw1.setText("");
//                    pw2.setText("");
//
//                    if (isChecked) {
//                        pw1.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
//                        pw2.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
//                    } else {
//                        pw1.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
//                        pw2.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
//                    }
//                }
//        );
//
//        Button btn = findViewById(R.id.change_pw_btn);
//        btn.setOnClickListener(
//                v -> {
//                    String password_1 = pw1.getText().toString();
//                    String password_2 = pw2.getText().toString();
//
//                    if (password_1.length() < 5)
//                        pw1.setError(getString(R.string.password_too_short));
//                    else if (!password_1.equals(password_2))
//                        pw2.setError(getString(R.string.passwords_do_not_match));
//                    else {
//                        try {
//                            KeyDb.changePassword(password_1.toCharArray());
//                            finish();
//                        } catch (Exception e) {
//                            new AlertDialog.Builder(ChangeMainPasswordActivity.this)
//                                    .setTitle(R.string.change_password_failed_title)
//                                    .setMessage(getString(R.string.change_password_failed_message) + e.getMessage())
//                                    .setIcon(android.R.drawable.ic_dialog_alert)
//                                    .show();
//                        }
//                    }
//                }
//        );
//    }
//}
