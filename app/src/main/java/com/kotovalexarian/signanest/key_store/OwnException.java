package com.kotovalexarian.signanest.key_store;

import java.security.GeneralSecurityException;

public final class OwnException extends GeneralSecurityException {
    public OwnException(String errorMessage) {
        super(errorMessage);
    }

    public OwnException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
