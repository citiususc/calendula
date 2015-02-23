package es.usc.citius.servando.calendula.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by joseangel.pineiro on 7/3/14.
 */
public class Settings {

    private static final String TAG = Settings.class.getName();

    private static final String SETTINGS_FILE_NAME = "settings.properties";

    private static final Settings instance = new Settings();

    Properties properties;

    public void load(Context ctx) throws Exception {
        Resources resources = ctx.getResources();
        AssetManager assetManager = resources.getAssets();
        // Read from the /assets directory

        Log.d(TAG, "Loading settings...");
        try {
            InputStream inputStream = assetManager.open(SETTINGS_FILE_NAME);
            properties = new Properties();
            properties.load(inputStream);
            Log.d(TAG, "Settings loaded successfully!" + properties.toString());
        } catch (IOException e) {
            properties = new Properties();
            throw new Exception("Error loading settings file", e);
        }
    }

    public String get(String key) {
        if (properties == null)
            throw new IllegalStateException("Settings not loaded");
        return properties.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        if (properties == null)
            throw new IllegalStateException("Settings not loaded");
        return properties.getProperty(key, defaultValue);
    }


    private Settings() {
    }

    public static Settings instance() {
        return instance;
    }

}
