package com.hekkelman.keylocker;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.hekkelman.keylocker.datamodel.KeyDb;

import java.io.File;

public class ChangeMainPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_main_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText pw1 = (EditText)findViewById(R.id.password_1);
        final EditText pw2 = (EditText)findViewById(R.id.password_2);

        final Switch sw = (Switch) findViewById(R.id.numeric_cb);

        sw.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
                }
        );

        Button btn = (Button) findViewById(R.id.change_pw_btn);
        btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String password_1 = pw1.getText().toString();
                        String password_2 = pw2.getText().toString();

                        if (password_1.length() < 5)
                            pw1.setError("Password is too short");
                        else if (password_1.equals(password_2) == false)
                            pw2.setError("Passwords do not match");
                        else {
                            try {
                                KeyDb keyDb = KeyDb.getInstance();
                                keyDb.changePassword(password_1.toCharArray());
                                finish();
                            } catch (Exception e) {
                                new AlertDialog.Builder(ChangeMainPasswordActivity.this)
                                        .setTitle("Creating a Locker Failed")
                                        .setMessage("Somehow, KeyLocker failed to create a new Locker file, the error is: " + e.getMessage())
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                            }
                        }
                    }
                }
        );
    }

    @Override
    public void onStart() {
        super.onStart();
        KeyDb.reference();
    }

    @Override
    public void onStop() {
        super.onStop();
        KeyDb.release();
    }
}
