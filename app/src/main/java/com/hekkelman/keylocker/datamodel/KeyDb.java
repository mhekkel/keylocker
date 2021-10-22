package com.hekkelman.keylocker.datamodel;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.hekkelman.keylocker.tasks.SaveKeyTask;
import com.hekkelman.keylocker.utilities.Settings;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

public class KeyDb {

    public static final String KEY_DB_NAME = "keylockerfile.txt";
    public static final SafetyToken safetyToken = new SafetyToken();
    private static final Object keyDbLock = new Object();
    private static KeyDb sInstance;
    private final File file;
    private final boolean backup;

    // fields
    private char[] password;
    private KeyChain keyChain;

    // constructor, regular key db file
    private KeyDb(char[] password, File file) throws KeyDbException {
        this.password = password;
        this.file = file;
        this.backup = false;

        if (file != null && file.exists())
            read();
        else
            keyChain = new KeyChain();
    }

    // constructor backup key db file
    public KeyDb(char[] password) throws KeyDbException {
        this.password = password;
        this.file = null;
        this.backup = true;

        keyChain = new KeyChain();
    }

    public static void init(Settings settings) {
        synchronized (keyDbLock) {
            ProcessLifecycleOwner.get().getLifecycle().addObserver(new DefaultLifecycleObserver() {
                @Override
                public void onStop(@NonNull LifecycleOwner owner) {
                    if (settings.getRelockOnBackground())
                        KeyDb.sInstance = null;
                }
            });
        }
    }

    public static void onReceivedScreenOff() {
        synchronized (keyDbLock) {
            KeyDb.sInstance = null;
        }
    }

    // Public interface
    public static boolean isUnlocked() {
        return sInstance != null;
    }

    public static void initialize(char[] password, File dir) throws KeyDbException {
        synchronized (keyDbLock) {
            File file = new File(dir, KEY_DB_NAME);
            if (file.exists())
                file.delete();

            sInstance = new KeyDb(password, file);
            sInstance.write();
        }
    }

    public static boolean isValidPassword(char[] plainPassword, File keyDbFile) {
        boolean result = false;

        synchronized (keyDbLock) {
            try {
                sInstance = new KeyDb(plainPassword, keyDbFile);
                sInstance.read();
                result = true;
            } catch (KeyDbException e) {
                sInstance = null;
            }
        }

        return result;
    }

    public static void changePassword(char[] password) throws KeyDbException {
        synchronized (keyDbLock) {
            sInstance.password = password;
            sInstance.write();
        }
    }

    public static List<Key> getKeys() {
        List<Key> result = new ArrayList<>();

        synchronized (keyDbLock) {
            for (Key k : sInstance.keyChain.getKeys()) {
                if (k.isDeleted())
                    continue;
                result.add(k);
            }

            Collections.sort(result, new Comparator<Key>() {
                @Override
                public int compare(Key lhs, Key rhs) {
                    return lhs.getName().compareToIgnoreCase(rhs.getName());
                }
            });
        }

        return result;
    }

    public static List<Key> getFilteredKeys(String query) {
        List<Key> result;

        synchronized (keyDbLock) {
            if (sInstance != null) {
                result = sInstance.keyChain.getKeys();
                if (!TextUtils.isEmpty(query))
                    result = result.stream().filter(key -> key.match(query)).collect(Collectors.toList());
            } else
                result = new ArrayList<>();
        }

        return result;
    }

    public static Key getKey(String keyId) {
        synchronized (keyDbLock) {
            return sInstance.keyChain.getKeyByID(keyId);
        }
    }

    public static void setKey(Key key, String name, String user, String password, String url) throws KeyDbException {
        synchronized (keyDbLock) {
            Key savedCopy = new Key(key);

            try {
                key.setName(name, safetyToken);
                key.setUser(user, safetyToken);
                key.setPassword(password, safetyToken);
                key.setUrl(url, safetyToken);

                sInstance.keyChain.addKey(key);
                sInstance.write();
            } catch (KeyDbException exception) {
                // restore the key...
                key.setName(savedCopy.getName(), safetyToken);
                key.setUser(savedCopy.getUser(), safetyToken);
                key.setPassword(savedCopy.getPassword(), safetyToken);
                key.setUrl(savedCopy.getUrl(), safetyToken);

                throw exception;
            }
        }
    }

    public static void deleteKey(Key key) throws KeyDbException {
        synchronized (keyDbLock) {
            key.setDeleted(true);
            sInstance.write();
        }
    }

    // Private interface

    public static void synchronize(Context context, Uri backupDir, char[] password) throws KeyDbException {
        synchronized (keyDbLock) {
            if (password == null)
                password = sInstance.password;

            DocumentFile dir = DocumentFile.fromTreeUri(context, backupDir);
            if (dir == null)
                throw new MissingFileException();

            KeyDb backup = new KeyDb(password);

            DocumentFile file = dir.findFile(KEY_DB_NAME);
            if (file != null) {
                try (InputStream is = context.getContentResolver().openInputStream(file.getUri())) {
                    backup.read(is);
                } catch (IOException e) {
                    throw new KeyDbRuntimeException(e);
                }
            }

            boolean changed = sInstance.synchronize(backup);
            sInstance.write();

            if (changed) {
                if (file == null)
                    file = dir.createFile("application/x-keylocker", KEY_DB_NAME);
                if (file == null)
                    throw new MissingFileException();

                try (OutputStream os = context.getContentResolver().openOutputStream(file.getUri())) {
                    backup.write(os);
                } catch (IOException e) {
                    throw new KeyDbRuntimeException(e);
                }
            }
        }
    }

    private void read() throws KeyDbException {
        try {
            read(new FileInputStream(this.file));
        } catch (FileNotFoundException e) {
            throw new MissingFileException();
        }
    }

    public void read(InputStream input) throws KeyDbException {
        try (InputStream is = EncryptedData.decrypt(this.password, input)) {
            Serializer serializer = new Persister();
            keyChain = serializer.read(KeyChain.class, is);
        } catch (Exception e) {
            throw new InvalidPasswordException();
        }
    }

    private void write() throws KeyDbException {
        try {
            write(new FileOutputStream(this.file));
        } catch (FileNotFoundException e) {
            throw new KeyDbRuntimeException(e);
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
            throw new KeyDbRuntimeException(e);
        }
    }

    private void synchronize(File file) throws KeyDbException {
        KeyDb db = new KeyDb(this.password, file);
        if (synchronize(db))
            db.write();
    }

    // synchronisation

    private void synchronize(File file, char[] password) throws KeyDbException {
        KeyDb db = new KeyDb(password, file);
        if (synchronize(db))
            db.write();
    }

    private boolean synchronize(InputStream file) throws KeyDbException {
        KeyDb db = new KeyDb(this.password, null);
        db.read(file);
        return synchronize(db);
    }

    private boolean synchronize(InputStream file, char[] password) throws KeyDbException {
        KeyDb db = new KeyDb(password, null);
        db.read(file);
        return synchronize(db);
    }

    private boolean synchronize(KeyDb db) throws KeyDbException {
        boolean result = this.keyChain.synchronize(db.keyChain);
        write();
        return result;
    }

    // a 'friend' construct as found in https://stackoverflow.com/questions/182278/is-there-a-way-to-simulate-the-c-friend-concept-in-java
    public static final class SafetyToken {
        private SafetyToken() {
        }
    }

    // accessors

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
