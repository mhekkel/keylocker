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
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.hekkelman.keylocker.com.hekkelman.keylocker.datamodel.Key;
import com.hekkelman.keylocker.com.hekkelman.keylocker.datamodel.KeyDb;

public class KeyDetailActivity extends AppCompatActivity {

    private Key key = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_key_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            this.key = KeyDb.getInstance().getKey(getIntent().getStringExtra("keyId"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        EditText editText;

        String name = this.key.getName();
        if (name != null) {
            EditText fieldName = (EditText) findViewById(R.id.keyNameField);
            fieldName.setText(name);
        }

        editText = (EditText) findViewById(R.id.keyPasswordField);
        String password = this.key.getPassword();
        if (password != null) {
            editText.setText(password);
        }

        editText = (EditText) findViewById(R.id.keyUserField);
        String user= this.key.getUser();
        if (user!= null) {
            editText.setText(user);
        }

        String lastModified = this.key.getTimestamp();
        if (lastModified != null) {
            TextView field = (TextView)findViewById(R.id.lastModifiedCaption);
            field.setText( String.format(getString(R.string.lastModifiedTemplate), lastModified));
        }
    }
}
