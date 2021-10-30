package com.hekkelman.keylocker.datamodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hekkelman.keylocker.utilities.Settings;

import java.io.File;
import java.util.List;

public class KeyDbModel extends AndroidViewModel {
    private KeyDbDao keyDbDao;
    private final MutableLiveData<Key> selectedKey = new MutableLiveData<>();
    private final MutableLiveData<Note> selectedNote = new MutableLiveData<>();
    private final MutableLiveData<List<Key>> availableKeys = new MutableLiveData<>();
    private boolean locked = true;

    public KeyDbModel(@NonNull Application application) {
        super(application);
    }

    public boolean unlock(char[] password) {
        try {
            keyDbDao = KeyDbFactory.createKeyLocker(getApplication(), password);
            locked = false;
        } catch (KeyDbException exception) {
            exception.printStackTrace();
        }
        return !locked;
    }

    public KeyDbDao getKeyDb() {
        return keyDbDao;
    }

    public boolean locked() {
        return locked;
    }

    public void select(Key key) {
        selectedKey.setValue(key);
    }

    public LiveData<Key> getSelectedKey() {
        return selectedKey;
    }

    public LiveData<List<Key>> getAvailableKeys() { return availableKeys; }

    public void select(Note note) {
        selectedNote.setValue(note);
    }

    public LiveData<Note> getSelectedNote() {
        return selectedNote;
    }

    public void loadKeys() {
        availableKeys.setValue(keyDbDao.getAllKeys());
    }

    public void lock() {
        this.locked = true;
    }
}
