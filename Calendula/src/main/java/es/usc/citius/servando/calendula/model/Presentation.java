package es.usc.citius.servando.calendula.model;

import android.content.res.Resources;

import es.usc.citius.servando.calendula.R;

/**
 * Created by joseangel.pineiro on 12/10/13.
 */
public enum Presentation {

    INJECTIONS(R.drawable.ic_presentation_1, R.string.injections),
    CAPSULES(R.drawable.ic_presentation_2, R.string.capsules),
    EFFERVESCENT(R.drawable.ic_presentation_3, R.string.effervescent),
    PILLS(R.drawable.ic_presentation_4, R.string.pills),
    SYRUP(R.drawable.ic_presentation_5, R.string.syrup),
    DROPS(R.drawable.ic_presentation_6, R.string.drops),
    UNKNOWN(R.drawable.ic_presentation_6, R.string.unknown);

    private int drawable = R.drawable.ic_presentation_1;
    private int nameString = R.string.unknown;

    Presentation(int drawable, int nameString) {
        this.drawable = drawable;
        this.nameString = nameString;
    }

    public int getDrawable() {
        return drawable;
    }

    public String getName(Resources r) {
        return r.getString(nameString);
    }

    public int getNameString() {
        return drawable;
    }
}
