package com.hekkelman.keylocker.datamodel;

import java.util.List;
import java.util.Optional;

public interface KeyDbDao {
    // key
    public Key createKey();
    public Optional<Key> getKey(String id);
    public List<Key> getAllKeys();
    public void saveKey(Key key) throws KeyDbException;
    public void updateKey(Key key, String name, String user, String password, String url) throws KeyDbException;
    public void deleteKey(Key key) throws KeyDbException;

    // note
    public Note createNote();
    public Optional<Note> getNote(String id);
    public List<Note> getAllNotes();
    public void saveNote(Note note) throws KeyDbException;
    public void updateNote(Note note, String name, String text) throws KeyDbException;
    public void deleteNote(Note note) throws KeyDbException;
}
