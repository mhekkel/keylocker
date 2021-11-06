package com.hekkelman.keylocker.datamodel;

import android.app.backup.BackupManager;
import android.content.Context;

import com.hekkelman.keylocker.R;

import java.io.File;
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
    private final Context context;
    private byte[] passwordHash;
    private final byte[] passwordKey;

    public static boolean exists() {
        File file = new File(mFilesDir, KEY_DB_NAME);
        return file.exists();
    }

    public static KeyLockerFile initialize(Context context, String password) throws KeyDbException {
        File file = new File(mFilesDir, KEY_DB_NAME);
        if (file.exists())
            //noinspection ResultOfMethodCallIgnored
            file.delete();

        KeyLockerFile keyDb = new KeyLockerFile(file, context, password);
        keyDb.write();
        return keyDb;
    }

    public static KeyLockerFile create(Context context, String password) throws KeyDbException {
        File keyFile = new File(mFilesDir, KEY_DB_NAME);
        return new KeyLockerFile(keyFile, context, password);
    }

    public KeyLockerFile(File file, Context context, String password) throws KeyDbException {
        super(file, password.toCharArray());
        this.context = context;

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

    @Override
    protected void write() throws KeyDbException {
        super.write();

        BackupManager backupManager = new BackupManager(context);
        backupManager.dataChanged();
    }

    public KeyNote.Key createWebDAVKey(String id, String name) {
        return new KeyNote.Key(id, name, "", "", "");
    }
}
