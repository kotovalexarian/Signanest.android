package com.libertarian_party.partynest;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private ListView keyAliasesListView;

    private void setWidgetInstanceVariables() {
        keyAliasesListView = (ListView)findViewById(R.id.keyAliasesListView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setWidgetInstanceVariables();

        final String[] itemsArray = new String[] {
                "Hello, World!",
                "Some text",
                "Foo Bar",
                "Zoo Car",
        };

        final ArrayAdapter<String> itemsArrayAdapter = new ArrayAdapter<>(
                this,
                R.layout.simple_text_list,
                R.id.textView,
                itemsArray);

        keyAliasesListView.setAdapter(itemsArrayAdapter);
    }
}
