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
    private final KeyDb2 keyDb;
    private final Settings settings;
    private final MutableLiveData<Key> selectedKey = new MutableLiveData<>();
    private final MutableLiveData<Note> selectedNote = new MutableLiveData<>();

    public KeyDbModel(@NonNull Application application) {
        super(application);
        settings = new Settings(application);

        File keyFile = new File(getApplication().getFilesDir(), KeyDb.KEY_DB_NAME);
        keyDb = new KeyDb2(keyFile);
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
}
