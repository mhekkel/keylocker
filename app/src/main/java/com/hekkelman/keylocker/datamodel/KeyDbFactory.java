package com.hekkelman.keylocker.datamodel;

import java.io.File;

public class KeyDbFactory {

    private final File mFilesDir;

    public KeyDbFactory(File mFilesDir) {
        this.mFilesDir = mFilesDir;
    }

    public static KeyDb createFromFile(File file) {
        return null;
    }

    public KeyLockerFile initialize(String password) throws KeyDbException {
        File file = new File(mFilesDir, KeyLockerFile.KEY_DB_NAME);
        if (file.exists())
            file.delete();

        KeyLockerFile keyDb = new KeyLockerFile(file, password);
        keyDb.write();
        return keyDb;
    }

    public KeyLockerFile create(String password) {
        File keyFile = new File(mFilesDir, KeyLockerFile.KEY_DB_NAME);

        try {
            return new KeyLockerFile(keyFile, password);
        } catch (KeyDbException e) {
            return null;
        }
    }

    public boolean exists() {
        return new File(mFilesDir, KeyLockerFile.KEY_DB_NAME).exists();
    }

    public void reset() {
        // TODO implement
    }
}
