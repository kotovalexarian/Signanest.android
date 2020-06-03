package com.kotovalexarian.signanest;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.kotovalexarian.signanest.key_store.KeyStoreWrapper;
import com.kotovalexarian.signanest.key_store.OwnException;

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
    public void beforeEach() throws OwnException {
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
    public void aliases() throws OwnException {
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
    public void encryptionAndDecryption() throws OwnException {
        final String alias = "foo";
        keyStoreWrapper.create(alias);

        final String plainText = "Hello, World!";
        final String cypherText = keyStoreWrapper.getByAlias(alias).encrypt(plainText);

        assertEquals(plainText, keyStoreWrapper.getByAlias(alias).decrypt(cypherText));
    }

    @Test
    public void signingAndVerifying() throws OwnException {
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
    public void getInfo() throws OwnException {
        keyStoreWrapper.create("foo");
        assertEquals("RSA", keyStoreWrapper.getByAlias("foo").getAlgorithm());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void indexOutOfBoundsWhenNoAliasesExist() throws OwnException {
        keyStoreWrapper.getByPosition(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void indexOutOfBoundsWhenSomeAliasesExist() throws OwnException {
        keyStoreWrapper.create("foo");
        keyStoreWrapper.create("bar");

        keyStoreWrapper.getByPosition(2);
    }

    @Test(expected = OwnException.class)
    public void creatingAliasThatAlreadyExists() throws OwnException {
        keyStoreWrapper.create("foo");
        keyStoreWrapper.create("foo");
    }

    @Test(expected = OwnException.class)
    public void deletingAliasThatDoesNotExist() throws OwnException {
        keyStoreWrapper.getByAlias("foo").delete();
    }

    @Test(expected = OwnException.class)
    public void encryptingWithAliasThanDoesNotExist() throws OwnException {
        keyStoreWrapper.getByAlias("foo").encrypt("Hello, World!");
    }

    @Test(expected = OwnException.class)
    public void decryptingWithAliasThatDoesNotExist() throws OwnException {
        keyStoreWrapper.getByAlias("foo").decrypt("jfhslkjhjkslhgjkhklhgkjfdsgh");
    }

    @Test(expected = OwnException.class)
    public void signingWithAliasThatDoesNotExist() throws OwnException {
        keyStoreWrapper.getByAlias("foo").sign("Hello, World!");
    }

    @Test(expected = OwnException.class)
    public void verifyingWithAliasThatDoesNotExist() throws OwnException {
        final String text = "Hello, World!";

        final String invalidSignature =
                Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));

        keyStoreWrapper.getByAlias("foo").verify(text, invalidSignature);
    }

    @Test(expected = OwnException.class)
    public void creatingInvalidAlias() throws OwnException {
        keyStoreWrapper.create("");
    }

    @Test(expected = OwnException.class)
    public void deletingInvalidAlias() throws OwnException {
        keyStoreWrapper.getByAlias("").delete();
    }

    @Test(expected = OwnException.class)
    public void encryptingWithInvalidAlias() throws OwnException {
        keyStoreWrapper.getByAlias("").encrypt("Hello, World!");
    }

    @Test(expected = OwnException.class)
    public void decryptingWithInvalidAlias() throws OwnException {
        keyStoreWrapper.getByAlias("").decrypt("dhsdkljghlksjfdhgkjhslghsdfkgh");
    }

    @Test(expected = OwnException.class)
    public void signingWithInvalidAlias() throws OwnException {
        keyStoreWrapper.getByAlias("").sign("Hello, World!");
    }

    @Test(expected = OwnException.class)
    public void verifyingWithInvalidAlias() throws OwnException {
        final String text = "Hello, World!";

        final String invalidSignature =
                Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));

        keyStoreWrapper.getByAlias("").verify(text, invalidSignature);
    }

    @Test(expected = OwnException.class)
    public void encryptingEmptyText() throws OwnException {
        keyStoreWrapper.create("foo");
        keyStoreWrapper.getByAlias("foo").encrypt("");
    }

    @Test(expected = OwnException.class)
    public void decryptingEmptyText() throws OwnException {
        keyStoreWrapper.create("foo");
        keyStoreWrapper.getByAlias("foo").decrypt("");
    }

    @Test(expected = OwnException.class)
    public void signingEmptyText() throws OwnException {
        keyStoreWrapper.create("foo");
        keyStoreWrapper.getByAlias("foo").sign("");
    }

    @Test(expected = OwnException.class)
    public void verifyingEmptyText() throws OwnException {
        final String text = "";

        final String invalidSignature =
                Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));

        keyStoreWrapper.create("foo");
        keyStoreWrapper.getByAlias("foo").verify(text, invalidSignature);
    }

    @Test(expected = OwnException.class)
    public void verifyingEmptySignature() throws OwnException {
        keyStoreWrapper.create("foo");
        keyStoreWrapper.getByAlias("foo").verify("Hello, World!", "");
    }
}
