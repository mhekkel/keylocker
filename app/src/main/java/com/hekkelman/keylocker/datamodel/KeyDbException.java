package com.hekkelman.keylocker.datamodel;

import androidx.annotation.Nullable;

/**
 * Created by maarten on 24-11-15.
 */
public class KeyDbException extends Exception {
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

    /**
     * Created by maarten on 24-11-15.
     */
    public final static class MissingFileException extends KeyDbException {
    }
}
