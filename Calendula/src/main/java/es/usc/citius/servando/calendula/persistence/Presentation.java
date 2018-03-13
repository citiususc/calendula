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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.util.PresentationsTypeface;

public enum Presentation {

    CAPSULES(R.string.capsules, R.plurals.capsules_units),
    PILLS( R.string.pills, R.plurals.pills_units),
    EFFERVESCENT( R.string.effervescent, R.plurals.effervescent_units),
    DROPS(R.string.drops, R.plurals.drops_units),
    SYRUP(R.string.syrup, R.plurals.syrup_units),
    POMADE(R.string.pomade, R.plurals.pomade_units),
    CREAM(R.string.cream, R.plurals.pomade_units),
    PATCHES(R.string.patches, R.plurals.patches_units),
    //POWDER_PATCHES(R.string.effervescent, R.plurals.effervescent_units),
    POWDER(R.string.powder, R.plurals.powder_units),
    SPRAY(R.string.spray, R.plurals.spray_units),
    INHALER(R.string.inhaler, R.plurals.inhaler_units),
    INJECTIONS(R.string.injections, R.plurals.injections_units),
    DIAMOND(R.string.generic_presentation, R.plurals.generic_units),
    SQUARE(R.string.generic_presentation, R.plurals.generic_units),
    TRIANGLE(R.string.generic_presentation, R.plurals.generic_units),
    CIRCLE(R.string.generic_presentation, R.plurals.generic_units),
    HEXAGON(R.string.generic_presentation, R.plurals.generic_units),
    PENTAGON(R.string.generic_presentation, R.plurals.generic_units),
    UNKNOWN(R.string.unknown, R.plurals.unknown_units);

    private int nameString = R.string.unknown;
    @PluralsRes
    private int unitsString = R.plurals.unknown_units;

    Presentation(int nameString, int unitsString) {
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
                return PresentationsTypeface.Icon.ic_pomade;
            case SYRUP:
                return PresentationsTypeface.Icon.ic_syrup;
            case SPRAY:
                return PresentationsTypeface.Icon.ic_spray;
            case PILLS:
                return PresentationsTypeface.Icon.ic_pill;
            //case POWDER_PATCHES:
            //    return PresentationsTypeface.Icon.ic_powder_patch;
            case CREAM:
                return PresentationsTypeface.Icon.ic_cream;
            case CIRCLE:
                return PresentationsTypeface.Icon.ic_circle;
            case POWDER:
                return PresentationsTypeface.Icon.ic_powder;
            case SQUARE:
                return PresentationsTypeface.Icon.ic_square;
            case DIAMOND:
                return PresentationsTypeface.Icon.ic_diamond;
            case HEXAGON:
                return PresentationsTypeface.Icon.ic_hexagon;
            case PENTAGON:
                return PresentationsTypeface.Icon.ic_pentagon;
            case TRIANGLE:
                return PresentationsTypeface.Icon.ic_triangle;
            case UNKNOWN:
                return PresentationsTypeface.Icon.ic_unknown;
            default:
                return PresentationsTypeface.Icon.ic_unknown;
        }
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

    public static List<Presentation> available() {
        // all minus unknown
        List<Presentation> result = new ArrayList<>(Arrays.asList(values()));
        if(result.contains(UNKNOWN)){
            result.remove(UNKNOWN);
        }
        return result;
    }
}
