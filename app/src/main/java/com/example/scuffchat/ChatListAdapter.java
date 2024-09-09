package com.example.scuffchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ChatListAdapter extends ArrayAdapter<String> {

    public ChatListAdapter(Context context, ArrayList<String> chatItems) {
        super(context, 0, chatItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String chatItem = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_chat_list, parent, false);
        }

        TextView chatNameTextView = convertView.findViewById(R.id.textChatName);


        chatNameTextView.setText(chatItem);

        return convertView;
    }
}