package com.kotovalexarian.signanest;

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
    public static final class OwnException extends GeneralSecurityException {
        public OwnException(String errorMessage) {
            super(errorMessage);
        }

        public OwnException(String errorMessage, Throwable err) {
            super(errorMessage, err);
        }
    }

    private final String keyStoreProvider = "AndroidKeyStore";

    private final KeyStore keyStore;

    private final ArrayList<String> aliases = new ArrayList<>();

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

    public int getAliasCount() {
        return aliases.size();
    }

    public String getAlias(final int position) throws IndexOutOfBoundsException {
        return aliases.get(position);
    }

    public void refresh() throws OwnException {
        aliases.clear();

        try {
            Enumeration<String> enumeration = keyStore.aliases();
            while (enumeration.hasMoreElements())
                aliases.add(enumeration.nextElement());
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

    public void delete(final String alias) throws OwnException {
        KeyWrapper keyWrapper = new KeyWrapper(this, keyStore, alias);
        keyWrapper.delete();
        refresh();
    }

    public String encrypt(final String alias, final String plainText) throws OwnException {
        KeyWrapper keyWrapper = new KeyWrapper(this, keyStore, alias);
        return keyWrapper.encrypt(plainText);
    }

    public String decrypt(final String alias, final String cipherText) throws OwnException {
        KeyWrapper keyWrapper = new KeyWrapper(this, keyStore, alias);
        return keyWrapper.decrypt(cipherText);
    }

    public String sign(final String alias, final String textString) throws OwnException {
        KeyWrapper keyWrapper = new KeyWrapper(this, keyStore, alias);
        return keyWrapper.sign(textString);
    }

    public boolean verify(final String alias, final String textString, final String signatureString)
            throws OwnException
    {
        KeyWrapper keyWrapper = new KeyWrapper(this, keyStore, alias);
        return keyWrapper.verify(textString, signatureString);
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
