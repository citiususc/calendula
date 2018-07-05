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
import android.support.annotation.PluralsRes;
import android.support.annotation.StringRes;

import com.mikepenz.iconics.typeface.IIcon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.util.PresentationsTypeface;

public enum Presentation {

    CAPSULES(R.string.capsules, R.plurals.capsules_units, PresentationsTypeface.Icon.ic_capsule),
    PILLS(R.string.pills, R.plurals.pills_units, PresentationsTypeface.Icon.ic_pill),
    EFFERVESCENT(R.string.effervescent, R.plurals.effervescent_units, PresentationsTypeface.Icon.ic_effervescent),
    DROPS(R.string.drops, R.plurals.drops_units, PresentationsTypeface.Icon.ic_drops),
    SYRUP(R.string.syrup, R.plurals.syrup_units, PresentationsTypeface.Icon.ic_syrup),
    POMADE(R.string.pomade, R.plurals.pomade_units, PresentationsTypeface.Icon.ic_pomade),
    CREAM(R.string.cream, R.plurals.pomade_units, PresentationsTypeface.Icon.ic_cream),
    PATCHES(R.string.patches, R.plurals.patches_units, PresentationsTypeface.Icon.ic_patch),
    //POWDER_PATCHES(R.string.effervescent, R.plurals.effervescent_units),
    POWDER(R.string.powder, R.plurals.powder_units, PresentationsTypeface.Icon.ic_powder),
    SPRAY(R.string.spray, R.plurals.spray_units, PresentationsTypeface.Icon.ic_spray),
    INHALER(R.string.inhaler, R.plurals.inhaler_units, PresentationsTypeface.Icon.ic_inhaler),
    INJECTIONS(R.string.injections, R.plurals.injections_units, PresentationsTypeface.Icon.ic_injection),
    DIAMOND(R.string.generic_presentation, R.plurals.generic_units, PresentationsTypeface.Icon.ic_diamond),
    SQUARE(R.string.generic_presentation, R.plurals.generic_units, PresentationsTypeface.Icon.ic_square),
    TRIANGLE(R.string.generic_presentation, R.plurals.generic_units, PresentationsTypeface.Icon.ic_triangle),
    CIRCLE(R.string.generic_presentation, R.plurals.generic_units, PresentationsTypeface.Icon.ic_circle),
    HEXAGON(R.string.generic_presentation, R.plurals.generic_units, PresentationsTypeface.Icon.ic_hexagon),
    PENTAGON(R.string.generic_presentation, R.plurals.generic_units, PresentationsTypeface.Icon.ic_pentagon),
    UNKNOWN(R.string.unknown, R.plurals.unknown_units, PresentationsTypeface.Icon.ic_unknown);

    @StringRes
    private int nameString = R.string.unknown;
    @PluralsRes
    private int unitsString = R.plurals.unknown_units;
    private IIcon icon;

    Presentation(int nameString, int unitsString, IIcon icon) {
        this.nameString = nameString;
        this.unitsString = unitsString;
        this.icon = icon;
    }

    public String getName(Resources r) {
        return r.getString(nameString);
    }

    public String units(Resources r, double quantity) {
        final int intValue = Math.abs(quantity) == 1 ? 1 : 2;
        return r.getQuantityString(unitsString, intValue);
    }

    public IIcon icon() {
        return this.icon;
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
