package com.hekkelman.keylocker.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hekkelman.keylocker.KeyLockerApp;
import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.Note;
import com.hekkelman.keylocker.tasks.SaveNoteTask;
import com.hekkelman.keylocker.tasks.TaskResult;
import com.hekkelman.keylocker.utilities.AppContainer;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class NoteDetailActivity extends AppCompatActivity {

    protected EditText nameField;
    protected EditText textField;
    protected TextView lastModified;
    private Note note;
    private ActivityResultLauncher<Intent> unlockResult;
    private SaveNoteTask saveNoteTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        AppContainer appContainer = ((KeyLockerApp) getApplication()).appContainer;
        this.saveNoteTask = new SaveNoteTask(this, appContainer.getExecutorService(), appContainer.getMainThreadHandler());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        nameField = findViewById(R.id.noteNameField);
        textField = findViewById(R.id.noteTextField);

        lastModified = findViewById(R.id.key_last_modified);

        unlockResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::onUnlockedResult);

        Intent intent = getIntent();
        String noteID = intent.getStringExtra("note-id");

        if (noteID == null) {
            lastModified.setVisibility(View.INVISIBLE);
            setNote(new Note());
        } else {
            Note note = KeyDb.getNote(noteID);
            if (note == null) {
                new AlertDialog.Builder(NoteDetailActivity.this)
                        .setTitle(R.string.dlog_missing_note_title)
                        .setMessage(R.string.dlog_missing_note_msg)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return;
            }

            setNote(note);
        }
    }

    private void setNote(Note note) {
        this.note = note;

        String name = note.getName();
        if (name != null) nameField.setText(name);

        String text = note.getText();
        if (text != null) textField.setText(text);

        String lastModified = note.getTimestamp();
        if (lastModified != null)
            this.lastModified.setText(String.format(getString(R.string.lastModifiedTemplate), lastModified));
    }

    public void onUnlockedResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_CANCELED)
            finish();
    }

    @Override
    public void onBackPressed() {
        if (noteChanged()) {
            new AlertDialog.Builder(NoteDetailActivity.this)
                    .setTitle(R.string.dlog_discard_changes_title)
                    .setMessage(R.string.dlog_discard_changes_msg)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    })
                    .setNeutralButton(R.string.dialog_save_before_close, (dialog, which) -> saveNote(true))
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
            saveNote(false);
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    private void saveNote(boolean finishOnSaved) {
        String name = nameField.getText().toString();

        if (TextUtils.isEmpty(name))
            nameField.setError(getString(R.string.note_name_is_required));
        else
            saveNoteTask.saveNote(note, name, textField.getText().toString(), finishOnSaved, this::onTaskResult);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!KeyDb.isUnlocked()) {
            Intent authIntent = new Intent(this, UnlockActivity.class);
            unlockResult.launch(authIntent);
        }
    }

    void onTaskResult(TaskResult<Boolean> result) {
        if (result instanceof TaskResult.Success) {
            Toast.makeText(this, R.string.save_successful, Toast.LENGTH_SHORT).show();
            if (((TaskResult.Success<Boolean>)result).data)
                finish();
        } else {
            Exception exception = ((TaskResult.Error)result).exception;
            new AlertDialog.Builder(NoteDetailActivity.this)
                    .setTitle(R.string.dlog_save_failed_title)
                    .setMessage(getString(R.string.dlog_save_failed_msg) + exception.getMessage())
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private boolean noteChanged() {
        boolean same = (TextUtils.isEmpty(nameField.getText()) ? TextUtils.isEmpty(note.getName()) : TextUtils.equals(nameField.getText(), note.getName()))
                && (TextUtils.isEmpty(textField.getText()) ? TextUtils.isEmpty(note.getText()) : TextUtils.equals(textField.getText(), note.getText()));

        return !same;
    }
}
