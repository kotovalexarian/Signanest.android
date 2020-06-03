package com.kotovalexarian.signanest;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.kotovalexarian.signanest.key_store.KeyStoreWrapper;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Base64;
import java.util.Enumeration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class KeyStoreWrapperTest {
    private KeyStoreWrapper keyStoreWrapper;

    @BeforeClass
    public static void beforeAll() throws Exception {
        deleteAllAliases();
    }

    @Before
    public void beforeEach() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper = new KeyStoreWrapper();
    }

    @After
    public void afterEach() throws Exception {
        deleteAllAliases();
    }

    private static void deleteAllAliases() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        Enumeration<String> enumeration = keyStore.aliases();
        while (enumeration.hasMoreElements()) {
            String alias = enumeration.nextElement();
            keyStore.deleteEntry(alias);
        }
    }

    @Test
    public void aliases() throws KeyStoreWrapper.OwnException {
        assertEquals(0, keyStoreWrapper.getCount());
        keyStoreWrapper.create("foo");
        assertEquals(1, keyStoreWrapper.getCount());
        keyStoreWrapper.create("bar");
        assertEquals(2, keyStoreWrapper.getCount());
        keyStoreWrapper.create("car");
        assertEquals(3, keyStoreWrapper.getCount());

        assertEquals("bar", keyStoreWrapper.getByPosition(0).getAlias());
        assertEquals("car", keyStoreWrapper.getByPosition(1).getAlias());
        assertEquals("foo", keyStoreWrapper.getByPosition(2).getAlias());

        keyStoreWrapper.getByAlias("car").delete();
        assertEquals(2, keyStoreWrapper.getCount());

        assertEquals("bar", keyStoreWrapper.getByPosition(0).getAlias());
        assertEquals("foo", keyStoreWrapper.getByPosition(1).getAlias());
    }

    @Test
    public void encryptionAndDecryption() throws KeyStoreWrapper.OwnException {
        final String alias = "foo";
        keyStoreWrapper.create(alias);

        final String plainText = "Hello, World!";
        final String cypherText = keyStoreWrapper.getByAlias(alias).encrypt(plainText);

        assertEquals(plainText, keyStoreWrapper.getByAlias(alias).decrypt(cypherText));
    }

    @Test
    public void signingAndVerifying() throws KeyStoreWrapper.OwnException {
        final String alias = "foo";
        keyStoreWrapper.create(alias);

        final String text = "Hello, World!";
        final String signature = keyStoreWrapper.getByAlias(alias).sign(text);

        final String invalidSignature =
                Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));

        assertTrue(keyStoreWrapper.getByAlias(alias).verify(text, signature));
        assertFalse(keyStoreWrapper.getByAlias(alias).verify(text, invalidSignature));
    }

    @Test
    public void getInfo() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.create("foo");
        assertEquals("RSA", keyStoreWrapper.getByAlias("foo").getAlgorithm());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void indexOutOfBoundsWhenNoAliasesExist() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.getByPosition(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void indexOutOfBoundsWhenSomeAliasesExist() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.create("foo");
        keyStoreWrapper.create("bar");

        keyStoreWrapper.getByPosition(2);
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void creatingAliasThatAlreadyExists() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.create("foo");
        keyStoreWrapper.create("foo");
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void deletingAliasThatDoesNotExist() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.getByAlias("foo").delete();
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void encryptingWithAliasThanDoesNotExist() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.getByAlias("foo").encrypt("Hello, World!");
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void decryptingWithAliasThatDoesNotExist() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.getByAlias("foo").decrypt("jfhslkjhjkslhgjkhklhgkjfdsgh");
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void signingWithAliasThatDoesNotExist() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.getByAlias("foo").sign("Hello, World!");
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void verifyingWithAliasThatDoesNotExist() throws KeyStoreWrapper.OwnException {
        final String text = "Hello, World!";

        final String invalidSignature =
                Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));

        keyStoreWrapper.getByAlias("foo").verify(text, invalidSignature);
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void creatingInvalidAlias() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.create("");
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void deletingInvalidAlias() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.getByAlias("").delete();
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void encryptingWithInvalidAlias() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.getByAlias("").encrypt("Hello, World!");
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void decryptingWithInvalidAlias() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.getByAlias("").decrypt("dhsdkljghlksjfdhgkjhslghsdfkgh");
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void signingWithInvalidAlias() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.getByAlias("").sign("Hello, World!");
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void verifyingWithInvalidAlias() throws KeyStoreWrapper.OwnException {
        final String text = "Hello, World!";

        final String invalidSignature =
                Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));

        keyStoreWrapper.getByAlias("").verify(text, invalidSignature);
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void encryptingEmptyText() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.create("foo");
        keyStoreWrapper.getByAlias("foo").encrypt("");
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void decryptingEmptyText() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.create("foo");
        keyStoreWrapper.getByAlias("foo").decrypt("");
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void signingEmptyText() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.create("foo");
        keyStoreWrapper.getByAlias("foo").sign("");
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void verifyingEmptyText() throws KeyStoreWrapper.OwnException {
        final String text = "";

        final String invalidSignature =
                Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));

        keyStoreWrapper.create("foo");
        keyStoreWrapper.getByAlias("foo").verify(text, invalidSignature);
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void verifyingEmptySignature() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.create("foo");
        keyStoreWrapper.getByAlias("foo").verify("Hello, World!", "");
    }
}
