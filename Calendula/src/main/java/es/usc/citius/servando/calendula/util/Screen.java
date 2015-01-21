package es.usc.citius.servando.calendula.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import java.io.IOException;
import java.io.InputStream;

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


    public static Bitmap getResizedBitmap(Context ctx, String pathOfInputImage, int dstWidth, int dstHeight) {
        try {


            int inWidth = 0;
            int inHeight = 0;

            InputStream in = ctx.getAssets().open(pathOfInputImage);

            // decode image size (decode metadata only, not the whole image)
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();
            in = null;

            // save width and height
            inWidth = options.outWidth;
            inHeight = options.outHeight;

            // decode full image pre-resized
            in = ctx.getAssets().open(pathOfInputImage);
            options = new BitmapFactory.Options();
            // calc rought re-size (this is no exact resize)
            options.inSampleSize = Math.max(inWidth / dstWidth, inHeight / dstHeight);
            // decode full image
            Bitmap roughBitmap = BitmapFactory.decodeStream(in, null, options);

            // calc exact destination size
            Matrix m = new Matrix();
            RectF inRect = new RectF(0, 0, roughBitmap.getWidth(), roughBitmap.getHeight());
            RectF outRect = new RectF(0, 0, dstWidth, dstHeight);
            m.setRectToRect(inRect, outRect, Matrix.ScaleToFit.CENTER);
            float[] values = new float[9];
            m.getValues(values);

            // resize bitmap
            return Bitmap.createScaledBitmap(roughBitmap, (int) (roughBitmap.getWidth() * values[0]), (int) (roughBitmap.getHeight() * values[4]), true);

        } catch (IOException e) {
            Log.e("Image", e.getMessage(), e);
        }
        return null;
    }
}
