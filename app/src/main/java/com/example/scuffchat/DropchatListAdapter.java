package com.example.scuffchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DropchatListAdapter extends ArrayAdapter<String> {

    public DropchatListAdapter(Context context, ArrayList<String> chatItems) {
        super(context, 0, chatItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        String chatItem = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_dropchat_list, parent, false);
        }

        // Lookup view for data population
        TextView chatNameTextView = convertView.findViewById(R.id.textDropchatName);

        // Populate the data into the template view using the data object
        chatNameTextView.setText(chatItem);

        // Return the completed view to render on screen
        return convertView;
    }
}