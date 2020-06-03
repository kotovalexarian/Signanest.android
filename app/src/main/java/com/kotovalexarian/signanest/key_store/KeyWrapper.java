package com.kotovalexarian.signanest.key_store;

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
            throws OwnException
    {
        if (alias.isEmpty()) throw new OwnException("Empty alias");

        this.keyStoreWrapper = keyStoreWrapper;
        this.keyStore = keyStore;
        this.alias = alias;
    }

    public String getAlias() { return alias; }

    public void ensureExists() throws OwnException {
        if (deleted) throw new OwnException("Alias was deleted");

        try {
            if (!keyStore.containsAlias(alias)) throw new OwnException("Alias doesn't exist");
        } catch (KeyStoreException e) {
            throw new OwnException("Key store doesn't work", e);
        }
    }

    public String getInfo() throws OwnException {
        return this.getAlgorithm();
    }

    public String getAlgorithm() throws OwnException {
        return this.privateKeyEntry().getPrivateKey().getAlgorithm();
    }

    public void delete() throws OwnException {
        ensureExists();

        try {
            deleted = true;
            keyStore.deleteEntry(alias);
            keyStoreWrapper.refresh();
        } catch (KeyStoreException e) {
            throw new OwnException("Key store failure", e);
        }
    }

    public String encrypt(final String plainText) throws OwnException {
        ensureExists();

        try {
            if (plainText.isEmpty()) throw new OwnException("Empty plain text");

            final KeyStore.PrivateKeyEntry privateKeyEntry = this.privateKeyEntry();
            final Cipher cipher = this.cipher();
            cipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.getCertificate().getPublicKey());

            return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
        } catch (InvalidKeyException e) {
            throw new OwnException("Invalid key", e);
        } catch (BadPaddingException e) {
            throw new OwnException("Bad padding", e);
        } catch (IllegalBlockSizeException e) {
            throw new OwnException("Illegal block size", e);
        }
    }

    public String decrypt(final String cipherText) throws OwnException {
        ensureExists();

        try {
            if (cipherText.isEmpty()) throw new OwnException("Empty cipher text");

            final KeyStore.PrivateKeyEntry privateKeyEntry = this.privateKeyEntry();
            final Cipher cipher = this.cipher();
            cipher.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());

            return new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)), StandardCharsets.UTF_8);
        } catch (InvalidKeyException e) {
            throw new OwnException("Invalid key", e);
        } catch (BadPaddingException e) {
            throw new OwnException("Bad padding", e);
        } catch (IllegalBlockSizeException e) {
            throw new OwnException("Illegal block size", e);
        }
    }

    public String sign(final String textString) throws OwnException {
        ensureExists();

        try {
            if (textString.isEmpty()) throw new OwnException("Empty text");

            final KeyStore.PrivateKeyEntry privateKeyEntry = this.privateKeyEntry();

            final Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKeyEntry.getPrivateKey());
            signature.update(textString.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (NoSuchAlgorithmException e) {
            throw new OwnException("No such algorithm", e);
        } catch (InvalidKeyException e) {
            throw new OwnException("Invalid key", e);
        } catch (SignatureException e) {
            throw new OwnException("Signature failure", e);
        }
    }

    public boolean verify(final String textString, final String signatureString)
            throws OwnException
    {
        ensureExists();

        try {
            if (textString.isEmpty()) throw new OwnException("Empty text");
            if (signatureString.isEmpty()) throw new OwnException("Empty signature");

            final KeyStore.PrivateKeyEntry privateKeyEntry = this.privateKeyEntry();

            final Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(privateKeyEntry.getCertificate());
            signature.update(textString.getBytes(StandardCharsets.UTF_8));

            return signature.verify(Base64.getDecoder().decode(signatureString));
        } catch (NoSuchAlgorithmException e) {
            throw new OwnException("No such algorithm", e);
        } catch (InvalidKeyException e) {
            throw new OwnException("Invalid key", e);
        } catch (SignatureException e) {
            throw new OwnException("Signature failure", e);
        }
    }

    private KeyStore.PrivateKeyEntry privateKeyEntry() throws OwnException {
        try {
            KeyStore.Entry entry = keyStore.getEntry(alias, null);

            if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
                throw new OwnException("Is not a private key");
            }

            return (KeyStore.PrivateKeyEntry)entry;
        } catch (KeyStoreException e) {
            throw new OwnException("Key store failure", e);
        } catch (NoSuchAlgorithmException e) {
            throw new OwnException("No such algorithm", e);
        } catch (UnrecoverableEntryException e) {
            throw new OwnException("Unrecoverable entry", e);
        }
    }

    private Cipher cipher() throws OwnException {
        try {
            return Cipher.getInstance("RSA/ECB/PKCS1Padding");
        } catch (NoSuchAlgorithmException e) {
            throw new OwnException("No such algorithm", e);
        } catch (NoSuchPaddingException e) {
            throw new OwnException("No such padding", e);
        }
    }
}
