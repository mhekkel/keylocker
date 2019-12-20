package com.hekkelman.keylocker;

import android.app.AlertDialog;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hekkelman.keylocker.datamodel.Key;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.KeyDbException;

import java.util.Random;

public class KeyDetailActivity extends AppCompatActivity {

    private String keyID;
    private boolean textChanged = false;

    @Bind(R.id.keyNameField) protected EditText nameField;
    @Bind(R.id.keyUserField) protected EditText userField;
    @Bind(R.id.keyPasswordField) protected EditText passwordField;
    @Bind(R.id.keyURLField) protected EditText urlField;
    @Bind(R.id.lastModifiedCaption) protected TextView lastModified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_key_detail);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().getBooleanExtra("restore-key", false)) {
            Key key = KeyDb.getCachedKey();
            if (key != null) {
                setKey(key);
                textChanged = true;
            }
            else
                finish();   // there was no key!
        }
        else
        {
            this.keyID = getIntent().getStringExtra("keyId");

            if (this.keyID == null)
            {
                lastModified.setVisibility(View.INVISIBLE);
            }
            else {
                Key key = KeyDb.getInstance().getKey(this.keyID);
                if (key == null) {
                    new AlertDialog.Builder(KeyDetailActivity.this)
                            .setTitle(R.string.dlog_missing_key_title)
                            .setMessage(R.string.dlog_missing_key_msg)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    return;
                }

                setKey(key);
            }
        }

        TextWatcher listener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                KeyDetailActivity.this.textChanged = true;
            }
        };

        nameField.addTextChangedListener(listener);
        passwordField.addTextChangedListener(listener);
        userField.addTextChangedListener(listener);
        urlField.addTextChangedListener(listener);
    }

    private void setKey(Key key) {
        this.keyID = key.getId();

        String name = key.getName();
        if (name != null) {
            nameField.setText(name);
        }

        String password = key.getPassword();
        if (password != null) {
            passwordField.setText(password);
        }

        String user= key.getUser();
        if (user!= null) {
            userField.setText(user);
        }

        String url= key.getUrl();
        if (url != null) {
            urlField.setText(url);
        }

        String lastModified = key.getTimestamp();
        if (lastModified != null) {
            this.lastModified.setText( String.format(getString(R.string.lastModifiedTemplate), lastModified));
        }
    }

    @Override
    public void onBackPressed() {
        if (textChanged == false) {
            finish();
        }
        else {
            new AlertDialog.Builder(KeyDetailActivity.this)
                    .setTitle(R.string.dlog_discard_changes_title)
                    .setMessage(R.string.dlog_discard_changes_msg)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            textChanged = false; // do not store this key again, please
                            KeyDb.removeCachedKey(keyID);
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
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
        else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }

    private boolean saveKey() {
        boolean result = false;

        String name = nameField.getText().toString();

        if (name == null || name.length() == 0) {
            nameField.setError(getString(R.string.keyNameIsRequired));
        } else {
            String user = userField.getText().toString();
            String password = passwordField.getText().toString();
            String url = urlField.getText().toString();

            try {
                KeyDb.getInstance().storeKey(keyID, name, user, password, url);

                result = true;
                this.textChanged = false;
                Toast.makeText(this, R.string.save_successful, Toast.LENGTH_SHORT).show();

                KeyDb.removeCachedKey(keyID);
            } catch (KeyDbException e) {
                new AlertDialog.Builder(KeyDetailActivity.this)
                        .setTitle(R.string.dlog_save_failed_title)
                        .setMessage(getString(R.string.dlog_save_failed_msg) + e.getMessage())
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }

        return result;
    }

    @Override
    protected void onPause() {

        // store the data in case we're about to disappear
        if (textChanged) {
            String name = nameField.getText().toString();
            String user = userField.getText().toString();
            String password = passwordField.getText().toString();
            String url = urlField.getText().toString();

            KeyDb.storeCachedKey(keyID, name, user, password, url);
        }

        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (KeyDb.getInstance() == null) {
            startActivity(new Intent(this, UnlockActivity.class));
            finish();
        } else {
            KeyDb.reference();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        KeyDb.release();
    }

    public void onClickRenewPassword(View v) {

        // get preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(KeyDetailActivity.this);
        int length = Integer.parseInt(prefs.getString("password-length", "8"));
        boolean noAmbiguous = prefs.getBoolean("password-no-ambiguous", true);
        boolean includeCapitals = prefs.getBoolean("password-include-capitals", true);
        boolean includeDigits = prefs.getBoolean("password-include-digits", true);
        boolean includeSymbols = prefs.getBoolean("password-include-symbols", true);

        String pw = generatePassword(length, noAmbiguous, includeCapitals, includeDigits, includeSymbols);

        passwordField.setText(pw);
    }

    public void onClickVisitURL(View v) {
        String url = urlField.getText().toString();
        if (url.startsWith("http://") == false)
            url = "http://" + url;

        try {
            Uri uri = Uri.parse(url);

            onCopyPassword(null);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(KeyDetailActivity.this, R.string.visitFailed, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.fab)
    public void onCopyPassword(View view) {
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);

        ClipData clip = ClipData.newPlainText("password", passwordField.getText().toString());
        clipboard.setPrimaryClip(clip);
    }

    private String generatePassword(int length, boolean noAmbiguous, boolean includeCapitals, boolean includeDigits, boolean includeSymbols)
    {
        final String kAmbiguous = "B8G6I1l0OQDS5Z2";

        String[] vowels = getString(R.string.vowels).split(";");
        String[] consonants = getString(R.string.consonants).split(";");
        String result = "";

        Random rng = new Random();

        boolean vowel = rng.nextBoolean();
        boolean wasVowel = false, hasDigits = false, hasSymbols = false, hasCapitals = false;

        for (;;)
        {
            if (result.length() >= length)
            {
                if (result.length() > length ||
                        includeDigits != hasDigits ||
                        includeSymbols != hasSymbols ||
                        includeCapitals != hasCapitals)
                {
                    result = "";
                    hasDigits = hasSymbols = hasCapitals = false;
                    continue;
                }

                break;
            }

            String s;
            if (vowel)
            {
                do
                    s = vowels[rng.nextInt(vowels.length)];
                while (wasVowel && s.length() > 1);
            }
            else
                s = consonants[rng.nextInt(consonants.length)];

            if (s.length() + result.length() > length)
                continue;

            if (noAmbiguous && kAmbiguous.contains(s))
                continue;

            if (includeCapitals && (result.length() == s.length() || vowel == false) && rng.nextInt(10) < 2)
            {
                result += s.toUpperCase();
                hasCapitals = true;
            }
            else
                result += s;

            if (vowel && (wasVowel || s.length() > 1 || rng.nextInt(10) > 3))
                vowel = false;
            else
                vowel = true;

            if (hasDigits == false && includeDigits && rng.nextInt(10) < 3)
            {
                String ch;
                do ch = new Character((char)(rng.nextInt(10) + '0')).toString();
                while (noAmbiguous && kAmbiguous.contains(ch));

                result += ch;
                hasDigits = true;
            }
            else if (hasSymbols == false && includeSymbols && rng.nextInt(10) < 2)
            {
                char[] kSymbols =
                        {
                                '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+',
                                ',', '-', '.', '/', ':', ';', '<', '=', '>', '?', '@',
                                '[', '\\', ']', '^', '_', '`', '{', '|', '}', '~',
                        };

                result += kSymbols[rng.nextInt(kSymbols.length)];
                hasSymbols = true;
            }
        }

        return result;
    }

}
