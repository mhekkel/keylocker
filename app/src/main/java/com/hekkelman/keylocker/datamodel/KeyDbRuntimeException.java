package com.hekkelman.keylocker.datamodel;

import androidx.annotation.Nullable;

/**
 * Created by maarten on 24-11-15.
 */
public class KeyDbRuntimeException extends KeyDbException {
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
