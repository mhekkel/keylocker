package com.hekkelman.keylocker.activities;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.hekkelman.keylocker.KeyLockerApp;
import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.databinding.ActivityKeyDetailBinding;
import com.hekkelman.keylocker.datamodel.KeyNote;
import com.hekkelman.keylocker.tasks.SaveKeyTask;
import com.hekkelman.keylocker.tasks.TaskResult;
import com.hekkelman.keylocker.utilities.AppContainer;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public class KeyDetailActivity extends KeyDbBaseActivity {

    private KeyNote.Key key;

    protected EditText nameField;
    protected EditText userField;
    protected EditText passwordField;
    protected EditText urlField;
    protected TextView lastModified;

    private SaveKeyTask saveKeyTask;
    private RelativeLayout container;

    public KeyDetailActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppContainer appContainer = ((KeyLockerApp) getApplication()).mAppContainer;
        this.saveKeyTask = new SaveKeyTask(appContainer.executorService, appContainer.mainThreadHandler);

        ActivityKeyDetailBinding binding = ActivityKeyDetailBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        container = binding.container;
        nameField = binding.keyNameField;
        userField = binding.keyUserField;
        passwordField = binding.keyPasswordField;
        urlField = binding.keyURLField;
        lastModified = binding.lastModifiedCaption;

        Intent intent = getIntent();
        String keyID = intent.getStringExtra("key-id");

        if (keyID == null) {
            lastModified.setVisibility(View.INVISIBLE);
            setKey(appContainer.keyDb.createKey());
        } else {
            Optional<KeyNote.Key> key = appContainer.keyDb.getKey(keyID);
            if (! key.isPresent()) {
                new AlertDialog.Builder(KeyDetailActivity.this)
                        .setTitle(R.string.dlog_missing_key_title)
                        .setMessage(R.string.dlog_missing_key_msg)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return;
            }

            setKey(key.get());
        }
    }

    @Override
    protected void loadData() {

    }

    private void setKey(KeyNote.Key key) {
        this.key = key;

        String name = key.getName();
        if (name != null) nameField.setText(name);

        String password = key.getPassword();
        if (password != null) passwordField.setText(password);

        String user = key.getUser();
        if (user != null) userField.setText(user);

        String url = key.getUrl();
        if (url != null) urlField.setText(url);

        String lastModified = key.getTimestamp();
        if (lastModified != null)
            this.lastModified.setText(String.format(getString(R.string.lastModifiedTemplate), lastModified));
    }

    @Override
    public void onBackPressed() {
        if (keyChanged()) {
            new AlertDialog.Builder(KeyDetailActivity.this)
                    .setTitle(R.string.dlog_discard_changes_title)
                    .setMessage(R.string.dlog_discard_changes_msg)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {})
                    .setNeutralButton(R.string.dialog_save_before_close, (dialog, which) -> saveKey(true))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.keymenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            saveKey(false);
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    private void saveKey(boolean finishOnSaved) {
        String name = nameField.getText().toString();

        if (TextUtils.isEmpty(name)) {
            nameField.setError(getString(R.string.key_name_is_required));
        } else {
            saveKeyTask.saveKey(mViewModel.keyDb, key, name, userField.getText().toString(),
                    passwordField.getText().toString(),
                    urlField.getText().toString(),
                    finishOnSaved, this::onTaskResult);
        }
    }

    public void onClickRenewPassword(View v) {
        int length = mSettings.getGeneratedPasswordLength();
        boolean noAmbiguous = mSettings.getGeneratedPasswordNoAmbiguous();
        boolean includeCapitals = mSettings.getGeneratedPasswordIncludeCapitals();
        boolean includeDigits = mSettings.getGeneratedPasswordIncludeDigits();
        boolean includeSymbols = mSettings.getGeneratedPasswordIncludeSymbols();

        String pw = generatePassword(length, noAmbiguous, includeCapitals, includeDigits, includeSymbols);

        passwordField.setText(pw);
    }

    public void onClickVisitURL(View v) {
        String url = urlField.getText().toString();
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            url = "http://" + url;

        try {
            Uri uri = Uri.parse(url);

            onCopyPassword(null);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            startActivity(intent);
        } catch (Exception e) {
            Snackbar.make(container, R.string.visitFailed, BaseTransientBottomBar.LENGTH_SHORT).show();
        }
    }

    //	@OnClick(R.id.fab)
    public void onCopyPassword(View view) {
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);

        ClipData clip = ClipData.newPlainText("password", passwordField.getText().toString());
        clipboard.setPrimaryClip(clip);
    }

    private String generatePassword(int length, boolean noAmbiguous, boolean includeCapitals, boolean includeDigits, boolean includeSymbols) {
        final String kAmbiguous = "B8G6I1l0OQDS5Z2";

        String[] vowels = getString(R.string.settings_default_password_vowels).split(";");
        String[] consonants = getString(R.string.settings_default_password_consonants).split(";");
        StringBuilder result = new StringBuilder();

        Random rng = new Random();

        boolean vowel = rng.nextBoolean();
        boolean hasDigits = false, hasSymbols = false, hasCapitals = false;

        for (; ; ) {
            if (result.length() >= length) {
                if (result.length() > length ||
                        includeDigits != hasDigits ||
                        includeSymbols != hasSymbols ||
                        includeCapitals != hasCapitals) {
                    result = new StringBuilder();
                    hasDigits = hasSymbols = hasCapitals = false;
                    continue;
                }

                break;
            }

            String s;
            if (vowel) s = vowels[rng.nextInt(vowels.length)];
            else s = consonants[rng.nextInt(consonants.length)];

            if (s.length() + result.length() > length)
                continue;

            if (noAmbiguous && kAmbiguous.contains(s))
                continue;

            if (includeCapitals && (result.length() == s.length() || !vowel) && rng.nextInt(10) < 2) {
                result.append(s.toUpperCase(Locale.ROOT));
                hasCapitals = true;
            } else
                result.append(s);

            vowel = !vowel || (s.length() <= 1 && rng.nextInt(10) <= 3);

            if (!hasDigits && includeDigits && rng.nextInt(10) < 3) {
                String ch;
                do ch = Character.valueOf((char) (rng.nextInt(10) + '0')).toString();
                while (noAmbiguous && kAmbiguous.contains(ch));

                result.append(ch);
                hasDigits = true;
            } else if (!hasSymbols && includeSymbols && rng.nextInt(10) < 2) {
                char[] kSymbols =
                        {
                                '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+',
                                ',', '-', '.', '/', ':', ';', '<', '=', '>', '?', '@',
                                '[', '\\', ']', '^', '_', '`', '{', '|', '}', '~',
                        };

                result.append(kSymbols[rng.nextInt(kSymbols.length)]);
                hasSymbols = true;
            }
        }

        return result.toString();
    }

    public void onTaskResult(TaskResult<Boolean> result) {
        if (result instanceof TaskResult.Success) {
            Toast.makeText(this, R.string.save_successful, Toast.LENGTH_SHORT).show();
            if (((TaskResult.Success<Boolean>)result).data)
                finish();
        } else {
            Exception exception = ((TaskResult.Error)result).exception;
            new AlertDialog.Builder(KeyDetailActivity.this)
                    .setTitle(R.string.dlog_save_failed_title)
                    .setMessage(getString(R.string.dlog_save_failed_msg) + exception.getMessage())
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private boolean keyChanged() {
        boolean same = (TextUtils.isEmpty(nameField.getText()) ? TextUtils.isEmpty(key.getName()) : TextUtils.equals(nameField.getText(), key.getName()))
                && (TextUtils.isEmpty(userField.getText()) ? TextUtils.isEmpty(key.getUser()) : TextUtils.equals(userField.getText(), key.getUser()))
                && (TextUtils.isEmpty(passwordField.getText()) ? TextUtils.isEmpty(key.getPassword()) : TextUtils.equals(passwordField.getText(), key.getPassword()))
                && (TextUtils.isEmpty(urlField.getText()) ? TextUtils.isEmpty(key.getUrl()) : TextUtils.equals(urlField.getText(), key.getUrl()));

        return !same;
    }
}
