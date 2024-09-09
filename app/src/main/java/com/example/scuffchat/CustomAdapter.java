package com.example.scuffchat;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<Message> {
    private Activity context;
    private ArrayList<Message> messages;

    public CustomAdapter(Activity context, ArrayList<Message> messages) {
        super(context, R.layout.message_item, messages);
        this.context = context;
        this.messages = messages;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.message_item, null, true);

        TextView textViewUserEmail = listViewItem.findViewById(R.id.textViewUserEmail);
        ImageView imageViewMessage = listViewItem.findViewById(R.id.imageViewMessage);

        Message message = messages.get(position);

        // Display user email
        textViewUserEmail.setText(message.getUserEmail());

        // If the message contains an image, display it
        if (message.getImageBitmap() != null) {
            imageViewMessage.setVisibility(View.VISIBLE);
            imageViewMessage.setImageBitmap(message.getImageBitmap());
        } else {
            imageViewMessage.setVisibility(View.GONE);
        }

        return listViewItem;
    }
}