package com.example.scuffchat;

import android.graphics.Bitmap;

public interface BitmapDownloadCallback {
    void onBitmapDownloaded(Bitmap bitmap);
    void onBitmapDownloadFailed(String errorMessage);
}