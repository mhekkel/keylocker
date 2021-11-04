package com.hekkelman.keylocker.datamodel;

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
    private byte[] passwordHash;
    private final byte[] passwordKey;

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
