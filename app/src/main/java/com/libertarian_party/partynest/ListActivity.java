package com.libertarian_party.partynest;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

public class ListActivity extends AppCompatActivity {
    private ListView listView;
    private String[] names;
    private String[] prices;
    private String[] descriptions;

    private void setInstanceVariables() {
        listView = (ListView)findViewById(R.id.listView);

        names        = getResources().getStringArray(R.array.names);
        prices       = getResources().getStringArray(R.array.prices);
        descriptions = getResources().getStringArray(R.array.descriptions);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        setInstanceVariables();
        ItemAdapter itemAdapter = new ItemAdapter(this, names, descriptions, prices);
        listView.setAdapter(itemAdapter);
    }
}