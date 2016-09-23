package es.usc.citius.servando.calendula.util;

import android.content.Context;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;

/**
 * Created by joseangel.pineiro on 10/29/15.
 */
public class IconUtils {

    public static IconicsDrawable icon(Context ctx, IIcon ic, int color){
        return new IconicsDrawable(ctx, ic)
                .sizeDp(48)
                .paddingDp(2)
                .colorRes(color);
    }

    public static IconicsDrawable icon(Context ctx, IIcon ic, int color, int size){
        return new IconicsDrawable(ctx, ic)
                .sizeDp(size)
                .paddingDp(0)
                .colorRes(color);
    }
}
