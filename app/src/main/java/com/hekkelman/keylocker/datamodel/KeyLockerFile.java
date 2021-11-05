package com.hekkelman.keylocker.datamodel;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class KeyLockerFile extends KeyDb {

    public static final String KEY_DB_NAME = "keylockerfile.txt";
    private static final int PRIVATE_KEY_HASH_SIZE = 16;
    public static File mFilesDir;
    private byte[] passwordHash;
    private final byte[] passwordKey;

    public static boolean exists() {
        File file = new File(mFilesDir, KEY_DB_NAME);
        return file.exists();
    }

    public static KeyLockerFile initialize(String password) throws KeyDbException {
        File file = new File(mFilesDir, KEY_DB_NAME);
        if (file.exists())
            file.delete();

        KeyLockerFile keyDb = new KeyLockerFile(file, password);
        keyDb.write();
        return keyDb;
    }

    public static KeyLockerFile create(String password) {
        File keyFile = new File(mFilesDir, KEY_DB_NAME);

        try {
            return new KeyLockerFile(keyFile, password);
        } catch (KeyDbException e) {
            return null;
        }
    }

    public KeyLockerFile(File file, String password) throws KeyDbException {
        super(file, password.toCharArray());

        SecureRandom rng = new SecureRandom();
        passwordKey = new byte[PRIVATE_KEY_HASH_SIZE];
        rng.nextBytes(passwordKey);

        try {
            passwordHash = hmac(passwordKey, password.getBytes());
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new KeyDbException.KeyDbRuntimeException(e);
        }
    }

    public boolean checkPassword(String password) {
        boolean result = false;
        try {
            byte[] test = hmac(passwordKey, password.getBytes());
            result = Arrays.equals(test, passwordHash);
        } catch (NoSuchAlgorithmException | InvalidKeyException ignored) {
        }
        return result;
    }

    byte[] hmac(byte[] key, byte[] message) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(message);
    }

    public void changePassword(String password) throws KeyDbException {
        super.changePassword(password);
        try {
            passwordHash = hmac(passwordKey, password.getBytes());
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new KeyDbException.KeyDbRuntimeException(e);
        }
    }
}
