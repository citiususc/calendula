package es.usc.citius.servando.calendula.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.graphics.Palette;

import java.util.HashMap;

import es.usc.citius.servando.calendula.R;

/**
 * Created by joseangel.pineiro on 10/26/15.
 */
public class AvatarMgr {

    public static final int[] avatars = new int[]{
            R.drawable.avatar1,
            R.drawable.avatar2,
            R.drawable.avatar3,
            R.drawable.avatar4,
            R.drawable.avatar5,
            R.drawable.avatar6,
            R.drawable.avatar7,
            R.drawable.avatar8,
            R.drawable.avatar9,
            R.drawable.avatar10,
            R.drawable.avatar11,
            R.drawable.avatar12,
            R.drawable.avatar_default
    };

    private static HashMap<Integer, int[]> cache = new HashMap<>();

    public static int[] colorsFor(Resources res, int imageResource){

        if(!cache.containsKey(imageResource)) {
            Bitmap bm = BitmapFactory.decodeResource(res, imageResource);
            Palette p = Palette.generate(bm);
            int[] colors = new int[]{
                    p.getVibrantColor(R.color.android_blue),
                    p.getLightVibrantColor(R.color.android_blue_light)
            };
            cache.put(imageResource,colors);
        }

        return cache.get(imageResource);
    }

}
