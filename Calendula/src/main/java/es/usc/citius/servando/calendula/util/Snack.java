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
import android.support.annotation.StringRes;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by joseangel.pineiro on 2/17/15.
 */
public class Snack {


    private static boolean shown = false;

    public static void show(final String string, final View rootView, final int duration) {
        final Snackbar snackbar = Snackbar.make(rootView, string, duration);
        snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                shown = false;
            }

            @Override
            public void onShown(Snackbar transientBottomBar) {
                super.onShown(transientBottomBar);
                shown = true;
            }
        });
        snackbar.show();
    }


    public static void show(final String string, final Activity activity, final int duration) {

        final ViewGroup rootView = (ViewGroup) ((ViewGroup) activity
                .findViewById(android.R.id.content)).getChildAt(0);

        show(string, rootView, duration);
    }

    public static void show(@StringRes final int string, final View rootView, final int duration) {
        show(rootView.getResources().getString(string), rootView, duration);
    }

    public static void show(final String string, final Activity activity) {
        show(string, activity, Snackbar.LENGTH_SHORT);
    }

    public static void show(final int string, final Activity activity) {
        show(activity.getResources().getString(string), activity, Snackbar.LENGTH_SHORT);
    }

    public static void show(final int string, final Activity activity, final int duration) {
        show(activity.getResources().getString(string), activity, duration);
    }

    public static void showIfUnobstructed(final int string, final Activity activity) {
        if (!shown)
            show(activity.getResources().getString(string), activity, Snackbar.LENGTH_SHORT);
    }

    public static void showIfUnobstructed(final int string, final Activity activity, final int duration) {
        if (!shown)
            show(activity.getResources().getString(string), activity, duration);
    }

}

