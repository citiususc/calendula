package es.usc.citius.servando.calendula.util;

import android.app.Activity;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.Display;

/**
 * Created by joseangel.pineiro on 11/20/13.
 */
public class Screen {

    public static PointF getDpSize(Activity activity) {

        PointF p = new PointF();
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        p.set(outMetrics.widthPixels / outMetrics.density, outMetrics.heightPixels / outMetrics.density);
        return p;
    }

    public static float getDensity(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        return outMetrics.density;
    }
}
