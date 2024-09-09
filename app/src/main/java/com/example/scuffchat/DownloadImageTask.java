package com.example.scuffchat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    private BitmapDownloadCallback callback;
    private static final String TAG = "DownloadImageTask";

    public DownloadImageTask(BitmapDownloadCallback callback) {
        this.callback = callback;
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        String imageUrl = urls[0];
        Bitmap bitmap = null;

        if (imageUrl == null || imageUrl.isEmpty()) {
            Log.e(TAG, "Image URL is null or empty");
            return null;
        }

        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            Log.e(TAG, "Error downloading image: " + e.getMessage());
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if (result != null) {
            callback.onBitmapDownloaded(result);
        } else {
            callback.onBitmapDownloadFailed("Bitmap download failed.");
        }
    }
}
