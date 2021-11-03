package com.hekkelman.keylocker.datamodel;

import java.util.List;
import java.util.Optional;

public interface KeyDbDao {
    // key
    Key createKey();
    Optional<Key> getKey(String id);
    List<Key> getAllKeys();
    void saveKey(Key key) throws KeyDbException;
    void updateKey(Key key, String name, String user, String password, String url) throws KeyDbException;
    void deleteKey(Key key) throws KeyDbException;
    void undoDeleteKey(String keyID);

    // note
    Note createNote();
    Optional<Note> getNote(String id);
    List<Note> getAllNotes();
    void saveNote(Note note) throws KeyDbException;
    void updateNote(Note note, String name, String text) throws KeyDbException;
    void deleteNote(Note note) throws KeyDbException;
    void undoDeleteNote(String noteID);
}
