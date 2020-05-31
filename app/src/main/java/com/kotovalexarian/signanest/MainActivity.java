package com.kotovalexarian.signanest;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private RecyclerView.Adapter recyclerViewAdapter;
    private KeyStoreWrapper keyStoreWrapper;
    private FloatingActionButton newKeyFab;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            keyStoreWrapper = new KeyStoreWrapper();
        } catch (KeyStoreWrapper.OwnException e) {
            throw new RuntimeException("Key store wrapper failure", e);
        }

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerViewLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerViewAdapter = new RecyclerViewAdapter(keyStoreWrapper);
        recyclerView.setAdapter(recyclerViewAdapter);

        try {
            keyStoreWrapper.onRefresh(new Runnable() {
                @Override
                public void run() {
                    recyclerViewAdapter.notifyDataSetChanged();
                }
            });
        } catch (KeyStoreWrapper.OwnException e) {
            throw new RuntimeException("Key store wrapper failure", e);
        }

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        newKeyFab = findViewById(R.id.newKeyFab);

        swipeRefreshLayout.setOnRefreshListener(onSwipeRefreshLayoutRefresh);
        newKeyFab.setOnClickListener(onNewKeyFabClick);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        try {
            keyStoreWrapper.refresh();
        } catch (KeyStoreWrapper.OwnException e) {
            throw new RuntimeException("Key store wrapper failure", e);
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
            recyclerViewHolder.textView.setText(keyStoreWrapper.getByPosition(position).getAlias());
        }

        @Override
        public int getItemCount() {
            return keyStoreWrapper.getCount();
        }
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public RecyclerViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.textView);
        }
    }

    private final SwipeRefreshLayout.OnRefreshListener onSwipeRefreshLayoutRefresh =
            new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            try {
                keyStoreWrapper.refresh();
            } catch (KeyStoreWrapper.OwnException e) {
                throw new RuntimeException("Key store wrapper failure", e);
            }

            swipeRefreshLayout.setRefreshing(false);
        }
    };

    private final View.OnClickListener onNewKeyFabClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), KeyActivity.class);
            startActivity(intent);
        }
    };
}
