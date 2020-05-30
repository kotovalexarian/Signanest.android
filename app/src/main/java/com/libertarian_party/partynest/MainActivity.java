package com.libertarian_party.partynest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private RecyclerView.Adapter recyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerViewLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);

        ArrayList<String> aliasesArrayList = new ArrayList<>();

        aliasesArrayList.add("Hello, World!");
        aliasesArrayList.add("Foo Bar");

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

        recyclerViewAdapter = new RecyclerViewAdapter(itemsArray);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    public static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
        private String[] items;

        public RecyclerViewAdapter(String[] items) {
            this.items = items;
        }

        @Override
        public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = (View)LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.simple_text_view, parent, false);

            return new RecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerViewHolder recyclerViewHolder, int position) {
            recyclerViewHolder.textView.setText(items[position]);
        }

        @Override
        public int getItemCount() {
            return items.length;
        }
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public RecyclerViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.textView);
        }
    }
}
