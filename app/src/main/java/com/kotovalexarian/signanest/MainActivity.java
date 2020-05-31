package com.kotovalexarian.signanest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private RecyclerView.Adapter recyclerViewAdapter;
    private KeyStoreWrapper keyStoreWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            keyStoreWrapper = new KeyStoreWrapper();
        } catch (KeyStoreWrapper.OwnException e) {
            keyStoreWrapper = null;
        }

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerViewLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerViewAdapter = new RecyclerViewAdapter(keyStoreWrapper);
        recyclerView.setAdapter(recyclerViewAdapter);

        if (keyStoreWrapper != null) {
            keyStoreWrapper.onRefresh = new Runnable() {
                @Override
                public void run() {
                    recyclerViewAdapter.notifyDataSetChanged();
                }
            };
        }
    }

    public static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
        private KeyStoreWrapper keyStoreWrapper;

        public RecyclerViewAdapter(KeyStoreWrapper keyStoreWrapper) {
            this.keyStoreWrapper = keyStoreWrapper;
        }

        @Override
        public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RecyclerViewHolder(
                    LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.simple_text_view, parent, false)
            );
        }

        @Override
        public void onBindViewHolder(RecyclerViewHolder recyclerViewHolder, int position) {
            recyclerViewHolder.textView.setText(keyStoreWrapper.getAlias(position));
        }

        @Override
        public int getItemCount() {
            return keyStoreWrapper.getAliasCount();
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
