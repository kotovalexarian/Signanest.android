package com.libertarian_party.partynest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ItemAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private String[] names;
    private String[] descriptions;
    private String[] prices;

    public ItemAdapter(Context context, String[] names, String[] descriptions, String[] prices) {
        this.layoutInflater =
                (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.names = names;
        this.descriptions = descriptions;
        this.prices = prices;
    }

    @Override
    public int getCount() {
        return names.length;
    }

    @Override
    public Object getItem(int position) {
        return names[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = layoutInflater.inflate(R.layout.list_view_detail, null);

        TextView nameTextView        = (TextView)view.findViewById(R.id.nameTextView);
        TextView descriptionTextView = (TextView)view.findViewById(R.id.descriptionTextView);
        TextView priceTextView       = (TextView)view.findViewById(R.id.priceTextView);

        String name        = names[position];
        String description = descriptions[position];
        String price       = prices[position];

        nameTextView.setText(name);
        descriptionTextView.setText(description);
        priceTextView.setText(price);

        return view;
    }
}
