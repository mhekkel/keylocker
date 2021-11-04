package com.hekkelman.keylocker.datamodel;

import com.hekkelman.keylocker.xmlenc.EncryptedData;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class KeyDb implements KeyDbDao {

    // fields
    private final File file;
    private final Object lock = new Object();
    private char[] password;
    private KeyChain keyChain;
    protected boolean backup = false;

    // constructor, regular key db file
    public KeyDb(File file, char[] password) throws KeyDbException {
        this.file = file;
        this.password = password;

        read();
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

    @Override
    public KeyNote.Key createKey() {
        return new KeyNote.Key();
    }

    @Override
    public Optional<KeyNote.Key> getKey(String id) {
        synchronized (lock) {
            return Optional.ofNullable(keyChain.getKeyByID(id));
        }
    }

    @Override
    public List<KeyNote.Key> getAllKeys() {
        synchronized (lock) {
            return keyChain.getKeys()
                    .stream()
                    .filter(k -> !k.isDeleted())
                    .sorted((lhs, rhs) -> lhs.getName().compareToIgnoreCase(rhs.getName()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void saveKey(KeyNote.Key key) throws KeyDbException {
        synchronized (lock) {
            keyChain.addKey(key);
            write();
        }
    }

    @Override
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

    @Override
    public void deleteKey(KeyNote.Key key) throws KeyDbException {
        synchronized (lock) {
            key.setDeleted(true);
            write();
        }
    }

    @Override
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

    @Override
    public KeyNote.Note createNote() {
        return new KeyNote.Note();
    }

    @Override
    public Optional<KeyNote.Note> getNote(String id) {
        synchronized (lock) {
            return Optional.ofNullable(keyChain.getNoteByID(id));
        }
    }

    @Override
    public List<KeyNote.Note> getAllNotes() {
        synchronized (lock) {
            return keyChain.getNotes()
                    .stream()
                    .filter(k -> !k.isDeleted())
                    .sorted((lhs, rhs) -> lhs.getName().compareToIgnoreCase(rhs.getName()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void saveNote(KeyNote.Note note) throws KeyDbException {
        synchronized (lock) {
            keyChain.addNote(note);
            write();
        }
    }

    @Override
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

    @Override
    public void deleteNote(KeyNote.Note note) throws KeyDbException {
        synchronized (lock) {
            note.setDeleted(true);
            write();
        }
    }

    @Override
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

//    public static void synchronize(Context context, Uri backupDir, char[] password) throws KeyDbException {
//        synchronized (keyDbLock) {
//            if (password == null)
//                password = sInstance.password;
//
//            DocumentFile dir = DocumentFile.fromTreeUri(context, backupDir);
//            if (dir == null)
//                throw new MissingFileException();
//
//            KeyDb backup = new KeyDb(password);
//
//            DocumentFile file = dir.findFile(KEY_DB_NAME);
//            if (file != null) {
//                try (InputStream is = context.getContentResolver().openInputStream(file.getUri())) {
//                    backup.read(is);
//                } catch (IOException e) {
//                    throw new KeyDbRuntimeException(e);
//                }
//            }
//
//            boolean changed = sInstance.synchronize(backup);
//            sInstance.write();
//
//            if (changed) {
//                if (file == null)
//                    file = dir.createFile("application/x-keylocker", KEY_DB_NAME);
//                if (file == null)
//                    throw new MissingFileException();
//
//                try (OutputStream os = context.getContentResolver().openOutputStream(file.getUri())) {
//                    backup.write(os);
//                } catch (IOException e) {
//                    throw new KeyDbRuntimeException(e);
//                }
//            }
//        }
//    }

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

            EncryptedData.encrypt(this.password, new ByteArrayInputStream(os.toByteArray()), output, this.backup);
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

//    private void synchronize(File file) throws KeyDbException {
//        KeyDb db = new KeyDb(this.password, file);
//        if (synchronize(db))
//            db.write();
//    }
//
//    // synchronisation
//
//    private void synchronize(File file, char[] password) throws KeyDbException {
//        KeyDb db = new KeyDb(password, file);
//        if (synchronize(db))
//            db.write();
//    }
//
//    private boolean synchronize(InputStream file) throws KeyDbException {
//        KeyDb db = new KeyDb(this.password, null);
//        db.read(file);
//        return synchronize(db);
//    }
//
//    private boolean synchronize(InputStream file, char[] password) throws KeyDbException {
//        KeyDb db = new KeyDb(password, null);
//        db.read(file);
//        return synchronize(db);
//    }
//
//    private boolean synchronize(KeyDb db) throws KeyDbException {
//        boolean result = this.keyChain.synchronize(db.keyChain);
//        write();
//        return result;
//    }
//
//	public KeyDb undeleteAll() throws KeyDbException {
//		for (Key k : keyChain.getKeys()) {
//			if (k.isDeleted()) {
//				k.setDeleted(false);
//			}
//		}
//
//		write();
//
//		return this;
//	}
}
