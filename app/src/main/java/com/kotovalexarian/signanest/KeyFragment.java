package com.kotovalexarian.signanest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.kotovalexarian.signanest.key_store.KeyStoreWrapper;
import com.kotovalexarian.signanest.key_store.KeyWrapper;

public class KeyFragment extends Fragment {
    public static final String ARG_ALIAS = "alias";

    private String argAlias;
    private KeyStoreWrapper keyStoreWrapper;
    private KeyWrapper keyWrapper;

    private TextView keyNameTextView;
    private TextView keyInfoTextView;

    public static KeyFragment newInstance(final String alias) {
        KeyFragment fragment = new KeyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ALIAS, alias);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() == null) {
            Navigation.findNavController(getView()).navigate(R.id.keyListAction);
        }

        argAlias = getArguments().getString(ARG_ALIAS);

        try {
            keyStoreWrapper = new KeyStoreWrapper();
            keyWrapper = keyStoreWrapper.getByAlias(argAlias);
        } catch (KeyStoreWrapper.OwnException e) {
            throw new RuntimeException("Key store failure", e);
        }
    }

    @Override
    public View onCreateView(
            final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_key, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        keyNameTextView = getView().findViewById(R.id.keyNameTextView);
        keyInfoTextView = getView().findViewById(R.id.keyInfoTextView);
        keyNameTextView.setText(argAlias);

        try {
            keyInfoTextView.setText(keyWrapper.getInfo());
        } catch (KeyStoreWrapper.OwnException e) {
            throw new RuntimeException("Key store failure", e);
        }
    }
}
