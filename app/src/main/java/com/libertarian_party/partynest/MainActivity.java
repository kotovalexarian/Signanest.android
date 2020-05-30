package com.libertarian_party.partynest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button addButton = (Button)findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText firstNumberEditText  = (EditText)findViewById(R.id.firstNumberEditText);
                EditText secondNumberEditText = (EditText)findViewById(R.id.secondNumberEditText);
                TextView resultTextView       = (TextView)findViewById(R.id.resultTextView);

                int firstNumber  = Integer.parseInt(firstNumberEditText.getText().toString());
                int secondNumber = Integer.parseInt(secondNumberEditText.getText().toString());
                int result = firstNumber + secondNumber;

                resultTextView.setText(String.valueOf(result));
            }
        });
    }
}
