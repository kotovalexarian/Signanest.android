package com.libertarian_party.partynest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button addActivityButton  = (Button)findViewById(R.id.addActivityButton);
        Button listActivityButton = (Button)findViewById(R.id.listActivityButton);

        addActivityButton.setOnClickListener(onAddActivityButtonClick);
        listActivityButton.setOnClickListener(onListActivityButtonClick);
    }

    private View.OnClickListener onAddActivityButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent startIntent = new Intent(getApplicationContext(), AddActivity.class);
            startActivity(startIntent);
        }
    };

    private View.OnClickListener onListActivityButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent startIntent = new Intent(getApplicationContext(), ListActivity.class);
            startActivity(startIntent);
        }
    };
}
