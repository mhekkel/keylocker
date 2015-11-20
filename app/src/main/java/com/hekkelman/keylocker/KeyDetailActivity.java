package com.hekkelman.keylocker;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.hekkelman.keylocker.com.hekkelman.keylocker.datamodel.Key;
import com.hekkelman.keylocker.com.hekkelman.keylocker.datamodel.KeyDb;

public class KeyDetailActivity extends AppCompatActivity {

    private Key key = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_key_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            this.key = KeyDb.getInstance().getKey(getIntent().getStringExtra("keyId"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String name = this.key.getName();
        if (name != null) {
            EditText fieldName = (EditText) findViewById(R.id.keyNameField);
            fieldName.setText(name);
        }

        EditText fieldPassword = (EditText) findViewById(R.id.keyPasswordField);
        String password = this.key.getPassword();
        if (password != null) {
            fieldPassword.setText(password);
        }
    }
}
