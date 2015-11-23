package com.hekkelman.keylocker;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.hekkelman.keylocker.com.hekkelman.keylocker.datamodel.Key;
import com.hekkelman.keylocker.com.hekkelman.keylocker.datamodel.KeyDb;

import java.util.UUID;

public class KeyDetailActivity extends AppCompatActivity {

    private String keyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_key_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.keyID = getIntent().getStringExtra("keyId");
        if (this.keyID == null)
        {
            View view = findViewById(R.id.lastModifiedCaption);
            view.setVisibility(View.INVISIBLE);
        }
        else
        {
            try {
                Key key = KeyDb.getInstance().getKey(this.keyID);

                EditText editText;

                String name = key.getName();
                if (name != null) {
                    EditText fieldName = (EditText) findViewById(R.id.keyNameField);
                    fieldName.setText(name);
                }

                editText = (EditText) findViewById(R.id.keyPasswordField);
                String password = key.getPassword();
                if (password != null) {
                    editText.setText(password);
                }

                editText = (EditText) findViewById(R.id.keyUserField);
                String user= key.getUser();
                if (user!= null) {
                    editText.setText(user);
                }

                editText = (EditText) findViewById(R.id.keyURLField);
                String url= key.getUrl();
                if (url != null) {
                    editText.setText(url);
                }

                String lastModified = key.getTimestamp();
                if (lastModified != null) {
                    TextView field = (TextView)findViewById(R.id.lastModifiedCaption);
                    field.setText( String.format(getString(R.string.lastModifiedTemplate), lastModified));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.keymenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            return saveKey();
        }

        return super.onOptionsItemSelected(item);
    }

    private String getField(int fieldID, int nameID, boolean required) {
        EditText field = (EditText) findViewById(fieldID);
        String result = field.getText().toString();

        if (result == null || result.length() == 0) {
            if (required)
                field.setError(String.format("A %s is required", getString(nameID)));
            return null;
        }

        return result;
    }

    private boolean saveKey() {
        boolean result = false;

        try {
            EditText field;

            field = (EditText) findViewById(R.id.keyNameField);
            String name = field.getText().toString();

            if (name == null || name.length() == 0)
                field.setError(getString(R.string.keyNameIsRequired));
            ;
            String user = getField(R.id.keyUserField, R.string.keyUserCaptionHint, false);
            String password = getField(R.id.keyPasswordField, R.string.keyPasswordCaptionHint, false);
            String url = getField(R.id.keyURLField, R.string.keyURLCaptionHint, false);

            if (name != null) {
                KeyDb.getInstance().storeKey(keyID, name, user, password, url);
                result = true;
            }
        } catch (Exception e) {

        }

        return result;
    }


//    @Override
//    protected void onPause() {
//        KeyDb.setInstance(null);
//
//        super.onPause();
//    }
}
