package com.libertarian_party.partynest;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {
    private ListView keyAliasesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setWidgetInstanceVariables();
        updateAliasesList();
    }

    private void setWidgetInstanceVariables() {
        keyAliasesListView = (ListView)findViewById(R.id.keyAliasesListView);
    }

    private void updateAliasesList() {
        ArrayList<String> aliasesArrayList = new ArrayList<>();

        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            Enumeration<String> aliasesEnumeration = keyStore.aliases();
            while (aliasesEnumeration.hasMoreElements())
                aliasesArrayList.add(aliasesEnumeration.nextElement());

        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            // Do nothing.
        }

        String[] itemsArray = new String[aliasesArrayList.size()];
        itemsArray = aliasesArrayList.toArray(itemsArray);

        final ArrayAdapter<String> itemsArrayAdapter = new ArrayAdapter<>(
                this,
                R.layout.simple_text_list,
                R.id.textView,
                itemsArray);

        keyAliasesListView.setAdapter(itemsArrayAdapter);
    }
}
