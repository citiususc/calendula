package es.usc.citius.servando.calendula.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.view.Display;

/**
 * Created by joseangel.pineiro on 11/20/13.
 */
public class Screen {

    private static Palette p;

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

    public static void createPalette(Context ctx, Bitmap bmp) {
        p = Palette.generate(bmp);
    }

    public static Palette getPalette() {
        return p;
    }

    public static int alpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.blue(color), Color.green(color));
    }


    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
