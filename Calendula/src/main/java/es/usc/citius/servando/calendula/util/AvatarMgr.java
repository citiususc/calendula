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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.graphics.Palette;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import es.usc.citius.servando.calendula.R;

/**
 * Created by joseangel.pineiro on 10/26/15.
 */
public class AvatarMgr {

    public static final String DEFAULT_AVATAR = "avatar_default";
    public static final String AVATAR_1 = "avatar_01";
    public static final String AVATAR_2 = "avatar_02";
    public static final String AVATAR_3 = "avatar_03";
    public static final String AVATAR_4 = "avatar_04";
    public static final String AVATAR_5 = "avatar_05";
    public static final String AVATAR_6 = "avatar_06";
    public static final String AVATAR_7 = "avatar_07";
    public static final String AVATAR_8 = "avatar_08";
    public static final String AVATAR_9 = "avatar_09";
    public static final String AVATAR_10 = "avatar_10";
    public static final String AVATAR_11 = "avatar_11";
    public static final String AVATAR_12 = "avatar_12";
    public static final String AVATAR_13 = "avatar_13";
    public static final String AVATAR_14 = "avatar_14";
    public static final String AVATAR_15 = "avatar_15";

    public static final Map<String, Integer> avatars;
    private static HashMap<String, int[]> cache = new HashMap<>();

    static {
        Map<String, Integer> map = new HashMap<>();
        map.put(AVATAR_1, R.drawable.avatar1);
        map.put(AVATAR_2, R.drawable.avatar2);
        map.put(AVATAR_3, R.drawable.avatar3);
        map.put(AVATAR_4, R.drawable.avatar4);
        map.put(AVATAR_5, R.drawable.avatar5);
        map.put(AVATAR_6, R.drawable.avatar6);
        map.put(AVATAR_7, R.drawable.avatar7);
        map.put(AVATAR_8, R.drawable.avatar8);
        map.put(AVATAR_9, R.drawable.avatar9);
        map.put(AVATAR_10, R.drawable.avatar10);
        map.put(AVATAR_11, R.drawable.avatar11);
        map.put(AVATAR_12, R.drawable.avatar12);
        map.put(AVATAR_13, R.drawable.baby);
        map.put(AVATAR_14, R.drawable.dog);
        map.put(AVATAR_15, R.drawable.cat);
        map.put(DEFAULT_AVATAR, R.drawable.avatar_default);

        avatars = Collections.unmodifiableMap(map);
    }

    public static int[] colorsFor(Resources res, String avatar) {

        int[] colors = new int[]{
                R.color.android_blue,
                R.color.android_blue_light};

        if (!cache.containsKey(avatar) && avatars.containsKey(avatar)) {
            Bitmap bm = BitmapFactory.decodeResource(res, avatars.get(avatar));
            if (bm != null) {
                Palette p = Palette.generate(bm);
                colors = new int[]{
                        p.getVibrantColor(res.getColor(R.color.android_blue)),
                        p.getLightVibrantColor(res.getColor(R.color.android_blue_light))
                };
            }
            cache.put(avatar, colors);
        }
        return cache.get(avatar);
    }

    public static int res(String avatar) {
        if (avatars.containsKey(avatar)) {
            return avatars.get(avatar);
        }
        return avatars.get(DEFAULT_AVATAR);
    }

}
