package com.example.scuffchat;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatsList extends AppCompatActivity {
    private ChatListAdapter adapter;
    private ListView listViewChats;
    private ArrayList<String> chatIds;
    private ConnectivityChangeReceiver receiver;
    private DatabaseReference chatsRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats_list);

        Button buttonLeaveChat = findViewById(R.id.LeaveChatList);
        buttonLeaveChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveChatList();
            }
        });

        receiver = new ConnectivityChangeReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);

        listViewChats = findViewById(R.id.listViewChats);
        chatIds = new ArrayList<>();
        adapter = new ChatListAdapter(this, chatIds);
        listViewChats.setAdapter(adapter);


        //set notification for when user get dropchat
        Intent serviceIntent = new Intent(this, FirebaseListenerWaterdropService.class);
        startService(serviceIntent);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        chatsRef = database.getReference("chat_rooms");


        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatIds.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String chatId = snapshot.getKey();
                    chatIds.add(chatId);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        listViewChats.setOnItemClickListener((parent, view, position, id) -> {
            String selectedChatId = chatIds.get(position);
            Intent intent = new Intent(ChatsList.this, ChatRoom.class);
            intent.putExtra("chatId", selectedChatId);
            startActivity(intent);
        });

        Button buttonCreateChat = findViewById(R.id.buttonCreateChat);
        buttonCreateChat.setOnClickListener(v -> createNewChat());
    }

    private void createNewChat() {
        Intent intent = new Intent(ChatsList.this, NewChat.class);
        startActivity(intent);
    }

    private void leaveChatList() {
        Intent intent = new Intent(ChatsList.this, MenuActivity.class);
        startActivity(intent);
        finish(); // Optional: Close the current activity
    }
}