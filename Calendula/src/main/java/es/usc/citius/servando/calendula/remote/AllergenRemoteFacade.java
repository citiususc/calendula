package es.usc.citius.servando.calendula.remote;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.PatientAllergen;
import es.usc.citius.servando.calendula.remote.activeingredients.ActiveIngredient;
import es.usc.citius.servando.calendula.remote.activeingredients.ActiveIngredientsClient;
import es.usc.citius.servando.calendula.remote.excipients.Excipient;
import es.usc.citius.servando.calendula.remote.excipients.ExcipientsClient;

/**
 * Created by alvaro.brey.vilas on 7/11/16.
 */

public class AllergenRemoteFacade {

    private Patient patient;

    private AllergenRemoteFacade() {
    }

    public AllergenRemoteFacade(Context ctx) {
        patient = DB.patients().getActive(ctx);
    }

    public List<PatientAllergen> findAllergensByName(final String name) throws IOException {

        List<PatientAllergen> list = new ArrayList<>();
        List<ActiveIngredient> activeIngredients = ActiveIngredientsClient.findActiveIngredientsByName(name);
        List<Excipient> excipients = ExcipientsClient.findExcipientsByName(name);
        for (ActiveIngredient activeIngredient : activeIngredients) {
            list.add(toAllergen(activeIngredient));
        }
        for (Excipient excipient : excipients) {
            list.add(toAllergen(excipient));
        }

        return list;
    }

    public PatientAllergen toAllergen(final ActiveIngredient ingredient) {
        PatientAllergen p = new PatientAllergen(ingredient.getNombre(), PatientAllergen.AllergenType.ACTIVE_INGREDIENT, ingredient.get_id(), patient);
        return p;
    }

    public PatientAllergen toAllergen(final Excipient excipient) {
        PatientAllergen p = new PatientAllergen(excipient.getNombre(), PatientAllergen.AllergenType.EXCIPIENT, excipient.get_id(), patient);
        return p;
    }
}
