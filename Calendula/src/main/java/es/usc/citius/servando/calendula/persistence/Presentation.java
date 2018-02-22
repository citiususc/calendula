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

package es.usc.citius.servando.calendula.persistence;

import android.content.res.Resources;
import android.support.annotation.DrawableRes;
import android.support.annotation.PluralsRes;
import android.support.annotation.StringRes;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.typeface.IIcon;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.util.PresentationsTypeface;

public enum Presentation {

    INJECTIONS(R.drawable.icp_injection, R.string.injections, R.plurals.injections_units),
    CAPSULES(R.drawable.icp_capsule, R.string.capsules, R.plurals.capsules_units),
    EFFERVESCENT(R.drawable.icp_effervescent, R.string.effervescent, R.plurals.effervescent_units),
    PILLS(R.drawable.icp_pill, R.string.pills, R.plurals.pills_units),
    SYRUP(R.drawable.icp_syrup, R.string.syrup, R.plurals.syrup_units),
    DROPS(R.drawable.icp_drop, R.string.drops, R.plurals.drops_units),
    POMADE(R.drawable.ic_ppomade, R.string.pomade, R.plurals.pomade_units),
    INHALER(R.drawable.icp_inhaler, R.string.inhaler, R.plurals.inhaler_units),
    SPRAY(R.drawable.icp_nasalspray, R.string.spray, R.plurals.spray_units),
    PATCHES(R.drawable.icp_patches, R.string.patches, R.plurals.patches_units),


    UNKNOWN(R.drawable.ic_presentation_6, R.string.unknown, R.plurals.unknown_units);

    @DrawableRes
    private int drawable = R.drawable.icp_injection;
    @StringRes
    private int nameString = R.string.unknown;
    @PluralsRes
    private int unitsString = R.plurals.unknown_units;

    Presentation(int drawable, int nameString, int unitsString) {
        this.drawable = drawable;
        this.nameString = nameString;
        this.unitsString = unitsString;
    }

    public static IIcon iconFor(Presentation p) {

        switch (p) {
            case CAPSULES:
                return PresentationsTypeface.Icon.ic_capsule;
            case DROPS:
                return PresentationsTypeface.Icon.ic_drops;
            case EFFERVESCENT:
                return PresentationsTypeface.Icon.ic_effervescent;
            case INHALER:
                return PresentationsTypeface.Icon.ic_inhaler;
            case INJECTIONS:
                return PresentationsTypeface.Icon.ic_injection;
            case PATCHES:
                return PresentationsTypeface.Icon.ic_patch;
            case POMADE:
                return PresentationsTypeface.Icon.ic_cream;
            case SPRAY:
                return PresentationsTypeface.Icon.ic_spray;
            case SYRUP:
                return PresentationsTypeface.Icon.ic_syrup;
            case PILLS:
                return PresentationsTypeface.Icon.ic_pill;
            default:
                return CommunityMaterial.Icon.cmd_help_circle;

        }


    }

    public int getDrawable() {
        return drawable;
    }

    public String getName(Resources r) {
        return r.getString(nameString);
    }

    public String units(Resources r, double quantity) {
        final int intValue = Math.abs(quantity) == 1 ? 1 : 2;
        return r.getQuantityString(unitsString, intValue);
    }

    public IIcon icon() {
        return iconFor(this);
    }
}
