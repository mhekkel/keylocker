package com.hekkelman.keylocker.datamodel;

import android.os.Build;

/**
 * Created by maarten on 24-11-15.
 */
public class KeyDbRuntimeException extends KeyDbException {
    public KeyDbRuntimeException(Exception ex) {
        super();
        addSuppressed(ex);
    }
}
