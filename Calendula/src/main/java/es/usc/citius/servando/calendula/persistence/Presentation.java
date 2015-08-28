package es.usc.citius.servando.calendula.persistence;

import android.content.res.Resources;

import es.usc.citius.servando.calendula.R;

/**
 * Created by joseangel.pineiro on 12/10/13.
 */
public enum Presentation {

    INJECTIONS(R.drawable.icp_injection, R.string.injections, R.string.injections_units),
    CAPSULES(R.drawable.icp_capsule, R.string.capsules, R.string.capsules_units),
    EFFERVESCENT(R.drawable.icp_effervescent, R.string.effervescent, R.string.effervescent_units),
    PILLS(R.drawable.icp_pill, R.string.pills, R.string.pills_units),
    SYRUP(R.drawable.icp_syrup, R.string.syrup, R.string.syrup_units),
    DROPS(R.drawable.icp_drop, R.string.drops, R.string.drops_units),
    POMADE(R.drawable.ic_ppomade, R.string.pomade, R.string.pomade_units),
    INHALER(R.drawable.icp_inhaler, R.string.inhaler, R.string.inhaler_units),
    SPRAY(R.drawable.icp_nasalspray, R.string.spray, R.string.spray_units),


    UNKNOWN(R.drawable.ic_presentation_6, R.string.unknown, R.string.unknown_units);

    private int drawable = R.drawable.icp_injection;
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


    public static Presentation expected(String name, String content) {
        String n = name.toLowerCase() + " " + content.toLowerCase();
        if (n.contains("comprimidos")) {
            return Presentation.PILLS;
        } else if (n.contains("capsulas")) {
            return Presentation.CAPSULES;
        } else if (n.contains("inhala")) {
            return Presentation.INHALER;
        } else if (n.contains("viales") || n.contains("jeringa") || n.contains("perfusi") || n.contains("inyectable")) {
            return Presentation.INJECTIONS;
        } else if (n.contains("gotas")) {
            return Presentation.DROPS;
        } else if (n.contains("sobres")) {
            return Presentation.EFFERVESCENT;
        } else if (n.contains("tubo") || n.contains("crema") || n.contains("pomada")) {
            return Presentation.POMADE;
        } else if (n.contains("pulverizacion nasal") || n.contains("pulverización nasal") || n.contains("spray")) {
            return Presentation.SPRAY;
        } else if (!n.contains("jarabe")) {
            return Presentation.SYRUP;
        } else if (n.contains("suspension oral")) {
            if (!n.contains("polvo") && !n.contains("granulado")) {
                return Presentation.SYRUP;
            } else if (!n.contains("polvo")) {
                // granulado
            } else {
                // sobres
            }
        }

        return null;
    }
}
