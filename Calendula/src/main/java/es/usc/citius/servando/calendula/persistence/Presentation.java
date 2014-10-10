package es.usc.citius.servando.calendula.persistence;

import android.content.res.Resources;

import es.usc.citius.servando.calendula.R;

/**
 * Created by joseangel.pineiro on 12/10/13.
 */
public enum Presentation {

    INJECTIONS(R.drawable.ic_presentation_1, R.string.injections, R.string.injections_units),
    CAPSULES(R.drawable.ic_presentation_2, R.string.capsules, R.string.capsules_units),
    EFFERVESCENT(R.drawable.ic_presentation_3, R.string.effervescent, R.string.effervescent_units),
    PILLS(R.drawable.ic_presentation_4, R.string.pills, R.string.pills_units),
    SYRUP(R.drawable.ic_presentation_5, R.string.syrup, R.string.syrup_units),
    DROPS(R.drawable.ic_presentation_6, R.string.drops, R.string.drops_units),
    UNKNOWN(R.drawable.ic_presentation_6, R.string.unknown, R.string.unknown_units);

    private int drawable = R.drawable.ic_presentation_1;
    private int nameString = R.string.unknown;
    private int unitsString = R.string.unknown_units;

    Presentation(int drawable, int nameString, int unitsString) {
        this.drawable = drawable;
        this.nameString = nameString;
        this.unitsString = unitsString;
    }

    public int getDrawable() {
        return drawable;
    }

    public String getName(Resources r) {
        return r.getString(nameString);
    }

    public String units(Resources r) {
        return r.getString(unitsString);
    }

}
