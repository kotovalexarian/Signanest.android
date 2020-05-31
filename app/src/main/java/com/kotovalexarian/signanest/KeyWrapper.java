package com.kotovalexarian.signanest;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class KeyWrapper {
    private final KeyStoreWrapper keyStoreWrapper;
    private final KeyStore keyStore;
    private final String alias;

    private boolean deleted = false;

    public KeyWrapper(
            final KeyStoreWrapper keyStoreWrapper,
            final KeyStore keyStore,
            final String alias
    )
            throws KeyStoreWrapper.OwnException
    {
        this.keyStoreWrapper = keyStoreWrapper;
        this.keyStore = keyStore;
        this.alias = alias;
    }

    public String getAlias() { return alias; }

    public void ensureExists() throws KeyStoreWrapper.OwnException {
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

    public void delete() throws KeyStoreWrapper.OwnException {
        ensureExists();

        try {
            deleted = true;
            keyStore.deleteEntry(alias);
            keyStoreWrapper.refresh();
        } catch (KeyStoreException e) {
            throw new KeyStoreWrapper.OwnException("Key store failure", e);
        }
    }

    public String encrypt(final String plainText) throws KeyStoreWrapper.OwnException {
        ensureExists();

        try {
            if (plainText.isEmpty()) throw new KeyStoreWrapper.OwnException("Empty plain text");

            final KeyStore.PrivateKeyEntry privateKeyEntry = this.privateKeyEntry();
            final Cipher cipher = this.cipher();
            cipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.getCertificate().getPublicKey());

            return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
        } catch (InvalidKeyException e) {
            throw new KeyStoreWrapper.OwnException("Invalid key", e);
        } catch (BadPaddingException e) {
            throw new KeyStoreWrapper.OwnException("Bad padding", e);
        } catch (IllegalBlockSizeException e) {
            throw new KeyStoreWrapper.OwnException("Illegal block size", e);
        }
    }

    public String decrypt(final String cipherText) throws KeyStoreWrapper.OwnException {
        ensureExists();

        try {
            if (cipherText.isEmpty()) throw new KeyStoreWrapper.OwnException("Empty cipher text");

            final KeyStore.PrivateKeyEntry privateKeyEntry = this.privateKeyEntry();
            final Cipher cipher = this.cipher();
            cipher.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());

            return new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)), StandardCharsets.UTF_8);
        } catch (InvalidKeyException e) {
            throw new KeyStoreWrapper.OwnException("Invalid key", e);
        } catch (BadPaddingException e) {
            throw new KeyStoreWrapper.OwnException("Bad padding", e);
        } catch (IllegalBlockSizeException e) {
            throw new KeyStoreWrapper.OwnException("Illegal block size", e);
        }
    }

    public String sign(final String textString) throws KeyStoreWrapper.OwnException {
        ensureExists();

        try {
            if (textString.isEmpty()) throw new KeyStoreWrapper.OwnException("Empty text");

            final KeyStore.PrivateKeyEntry privateKeyEntry = this.privateKeyEntry();

            final Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKeyEntry.getPrivateKey());
            signature.update(textString.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (NoSuchAlgorithmException e) {
            throw new KeyStoreWrapper.OwnException("No such algorithm", e);
        } catch (InvalidKeyException e) {
            throw new KeyStoreWrapper.OwnException("Invalid key", e);
        } catch (SignatureException e) {
            throw new KeyStoreWrapper.OwnException("Signature failure", e);
        }
    }

    public boolean verify(final String textString, final String signatureString)
            throws KeyStoreWrapper.OwnException
    {
        ensureExists();

        try {
            if (textString.isEmpty()) throw new KeyStoreWrapper.OwnException("Empty text");
            if (signatureString.isEmpty()) {
                throw new KeyStoreWrapper.OwnException("Empty signature");
            }

            final KeyStore.PrivateKeyEntry privateKeyEntry = this.privateKeyEntry();

            final Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(privateKeyEntry.getCertificate());
            signature.update(textString.getBytes(StandardCharsets.UTF_8));

            return signature.verify(Base64.getDecoder().decode(signatureString));
        } catch (NoSuchAlgorithmException e) {
            throw new KeyStoreWrapper.OwnException("No such algorithm", e);
        } catch (InvalidKeyException e) {
            throw new KeyStoreWrapper.OwnException("Invalid key", e);
        } catch (SignatureException e) {
            throw new KeyStoreWrapper.OwnException("Signature failure", e);
        }
    }

    private KeyStore.PrivateKeyEntry privateKeyEntry() throws KeyStoreWrapper.OwnException {
        try {
            KeyStore.Entry entry = keyStore.getEntry(alias, null);

            if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
                throw new KeyStoreWrapper.OwnException("Is not a private key");
            }

            return (KeyStore.PrivateKeyEntry)entry;
        } catch (KeyStoreException e) {
            throw new KeyStoreWrapper.OwnException("Key store failure", e);
        } catch (NoSuchAlgorithmException e) {
            throw new KeyStoreWrapper.OwnException("No such algorithm", e);
        } catch (UnrecoverableEntryException e) {
            throw new KeyStoreWrapper.OwnException("Unrecoverable entry", e);
        }
    }

    private Cipher cipher() throws KeyStoreWrapper.OwnException {
        try {
            return Cipher.getInstance("RSA/ECB/PKCS1Padding");
        } catch (NoSuchAlgorithmException e) {
            throw new KeyStoreWrapper.OwnException("No such algorithm", e);
        } catch (NoSuchPaddingException e) {
            throw new KeyStoreWrapper.OwnException("No such padding", e);
        }
    }
}
