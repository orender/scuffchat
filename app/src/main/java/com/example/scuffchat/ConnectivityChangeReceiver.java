package com.example.scuffchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.widget.Toast;

public class ConnectivityChangeReceiver extends BroadcastReceiver {

    private static final int INTERNET_QUALITY_THRESHOLD = 2; // Adjust this threshold as needed
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());

        Network network = connectivityManager.getActiveNetwork();

        if (network == null) {
            // No active network, handle absence of internet connection
            showPopupAndCloseApp();
            return;
        }

        if (capabilities != null) {
            int internetQuality = getInternetQuality(capabilities);

            if (internetQuality < INTERNET_QUALITY_THRESHOLD) {
                showPopupAndCloseApp();
            }
        }
    }

    private int getInternetQuality(NetworkCapabilities capabilities) {
        // Check internet quality based on the network capabilities
        // Return a value representing the quality of the internet connection
        // For simplicity, this example uses a basic check
        if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            return 5; // Good internet quality
        } else {
            return 1; // Bad internet quality
        }
    }

    private void showPopupAndCloseApp() {
        // Show a popup message indicating no internet connection
        Toast.makeText(mContext, "No internet connection", Toast.LENGTH_SHORT).show();

        // Close the app after a short delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Close the app
                System.exit(0);
            }
        }, 2000); // 2000 milliseconds (2 seconds) delay before closing the app
    }
}