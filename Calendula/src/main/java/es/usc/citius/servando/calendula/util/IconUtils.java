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
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.util;

import android.content.Context;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by joseangel.pineiro on 10/29/15.
 */
public class IconUtils {

    private static Random random = new Random();

    private static List<CommunityMaterial.Icon> niceIcons = Arrays.asList(
            CommunityMaterial.Icon.cmd_cow,
            CommunityMaterial.Icon.cmd_duck,
            CommunityMaterial.Icon.cmd_flower,
            CommunityMaterial.Icon.cmd_thumb_up,
            CommunityMaterial.Icon.cmd_owl,
            CommunityMaterial.Icon.cmd_cat
    );

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

    public static IconicsDrawable icon(Context ctx, IIcon ic, int color, int size, int padding){
        return new IconicsDrawable(ctx, ic)
                .sizeDp(size)
                .paddingDp(padding)
                .colorRes(color);
    }

    public static IIcon randomNiceIcon(){
        return niceIcons.get(random.nextInt(niceIcons.size()));
    }
}
