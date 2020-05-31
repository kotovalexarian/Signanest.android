package com.kotovalexarian.signanest;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class KeyActivity extends AppCompatActivity {
    private Button createKeyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key);

        createKeyButton = findViewById(R.id.createKeyButton);
        createKeyButton.setOnClickListener(onCreateKeyButtonClick);
    }

    private final View.OnClickListener onCreateKeyButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                final EditText keyNameEditText = findViewById(R.id.keyNameEditText);
                final KeyStoreWrapper keyStoreWrapper = new KeyStoreWrapper();
                final String keyName = keyNameEditText.getText().toString();
                keyStoreWrapper.create(keyName);
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