package com.hekkelman.keylocker.datamodel;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import com.hekkelman.keylocker.xmlenc.EncryptedData;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class KeyDb {

    // fields
    private final File file;
    private final Object lock = new Object();
    private char[] password;
    private KeyChain keyChain;

    // constructor for empty keydb
    protected KeyDb(char[] password) {
        this.file = null;
        this.password = password;
        this.keyChain = new KeyChain();
    }

    // constructor, regular key db file
    public KeyDb(File file, char[] password) throws KeyDbException {
        this.file = file;
        this.password = password;

        if (file.exists())
            read();
        else
            keyChain = new KeyChain();
    }

    public void changePassword(String password) throws KeyDbException {
        synchronized (lock) {
            char[] savedPassword = this.password;
            this.password = password.toCharArray();
            try {
                write();
            } catch (Exception e) {
                this.password = savedPassword;
                throw e;
            }
        }
    }

    public KeyNote.Key createKey() {
        return new KeyNote.Key();
    }

    public Optional<KeyNote.Key> getKey(String id) {
        synchronized (lock) {
            return Optional.ofNullable(keyChain.getKeyByID(id));
        }
    }

    public List<KeyNote.Key> getAllKeys() {
        synchronized (lock) {
            return keyChain.getKeys()
                    .stream()
                    .filter(k -> !k.isDeleted())
                    .sorted((lhs, rhs) -> lhs.getName().compareToIgnoreCase(rhs.getName()))
                    .collect(Collectors.toList());
        }
    }

    public void updateKey(KeyNote.Key key, String name, String user, String password, String url) throws KeyDbException {
        synchronized (lock) {
            KeyNote.Key savedCopy = new KeyNote.Key(key);

            try {
                key.setName(name);
                key.setUser(user);
                key.setPassword(password);
                key.setUrl(url);

                keyChain.addKey(key);
                write();
            } catch (KeyDbException exception) {

                // restore the key...
                key.setName(savedCopy.getName());
                key.setUser(savedCopy.getUser());
                key.setPassword(savedCopy.getPassword());
                key.setUrl(savedCopy.getUrl());

                throw exception;
            }
        }
    }

    public void deleteKey(KeyNote.Key key) throws KeyDbException {
        synchronized (lock) {
            key.setDeleted(true);
            write();
        }
    }

    public void undoDeleteKey(String keyID) {
        synchronized (lock) {
            KeyNote.Key key = keyChain.getKeyByID(keyID);
            if (key != null) {
                key.setDeleted(false);
                try {
                    write();
                } catch (KeyDbException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Optional<KeyNote.Note> getNote(String id) {
        synchronized (lock) {
            return Optional.ofNullable(keyChain.getNoteByID(id));
        }
    }

    public List<KeyNote.Note> getAllNotes() {
        synchronized (lock) {
            return keyChain.getNotes()
                    .stream()
                    .filter(k -> !k.isDeleted())
                    .sorted((lhs, rhs) -> lhs.getName().compareToIgnoreCase(rhs.getName()))
                    .collect(Collectors.toList());
        }
    }

    public void updateNote(KeyNote.Note note, String name, String text) throws KeyDbException {
        synchronized (lock) {
            KeyNote.Note savedCopy = new KeyNote.Note(note);

            try {
                note.setName(name);
                note.setText(text);

                keyChain.addNote(note);
                write();
            } catch (KeyDbException exception) {
                // restore the note...
                note.setName(savedCopy.getName());
                note.setText(savedCopy.getText());

                throw exception;
            }
        }
    }

    public void deleteNote(KeyNote.Note note) throws KeyDbException {
        synchronized (lock) {
            note.setDeleted(true);
            write();
        }
    }

    public void undoDeleteNote(String noteID) {
        synchronized (lock) {
            KeyNote.Note note = keyChain.getNoteByID(noteID);
            if (note != null) {
                note.setDeleted(true);
                try {
                    write();
                } catch (KeyDbException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void synchronize(Context context, Uri backupDir, String password) throws KeyDbException {
        synchronized (lock) {
            char[] pw = password != null ? password.toCharArray() : this.password;

            DocumentFile dir = DocumentFile.fromTreeUri(context, backupDir);
            if (dir == null)
                throw new KeyDbException.MissingFileException();

            KeyDb backup = new KeyDb(pw);

            DocumentFile file = dir.findFile(KeyLockerFile.KEY_DB_NAME);
            if (file != null) {
                try (InputStream is = context.getContentResolver().openInputStream(file.getUri())) {
                    backup.read(is);
                } catch (IOException e) {
                    throw new KeyDbException.KeyDbRuntimeException(e);
                }
            }

            boolean changed = synchronize(backup);
            write();

            if (changed) {
                if (file == null)
                    file = dir.createFile("application/x-keylocker", KeyLockerFile.KEY_DB_NAME);
                if (file == null)
                    throw new KeyDbException.MissingFileException();

                try (OutputStream os = context.getContentResolver().openOutputStream(file.getUri())) {
                    backup.write(os);
                } catch (IOException e) {
                    throw new KeyDbException.KeyDbRuntimeException(e);
                }
            }
        }
    }

    private void read() throws KeyDbException {
        try {
            read(new FileInputStream(this.file));
        } catch (FileNotFoundException e) {
            throw new KeyDbException.MissingFileException();
        }
    }

    protected void read(InputStream input) throws KeyDbException {
        try (InputStream is = EncryptedData.decrypt(this.password, input)) {
            Serializer serializer = new Persister();
            keyChain = serializer.read(KeyChain.class, is);
        } catch (Exception e) {
            throw new KeyDbException.InvalidPasswordException();
        }
    }

    protected void write() throws KeyDbException {
        try {
            write(new FileOutputStream(this.file));
        } catch (FileNotFoundException e) {
            throw new KeyDbException.KeyDbRuntimeException(e);
        }
    }

    private void write(OutputStream output) throws KeyDbException {
        try {
            // intermediate storage of unencrypted data
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            Serializer serializer = new Persister();
            serializer.write(keyChain, os);

            EncryptedData.encrypt(this.password, new ByteArrayInputStream(os.toByteArray()), output);
        } catch (Exception e) {
            throw new KeyDbException.KeyDbRuntimeException(e);
        }
    }

    public void delete(KeyNote keyNote) throws KeyDbException {
        if (keyNote instanceof KeyNote.Key)
            deleteKey((KeyNote.Key) keyNote);
        else if (keyNote instanceof KeyNote.Note)
            deleteNote((KeyNote.Note) keyNote);
    }

    public void undoDelete(KeyNote keyNote) {
        if (keyNote instanceof KeyNote.Key)
            undoDeleteKey(keyNote.getId());
        else if (keyNote instanceof KeyNote.Note)
            undoDeleteNote(keyNote.getId());
    }
    private boolean synchronize(KeyDb db) throws KeyDbException {
        boolean result = this.keyChain.synchronize(db.keyChain);
        write();
        return result;
    }
}
