package com.hekkelman.keylocker.datamodel;


import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;
import java.util.Vector;

@Root
public class KeyChain {
    @ElementList(type = KeyNote.Key.class)
    private List<KeyNote.Key> keys;

    @ElementList(type = KeyNote.Note.class)
    private List<KeyNote.Note> notes;

    public KeyChain() {
        this.keys = new Vector<>();
        this.notes = new Vector<>();
    }

    public List<KeyNote.Key> getKeys() {
        return keys;
    }

    public void addKey(KeyNote.Key key) {
        if (!this.keys.contains(key)) this.keys.add(key);
    }

    public List<KeyNote.Note> getNotes() {
        return notes;
    }

    public void addNote(KeyNote.Note note) {
        if (!this.notes.contains(note)) this.notes.add(note);
    }

    // more accessors
    KeyNote.Key getKeyByID(String id) {
        for (KeyNote.Key k : this.keys)
            if (k.getId().equals(id))
                return k;
        return null;
    }

    KeyNote.Note getNoteByID(String id) {
        for (KeyNote.Note n : this.notes) {
            if (n.getId().equals(id))
                return n;
        }
        return null;
    }

    public boolean synchronize(KeyChain kc) {
        boolean changed = false;

        for (KeyNote.Key key : this.keys) {
            KeyNote.Key kcKey = kc.getKeyByID(key.getId());
            if (kcKey == null) {
                kc.keys.add(new KeyNote.Key(key));
                changed = true;
            } else if (kcKey.synchronize(key) != 0)
                changed = true;
        }

        for (KeyNote.Key key : kc.keys) {
            KeyNote.Key tKey = this.getKeyByID(key.getId());
            if (tKey == null)
                this.keys.add(new KeyNote.Key(key));
            else
                tKey.synchronize(key);
        }

        for (KeyNote.Note note : this.notes) {
            KeyNote.Note kcNote = kc.getNoteByID(note.getId());
            if (kcNote == null) {
                kc.notes.add(new KeyNote.Note(note));
                changed = true;
            } else if (kcNote.synchronize(note) != 0)
                changed = true;
        }

        for (KeyNote.Note Note : kc.notes) {
            KeyNote.Note tNote = this.getNoteByID(Note.getId());
            if (tNote == null)
                this.notes.add(new KeyNote.Note(Note));
            else
                tNote.synchronize(Note);
        }

        return changed;
    }

    public void purge() {
        this.keys.removeIf(KeyNote.Key::isDeleted);
        this.notes.removeIf(KeyNote.Note::isDeleted);
    }
}
