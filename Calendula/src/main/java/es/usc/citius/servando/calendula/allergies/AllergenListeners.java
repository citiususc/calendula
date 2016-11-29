package es.usc.citius.servando.calendula.allergies;

import es.usc.citius.servando.calendula.persistence.PatientAllergen;

/**
 * Created by alvaro.brey.vilas on 8/11/16.
 */

public class AllergenListeners {
    public interface DeleteAllergyActionListener {
        public void onDeleteAction(PatientAllergen allergen);
    }

    public interface AddAllergyActionListener {
        public void onAddAction(AllergenVO allergen);
    }
}
