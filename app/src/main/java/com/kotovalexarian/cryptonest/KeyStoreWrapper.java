package com.kotovalexarian.cryptonest;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Base64;
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

    private final Context context;
    private final KeyStore keyStore;

    private final ArrayList<String> aliases = new ArrayList<>();

    public Runnable onRefresh;

    public KeyStoreWrapper(final Context context) throws OwnException {
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
        try {
            if (!keyStore.containsAlias(alias)) throw new OwnException("Alias doesn't exist");

            keyStore.deleteEntry(alias);
        } catch (KeyStoreException e) {
            throw new OwnException("Can not delete alias", e);
        }

        refresh();
    }

    public String encrypt(final String alias, final String plainText) throws OwnException {
        if (plainText.isEmpty()) throw new OwnException("Empty plain text");

        KeyStore.PrivateKeyEntry privateKeyEntry = this.privateKeyEntry(alias);

        return plainText;
    }

    public String decrypt(final String alias, final String cypherText) throws OwnException {
        if (cypherText.isEmpty()) throw new OwnException("Empty cypher text");

        KeyStore.PrivateKeyEntry privateKeyEntry = this.privateKeyEntry(alias);

        return cypherText;
    }

    public String sign(final String alias, final String textString) throws OwnException {
        try {
            if (textString.isEmpty()) throw new OwnException("Empty text");

            final KeyStore.PrivateKeyEntry privateKeyEntry = this.privateKeyEntry(alias);

            final Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKeyEntry.getPrivateKey());
            signature.update(textString.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new OwnException("Can not sign", e);
        }
    }

    public boolean verify(final String alias, final String textString, final String signatureString)
            throws OwnException
    {
        try {
            if (textString.isEmpty()) throw new OwnException("Empty text");
            if (signatureString.isEmpty()) throw new OwnException("Empty signature");

            final KeyStore.PrivateKeyEntry privateKeyEntry = this.privateKeyEntry(alias);

            final Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(privateKeyEntry.getCertificate());
            signature.update(textString.getBytes(StandardCharsets.UTF_8));

            return signature.verify(Base64.getDecoder().decode(signatureString));
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new OwnException("Can not verify", e);
        }
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

    private KeyStore.PrivateKeyEntry privateKeyEntry(final String alias) throws OwnException {
        try {
            if (!keyStore.containsAlias(alias)) throw new OwnException("Alias doesn't exist");

            KeyStore.Entry entry = keyStore.getEntry(alias, null);

            if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
                throw new OwnException("Is not a private key");
            }

            return (KeyStore.PrivateKeyEntry)entry;
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException e) {
            throw new OwnException("Can not obtain private key", e);
        }
    }
}
