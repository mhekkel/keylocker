package com.hekkelman.keylocker.datamodel;

import android.content.Context;

import java.io.File;

public class KeyDbFactory {

    public static final String KEY_DB_NAME = "keylockerfile.txt";

    public static void initialize(Context context, char[] password) throws KeyDbException {
        File file = new File(context.getFilesDir(), KeyDbFactory.KEY_DB_NAME);
        if (file.exists())
            file.delete();

        new KeyDb(file, password).write();
    }


    public static KeyDb createKeyLocker(Context context, char[] password) throws KeyDbException {
        File keyFile = new File(context.getFilesDir(), KeyDbFactory.KEY_DB_NAME);
        return new KeyDb(keyFile, password);
    }

    public static KeyDb createFromFile(File file) {
        return null;
    }

    public static boolean exists(Context context) {
        return new File(context.getFilesDir(), KeyDbFactory.KEY_DB_NAME).exists();
    }
}
