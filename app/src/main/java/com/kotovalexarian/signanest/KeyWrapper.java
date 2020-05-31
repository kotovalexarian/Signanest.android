package com.kotovalexarian.signanest;

import java.security.KeyStore;
import java.security.KeyStoreException;

public class KeyWrapper {
    private final KeyStore keyStore;
    private final String alias;

    private boolean deleted = false;

    public KeyWrapper(final KeyStore keyStore, final String alias)
            throws KeyStoreWrapper.OwnException
    {
        this.keyStore = keyStore;
        this.alias    = alias;

        ensureExists();
    }

    public void delete() throws KeyStoreWrapper.OwnException {
        ensureExists();

        try {
            keyStore.deleteEntry(alias);
        } catch (KeyStoreException e) {
            throw new KeyStoreWrapper.OwnException("Can not delete alias", e);
        }
    }

    private void ensureExists() throws KeyStoreWrapper.OwnException {
        if (deleted) {
            throw new KeyStoreWrapper.OwnException("Alias was deleted");
        }

        try {
            if (!keyStore.containsAlias(alias)) {
                throw new KeyStoreWrapper.OwnException("Alias doesn't exist");
            }
        } catch (KeyStoreException e) {
            throw new KeyStoreWrapper.OwnException("Key store doesn't work", e);
        }
    }
}
