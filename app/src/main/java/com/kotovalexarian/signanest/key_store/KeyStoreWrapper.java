package com.kotovalexarian.signanest.key_store;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Enumeration;

public final class KeyStoreWrapper {
    private final String keyStoreProvider = "AndroidKeyStore";

    private final KeyStore keyStore;

    private final ArrayList<KeyWrapper> keyWrappers = new ArrayList<>();

    private Runnable onRefresh = null;

    public KeyStoreWrapper() throws OwnException {
        try {
            keyStore = KeyStore.getInstance(keyStoreProvider);
            keyStore.load(null);
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new OwnException("Can not initialize key store", e);
        }

        refresh();
    }

    public void onRefresh(final Runnable onRefresh) throws OwnException {
        if (this.onRefresh != null) throw new OwnException("Refresh callback already set");

        this.onRefresh = onRefresh;
    }

    public int getCount() { return keyWrappers.size(); }

    public KeyWrapper getByAlias(final String alias) throws OwnException {
        for (KeyWrapper keyWrapper : keyWrappers) {
            if (keyWrapper.getAlias().equals(alias)) {
                return keyWrapper;
            }
        }

        throw new OwnException("Alias doesn't exist");
    }

    public KeyWrapper getByPosition(final int position) throws IndexOutOfBoundsException {
        return keyWrappers.get(position);
    }

    public void refresh() throws OwnException {
        keyWrappers.clear();

        try {
            Enumeration<String> enumeration = keyStore.aliases();
            while (enumeration.hasMoreElements()) {
                final String alias = enumeration.nextElement();
                keyWrappers.add(new KeyWrapper(this, keyStore, alias));
            }
        } catch (KeyStoreException e) {
            throw new OwnException("Can not fetch aliases", e);
        }

        if (onRefresh != null) {
            onRefresh.run();
        }
    }

    public void create(final String alias) throws OwnException, IllegalArgumentException
    {
        try {
            if (alias.isEmpty()) throw new OwnException("Empty alias");
            if (keyStore.containsAlias(alias)) throw new OwnException("Alias already exists");

            KeyGenParameterSpec keyGenParameterSpec = this.keyGenParameterSpec(alias);

            KeyPairGenerator keyPairGenerator =
                    KeyPairGenerator.getInstance("RSA", keyStoreProvider);
            keyPairGenerator.initialize(keyGenParameterSpec);

            KeyPair keyPair = keyPairGenerator.generateKeyPair();
        } catch (KeyStoreException | NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            throw new OwnException("Can not generate key", e);
        }

        refresh();
    }

    private KeyGenParameterSpec keyGenParameterSpec(final String alias) {
        return new KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT |
                        KeyProperties.PURPOSE_DECRYPT |
                        KeyProperties.PURPOSE_SIGN |
                        KeyProperties.PURPOSE_VERIFY |
                        KeyProperties.PURPOSE_WRAP_KEY)
                .setAttestationChallenge(null)
                .setDigests(
                        KeyProperties.DIGEST_SHA224,
                        KeyProperties.DIGEST_SHA256,
                        KeyProperties.DIGEST_SHA384,
                        KeyProperties.DIGEST_SHA512)
                .setEncryptionPaddings(
                        KeyProperties.ENCRYPTION_PADDING_RSA_OAEP,
                        KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .setIsStrongBoxBacked(false) // Because it isn't available
                .setKeySize(2048)
                .setRandomizedEncryptionRequired(true)
                .setSignaturePaddings(
                        KeyProperties.SIGNATURE_PADDING_RSA_PKCS1,
                        KeyProperties.SIGNATURE_PADDING_RSA_PSS)
                .setUnlockedDeviceRequired(false)
                .setUserAuthenticationRequired(false)
                .setUserConfirmationRequired(false)
                .setUserPresenceRequired(false)
                .build();

    }
}
