package com.libertarian_party.partynest;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.KeyStore;
import java.util.Enumeration;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class KeyStoreWrapperTest {
    @BeforeClass
    public static void beforeAll() throws Exception {
        deleteAllAliases();
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
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        KeyStoreWrapper keyStoreWrapper = new KeyStoreWrapper(context);

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
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void indexOutOfBoundsWhenNoAliasesExist() throws KeyStoreWrapper.OwnException {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        KeyStoreWrapper keyStoreWrapper = new KeyStoreWrapper(context);

        keyStoreWrapper.getAlias(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void indexOutOfBoundsWhenSomeAliasesExist() throws KeyStoreWrapper.OwnException {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        KeyStoreWrapper keyStoreWrapper = new KeyStoreWrapper(context);
        keyStoreWrapper.create("foo");
        keyStoreWrapper.create("bar");

        keyStoreWrapper.getAlias(2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidName() throws KeyStoreWrapper.OwnException {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        KeyStoreWrapper keyStoreWrapper = new KeyStoreWrapper(context);

        keyStoreWrapper.create("");
    }

    @Test(expected = KeyStoreWrapper.OwnException.class)
    public void aliasAlreadyExists() throws KeyStoreWrapper.OwnException {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        KeyStoreWrapper keyStoreWrapper = new KeyStoreWrapper(context);

        keyStoreWrapper.create("foo");
        keyStoreWrapper.create("foo");
    }
}
