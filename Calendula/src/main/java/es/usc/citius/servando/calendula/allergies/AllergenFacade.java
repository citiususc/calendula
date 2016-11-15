package es.usc.citius.servando.calendula.allergies;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.model.persistence.ActiveIngredient;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Excipient;

/**
 * Created by alvaro.brey.vilas on 15/11/16.
 */

public class AllergenFacade {

    private static final long ALLERGEN_SEARCH_LIMIT = 60;
    private static final String TAG = "AllergenFacade";


    public static List<AllergenVO> searchForAllergens(final String name) {
        Log.d(TAG, "searchForAllergens() called with: name = [" + name + "]");

        final String pattern = "%" + name + "%";

        List<AllergenVO> ret = new ArrayList<>();
        List<ActiveIngredient> activeIngredients = DB.drugDB().activeIngredients().like(ActiveIngredient.COLUMN_NAME, pattern, ALLERGEN_SEARCH_LIMIT);
        Log.v(TAG, "Received " + activeIngredients.size() + " active ingredients");
        for (ActiveIngredient activeIngredient : activeIngredients) {
            ret.add(new AllergenVO(activeIngredient));
        }
        List<Excipient> excipients = DB.drugDB().excipients().like(ActiveIngredient.COLUMN_NAME, pattern, ALLERGEN_SEARCH_LIMIT - activeIngredients.size());
        Log.v(TAG, "Received " + excipients.size() + " excipients");
        for (Excipient excipient : excipients) {
            ret.add(new AllergenVO(excipient));
        }

        return ret;
    }
}
