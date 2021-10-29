package com.hekkelman.keylocker.datamodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hekkelman.keylocker.utilities.Settings;

import java.io.File;

public class KeyDbModel extends AndroidViewModel {
    private KeyDb keyDb;
    private final Settings settings;
    private final MutableLiveData<Key> selectedKey = new MutableLiveData<>();
    private final MutableLiveData<Note> selectedNote = new MutableLiveData<>();
    private final File keyFile;

    public KeyDbModel(@NonNull Application application) {
        super(application);
        settings = new Settings(application);

        keyFile = new File(getApplication().getFilesDir(), KeyDb.KEY_DB_NAME);
        keyDb = null;
    }

    public boolean exists() {
        return keyFile.exists();
    }

    public boolean locked() {
        return keyDb == null;
    }

    public void select(Key key) {
        selectedKey.setValue(key);
    }

    public LiveData<Key> getSelectedKey() {
        return selectedKey;
    }

    public void select(Note note) {
        selectedNote.setValue(note);
    }

    public LiveData<Note> getSelectedNote() {
        return selectedNote;
    }

    public void unlock(char[] password) {
        try {
            keyDb = new KeyDb(password, keyFile);
        } catch (KeyDbException exception) {
            exception.printStackTrace();
        }
    }
}
