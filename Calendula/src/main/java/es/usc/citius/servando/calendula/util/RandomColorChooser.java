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
