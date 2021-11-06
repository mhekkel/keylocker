package com.hekkelman.keylocker.datamodel;

import androidx.annotation.Nullable;

/**
 * Created by maarten on 24-11-15.
 */
public class KeyDbException extends Exception {

    public KeyDbException() {
        super();
    }

    public KeyDbException(String message) {
        super(message);
    }

    /**
     * Created by maarten on 24-11-15.
     */
    public final static class KeyDbRuntimeException extends KeyDbException {
        public KeyDbRuntimeException(Exception ex) {
            super();
            addSuppressed(ex);
        }

        @Nullable
        @Override
        public String getMessage() {
            return getSuppressed()[0].getMessage();
        }
    }

    public final static class InvalidKeyDbFileException extends KeyDbException {
        public InvalidKeyDbFileException(Exception ex) {
            super();
            addSuppressed(ex);
        }

        @Nullable
        @Override
        public String getMessage() {
            return getSuppressed()[0].getMessage();
        }
    }

    /**
     * Created by maarten on 24-11-15.
     */
    public final static class InvalidPasswordException extends KeyDbException {
        public InvalidPasswordException() {
        }
    }

    public final static class CouldNotCreateFileException extends KeyDbException {
        @Nullable
        @Override
        public String getMessage() {
            return "Could not create file";
        }
    }
}
