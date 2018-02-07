/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
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

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.mikepenz.materialize.Materialize;
import com.mikepenz.materialize.MaterializeBuilder;

import java.io.IOException;
import java.io.InputStream;

import es.usc.citius.servando.calendula.R;

/**
 * Created by joseangel.pineiro on 11/20/13.
 */
public class ScreenUtils {

    private static final String TAG = "ScreenUtils";
    private static Palette p;

    public static PointF getDpSize(Context context) {
        PointF p = new PointF();
        DisplayMetrics outMetrics = context.getResources().getDisplayMetrics();
        p.set(outMetrics.widthPixels / outMetrics.density, outMetrics.heightPixels / outMetrics.density);
        return p;
    }

    public static float getDensity(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        return outMetrics.density;
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
            LogUtil.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public static int equivalentNoAlpha(int color, float factor) {

        int white = 255;

        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        int r = (int) (white + (red - white) * factor);
        int g = (int) (white + (green - white) * factor);
        int b = (int) (white + (blue - white) * factor);

        return Color.rgb(r, g, b);

    }


    public static int equivalentNoAlpha(int color, int background, float factor) {

        int r_background = Color.red(background);
        int g_background = Color.green(background);
        int b_background = Color.blue(background);

        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        int r = (int) (r_background + (red - r_background) * factor);
        int g = (int) (g_background + (green - g_background) * factor);
        int b = (int) (b_background + (blue - b_background) * factor);

        return Color.rgb(r, g, b);

    }

    public static void setStatusBarColor(Activity activity, @ColorInt int color) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.setStatusBarColor(color);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            setWindowFlag(activity, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        }
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }


    public static Materialize materialize(Activity activity) {
        return materialize(activity, R.color.android_blue_statusbar);
    }

    public static Materialize materialize(Activity activity, int colorRes) {
        return new MaterializeBuilder()
                .withActivity(activity)
                .withTintedStatusBar(true)
                .withTranslucentStatusBar(true)
                .withStatusBarColorRes(colorRes)
                .build();
    }

    public static Materialize materializeForColor(Activity activity, int color) {
        return new MaterializeBuilder()
                .withActivity(activity)
                .withTintedStatusBar(true)
                .withTranslucentStatusBar(true)
                .withStatusBarColor(color)
                .build();
    }

    public static int getStatusBarHeight(Context ctx) {
        return ctx.getResources().getDimensionPixelSize(R.dimen.status_bar_height);
    }

    public static int dpToPx(Resources r, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }


}
