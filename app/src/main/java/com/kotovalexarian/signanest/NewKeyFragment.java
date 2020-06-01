package com.kotovalexarian.signanest;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.util.NavigableMap;

public class NewKeyFragment extends Fragment {
    private Button createKeyButton;

    public static NewKeyFragment newInstance() {
        final NewKeyFragment fragment = new NewKeyFragment();
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
        return inflater.inflate(R.layout.fragment_new_key, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        createKeyButton = getView().findViewById(R.id.createKeyButton);
        createKeyButton.setOnClickListener(onCreateKeyButtonClick);
    }

    private final View.OnClickListener onCreateKeyButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                final EditText keyNameEditText = getView().findViewById(R.id.keyNameEditText);
                final KeyStoreWrapper keyStoreWrapper = new KeyStoreWrapper();
                final String keyName = keyNameEditText.getText().toString();
                keyStoreWrapper.create(keyName);
                Navigation.findNavController(view).navigate(R.id.keyCreatedAction);
            } catch (KeyStoreWrapper.OwnException e) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Error!")
                        .setMessage("Can not create key due to error.")
                        .create()
                        .show();
            }
        }
    };
}
