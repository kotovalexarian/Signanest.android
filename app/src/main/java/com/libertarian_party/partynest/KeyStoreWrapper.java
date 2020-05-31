package com.libertarian_party.partynest;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;

import javax.security.auth.x500.X500Principal;

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
    private final X500Principal x500Principal =
            new X500Principal("CN=libertarian-party.com");

    private final Context context;
    private final KeyStore keyStore;

    private final ArrayList<String> aliases = new ArrayList<>();

    public Runnable onRefresh;

    public KeyStoreWrapper(Context context) throws OwnException {
        this.context = context;

        try {
            keyStore = KeyStore.getInstance(keyStoreProvider);
            keyStore.load(null);
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new OwnException("Can not initialize key store", e);
        }

        refresh();
    }

    public int getAliasCount() {
        return aliases.size();
    }

    public String getAlias(int position) throws IndexOutOfBoundsException {
        return aliases.get(position);
    }

    private void refresh() throws OwnException {
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

    public void create(String alias) throws OwnException, IllegalArgumentException
    {
        try {
            if (keyStore.containsAlias(alias)) throw new OwnException("Alias already exists");

            KeyGenParameterSpec keyGenParameterSpec =
                    new KeyGenParameterSpec.Builder(
                            alias,
                            KeyProperties.PURPOSE_ENCRYPT |
                                    KeyProperties.PURPOSE_DECRYPT |
                                    KeyProperties.PURPOSE_SIGN |
                                    KeyProperties.PURPOSE_VERIFY |
                                    KeyProperties.PURPOSE_WRAP_KEY)
                            .setAttestationChallenge(null)
                            .setCertificateSerialNumber(BigInteger.ONE)
                            .setCertificateSubject(x500Principal)
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

            KeyPairGenerator keyPairGenerator =
                    KeyPairGenerator.getInstance("RSA", keyStoreProvider);
            keyPairGenerator.initialize(keyGenParameterSpec);

            KeyPair keyPair = keyPairGenerator.generateKeyPair();
        } catch (KeyStoreException | NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            throw new OwnException("Can not generate key", e);
        }

        refresh();
    }

    public void delete(String alias) throws OwnException {
        try {
            if (!keyStore.containsAlias(alias)) throw new OwnException("Alias doesn't exist");

            keyStore.deleteEntry(alias);
        } catch (KeyStoreException e) {
            throw new OwnException("Can not delete alias", e);
        }

        refresh();
    }

    public String encrypt(String alias, String plainText) throws OwnException {
        try {
            if (!keyStore.containsAlias(alias)) throw new OwnException("Alias doesn't exist");
            if (plainText.isEmpty()) throw new OwnException("Empty plain text");

            KeyStore.PrivateKeyEntry privateKeyEntry =
                    (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);

            return plainText;
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException e) {
            throw new OwnException("Can not encrypt", e);
        }
    }

    public String decrypt(String alias, String cypherText) throws OwnException {
        try {
            if (!keyStore.containsAlias(alias)) throw new OwnException("Alias doesn't exist");
            if (cypherText.isEmpty()) throw new OwnException("Empty cypher text");

            KeyStore.PrivateKeyEntry privateKeyEntry =
                    (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);

            return cypherText;
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException e) {
            throw new OwnException("Can not decrypt", e);
        }
    }
}
