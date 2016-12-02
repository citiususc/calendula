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
 *    along with this software.  If not, see <http://www.gnu.org/licenses>.
 */

package es.usc.citius.servando.calendula.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Utility to access default shared preferences
 */
public class PreferenceUtils {

    private static PreferenceUtils instance;
    private final Context context;

    private PreferenceUtils(Context context){
        this.context = context;
    }

    public static void init(Context context){
        instance = new PreferenceUtils(context);
    }

    public static PreferenceUtils instance(){
        if(instance == null)
            throw new RuntimeException("PreferenceUtil must be initialized before using calling init(context)");
        return instance;
    }

    public SharedPreferences preferences(){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

}
