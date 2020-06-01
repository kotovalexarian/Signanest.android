package com.kotovalexarian.signanest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class KeyListFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private RecyclerView.Adapter recyclerViewAdapter;
    private KeyStoreWrapper keyStoreWrapper;
    private FloatingActionButton newKeyFab;
    private SwipeRefreshLayout swipeRefreshLayout;

    public static KeyListFragment newInstance() {
        final KeyListFragment fragment = new KeyListFragment();
        final Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_key_list, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        try {
            keyStoreWrapper = new KeyStoreWrapper();
        } catch (KeyStoreWrapper.OwnException e) {
            throw new RuntimeException("Key store wrapper failure", e);
        }

        recyclerView = (RecyclerView)getView().findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerViewLayoutManager = new LinearLayoutManager(getView().getContext());
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

        swipeRefreshLayout = getView().findViewById(R.id.swipeRefreshLayout);
        newKeyFab = getView().findViewById(R.id.newKeyFab);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    keyStoreWrapper.refresh();
                } catch (KeyStoreWrapper.OwnException e) {
                    throw new RuntimeException("Key store wrapper failure", e);
                }

                swipeRefreshLayout.setRefreshing(false);
            }
        });

        newKeyFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.newKeyAction);
            }
        });
    }

    public static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
        private KeyStoreWrapper keyStoreWrapper;

        public RecyclerViewAdapter(final KeyStoreWrapper keyStoreWrapper) {
            this.keyStoreWrapper = keyStoreWrapper;
        }

        @Override
        public RecyclerViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            return new RecyclerViewHolder(
                    LayoutInflater
                            .from(parent.getContext())
                            .inflate(R.layout.key_list_item, parent, false)
            );
        }

        @Override
        public void onBindViewHolder(
                final RecyclerViewHolder recyclerViewHolder,
                final int position)
        {
            final String alias = keyStoreWrapper.getByPosition(position).getAlias();
            recyclerViewHolder.textView.setText(alias);
            recyclerViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Bundle bundle = new Bundle();
                    bundle.putString(KeyFragment.ARG_ALIAS, alias);
                    Navigation.findNavController(view).navigate(R.id.showKeyAction, bundle);
                }
            });
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
            textView = view.findViewById(R.id.keyNameTextView);
        }
    }
}
