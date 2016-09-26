/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2016 CITIUS - USC
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

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

    private Settings() {
    }

    public static Settings instance() {
        return instance;
    }

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

}
