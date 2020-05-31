package com.libertarian_party.partynest;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

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
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        keyStoreWrapper = new KeyStoreWrapper(context);
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
        assertEquals(0, keyStoreWrapper.getAliasCount());
        keyStoreWrapper.create("foo");
        assertEquals(1, keyStoreWrapper.getAliasCount());
        keyStoreWrapper.create("bar");
        assertEquals(2, keyStoreWrapper.getAliasCount());
        keyStoreWrapper.create("car");
        assertEquals(3, keyStoreWrapper.getAliasCount());

        assertEquals("bar", keyStoreWrapper.getAlias(0));
        assertEquals("car", keyStoreWrapper.getAlias(1));
        assertEquals("foo", keyStoreWrapper.getAlias(2));

        keyStoreWrapper.delete("car");
        assertEquals(2, keyStoreWrapper.getAliasCount());

        assertEquals("bar", keyStoreWrapper.getAlias(0));
        assertEquals("foo", keyStoreWrapper.getAlias(1));
    }

    @Test
    public void encryptionAndDecryption() throws KeyStoreWrapper.OwnException {
        final String alias = "foo";
        keyStoreWrapper.create(alias);

        final String plainText = "Hello, World!";
        final String cypherText = keyStoreWrapper.encrypt(alias, plainText);

        assertEquals(plainText, keyStoreWrapper.decrypt(alias, cypherText));
    }

    @Test
    public void signingAndVerifying() throws KeyStoreWrapper.OwnException {
        final String alias = "foo";
        keyStoreWrapper.create(alias);

        final String text = "Hello, World!";
        final String signature = keyStoreWrapper.sign(alias, text);

        final String invalidSignature =
                Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));

        assertTrue(keyStoreWrapper.verify(alias, text, signature));
        assertFalse(keyStoreWrapper.verify(alias, text, invalidSignature));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void indexOutOfBoundsWhenNoAliasesExist() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.getAlias(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void indexOutOfBoundsWhenSomeAliasesExist() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.create("foo");
        keyStoreWrapper.create("bar");

        keyStoreWrapper.getAlias(2);
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void creatingAliasThatAlreadyExists() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.create("foo");
        keyStoreWrapper.create("foo");
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void deletingAliasThatDoesNotExist() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.delete("foo");
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void encryptingWithAliasThanDoesNotExist() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.encrypt("foo", "Hello, World!");
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void decryptingWithAliasThatDoesNotExist() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.decrypt("foo", "jfhslkjhjkslhgjkhklhgkjfdsgh");
    }

    @Test(expected = IllegalArgumentException.class)
    public void creatingInvalidAlias() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.create("");
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void deletingInvalidAlias() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.delete("");
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void encryptingWithInvalidAlias() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.encrypt("", "Hello, World!");
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void decryptingWithInvalidAlias() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.decrypt("", "dhsdkljghlksjfdhgkjhslghsdfkgh");
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void encryptingEmptyText() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.create("foo");
        keyStoreWrapper.encrypt("foo", "");
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void decryptingEmptyText() throws KeyStoreWrapper.OwnException {
        keyStoreWrapper.create("foo");
        keyStoreWrapper.decrypt("foo", "");
    }
}
