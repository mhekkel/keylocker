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

//        // bottom toolbar
//
//        Toolbar toolbarBottom = (Toolbar) findViewById(R.id.toolbar_bottom);
//        toolbarBottom.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                switch(item.getItemId()){
//                    case R.id.action_settings:
//                        // TODO
//                        break;
//                    // TODO: Other cases
//                }
//                return true;
//            }
//        });
//        // Inflate a menu to be displayed in the toolbar
//        toolbarBottom.inflateMenu(R.menu.keymenu);

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

//    @Override
//    protected void onPause() {
//        KeyDb.setInstance(null);
//
//        super.onPause();
//    }
}
