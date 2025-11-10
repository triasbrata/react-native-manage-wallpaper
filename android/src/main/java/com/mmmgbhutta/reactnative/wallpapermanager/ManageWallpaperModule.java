package com.mmmgbhutta.reactnative.wallpapermanager;

import android.app.WallpaperManager;
import android.net.Uri;
import android.os.Build;

import java.io.InputStream;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

public class ManageWallpaperModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private WallpaperManager wallpaperManager;

    public ManageWallpaperModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        wallpaperManager = WallpaperManager.getInstance(reactContext);
    }

    @Override
    public String getName() {
        return "ManageWallpaper";
    }

    @ReactMethod
    public void setWallpaper(final ReadableMap params, final String type, Callback callback) {
        final String source = params.hasKey("uri") ? params.getString("uri") : null;

        WritableMap map = Arguments.createMap();

        if (source == null) {
            map.putString("status", "error");
            map.putString("msg", "Source URI is required");
            map.putString("url", "");
            callback.invoke(map);
            return;
        }

        // Parse URI for local storage file
        Uri uri;
        try {
            uri = Uri.parse(source);
            if (uri.getScheme() == null) {
                map.putString("status", "error");
                map.putString("msg", "Invalid URI: missing scheme");
                map.putString("url", source);
                callback.invoke(map);
                return;
            }

            // Only accept file:// and content:// URIs
            if (!uri.getScheme().equals("file") && !uri.getScheme().equals("content")) {
                map.putString("status", "error");
                map.putString("msg", "Only local file URIs are supported (file:// or content://)");
                map.putString("url", source);
                callback.invoke(map);
                return;
            }
        } catch (Exception e) {
            map.putString("status", "error");
            map.putString("msg", "Invalid URI: " + e.getMessage());
            map.putString("url", source);
            callback.invoke(map);
            return;
        }

        // Read file and set wallpaper
        try {
            InputStream stream = reactContext.getContentResolver().openInputStream(uri);
            if (stream == null) {
                map.putString("status", "error");
                map.putString("msg", "Failed to open file stream");
                map.putString("url", source);
                callback.invoke(map);
                return;
            }

            // Set wallpaper with version check for separate home/lock support
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (type.equals("home")) {
                    wallpaperManager.setStream(stream, null, false, WallpaperManager.FLAG_SYSTEM);
                } else if (type.equals("lock")) {
                    wallpaperManager.setStream(stream, null, false, WallpaperManager.FLAG_LOCK);
                } else {
                    wallpaperManager.setStream(stream, null, false, 0);
                }
            } else {
                wallpaperManager.setStream(stream);
            }

            stream.close();

            map.putString("status", "success");
            map.putString("msg", "Set Wallpaper Success");
            map.putString("url", source);
            callback.invoke(map);
        } catch (Exception e) {
            map.putString("status", "error");
            map.putString("msg", "Exception: " + e.getMessage());
            map.putString("url", source);
            callback.invoke(map);
        }
    }
}
