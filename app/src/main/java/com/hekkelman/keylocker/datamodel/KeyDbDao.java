package com.hekkelman.keylocker.datamodel;

import java.util.List;
import java.util.Optional;

public interface KeyDbDao {
    // key
    KeyNote.Key createKey();
    Optional<KeyNote.Key> getKey(String id);
    List<KeyNote.Key> getAllKeys();
    void saveKey(KeyNote.Key key) throws KeyDbException;
    void updateKey(KeyNote.Key key, String name, String user, String password, String url) throws KeyDbException;
    void deleteKey(KeyNote.Key key) throws KeyDbException;
    void undoDeleteKey(String keyID);

    // note
    KeyNote.Note createNote();
    Optional<KeyNote.Note> getNote(String id);
    List<KeyNote.Note> getAllNotes();
    void saveNote(KeyNote.Note note) throws KeyDbException;
    void updateNote(KeyNote.Note note, String name, String text) throws KeyDbException;
    void deleteNote(KeyNote.Note note) throws KeyDbException;
    void undoDeleteNote(String noteID);
}
