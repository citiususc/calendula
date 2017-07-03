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

import android.app.Activity;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;

/**
 * Created by joseangel.pineiro on 2/17/15.
 */
public class Snack {

    public static void show(final String string, final Activity activity, final Snackbar.SnackbarDuration duration) {

        SnackbarManager.show(com.nispok.snackbar.Snackbar.with(activity.getApplicationContext())
                        .type(SnackbarType.MULTI_LINE)
                        .duration(duration)
                        .text(string)
                , activity);
    }

    public static void show(final String string, final Activity activity) {
        show(string, activity, Snackbar.SnackbarDuration.LENGTH_SHORT);
    }

    public static void show(final int string, final Activity activity) {
        show(activity.getResources().getString(string), activity, Snackbar.SnackbarDuration.LENGTH_SHORT);
    }

    public static void show(final int string, final Activity activity, final Snackbar.SnackbarDuration duration) {
        show(activity.getResources().getString(string), activity, duration);
    }

    public static void showIfUnobstructed(final int string, final Activity activity) {
        Snackbar current = SnackbarManager.getCurrentSnackbar();
        if (current == null || !current.isShowing())
            show(activity.getResources().getString(string), activity, Snackbar.SnackbarDuration.LENGTH_SHORT);
    }

    public static void showIfUnobstructed(final int string, final Activity activity, final Snackbar.SnackbarDuration duration) {
        Snackbar current = SnackbarManager.getCurrentSnackbar();
        if (current == null || !current.isShowing())
            show(activity.getResources().getString(string), activity, duration);
    }

}

