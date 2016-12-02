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

import android.content.res.Resources;

import es.usc.citius.servando.calendula.R;

/**
 * Created by joseangel.pineiro on 11/19/13.
 */
public class RandomColorChooser {

    private static final int[] primary_colors = {
            R.color.android_blue,
            R.color.android_pink,
            R.color.android_green,
            R.color.android_orange,
            R.color.android_red
    };
    private static final int[] secondary_colors = {
            R.color.android_blue_light,
            R.color.android_pink_light,
            R.color.android_green_light,
            R.color.android_orange_light,
            R.color.android_red_light
    };
    private static int colorIndex = 0;

    public static int getNextColorIndex() {
        return (colorIndex++) % primary_colors.length;
    }

    public static int getFixedColor(Object obj, Resources res) {
        return res.getColor(primary_colors[obj.hashCode() % primary_colors.length]);
    }

    public static int getFixedColorIdx(Object obj) {
        return obj.hashCode() % primary_colors.length;
    }

    public static int getPrimaryColor(int idx, Resources res) {
        return res.getColor(primary_colors[idx]);
    }

    public static int getSecondaryColor(int idx, Resources res) {
        return res.getColor(secondary_colors[idx]);
    }

}
