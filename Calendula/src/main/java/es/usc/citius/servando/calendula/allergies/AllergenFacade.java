package es.usc.citius.servando.calendula.allergies;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.model.persistence.ActiveIngredient;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Excipient;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;
import es.usc.citius.servando.calendula.drugdb.model.persistence.PrescriptionActiveIngredient;
import es.usc.citius.servando.calendula.drugdb.model.persistence.PrescriptionExcipient;

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

        Log.d(TAG, "searchForAllergens() returned: " + ret);
        return ret;
    }

    public static List<AllergenVO> findAllergensForPrescription(final Prescription p) {
        Log.d(TAG, "getAllergensForPrescription() called with: p = [" + p + "]");
        List<AllergenVO> ret = new ArrayList<>();
        String code = p.getCode();

        // active ingredients
        List<PrescriptionActiveIngredient> pais = DB.drugDB().prescriptionActiveIngredients().findBy(PrescriptionActiveIngredient.COLUMN_PRESCRIPTION_CODE, code);
        for (PrescriptionActiveIngredient pai : pais) {
            List<ActiveIngredient> dbai = DB.drugDB().activeIngredients().findBy(ActiveIngredient.COLUMN_ACTIVE_INGREDIENT_ID, pai.getActiveIngredientID());
            if (dbai.size() != 1) {
                Log.e(TAG, "findAllergensForPrescription: wrong AI: " + pai);
            } else {
                ret.add(new AllergenVO(dbai.get(0)));
            }
        }

        //excipients
        List<PrescriptionExcipient> pes = DB.drugDB().prescriptionExcipients().findBy(PrescriptionExcipient.COLUMN_PRESCRIPTION_CODE, code);
        for (PrescriptionExcipient pe : pes) {
            List<Excipient> dbe = DB.drugDB().excipients().findBy(Excipient.COLUMN_EXCIPIENT_ID, pe.getExcipientID());
            if (dbe.size() != 1) {
                Log.e(TAG, "findAllergensForPrescription: wrong AI: " + pe);
            } else {
                ret.add(new AllergenVO(dbe.get(0)));
            }
        }


        Log.d(TAG, "getAllergensForPrescription() returned: " + ret);
        return ret;
    }

    /**
     * Checks if the current patient has any allergies to the supplied prescription.
     *
     * @param ctx the context
     * @param p   the prescription
     * @return list of intersections between patient allergies and prescription allergens.
     */
    public static List<AllergenVO> checkAllergies(Context ctx, Prescription p) {
        List<AllergenVO> prescriptionAllergens = AllergenFacade.findAllergensForPrescription(p);
        List<AllergenVO> patientAllergies = AllergenConversionUtil.toVO(DB.patientAllergens().findAllForActivePatient(ctx));

        prescriptionAllergens.retainAll(patientAllergies);

        return prescriptionAllergens;
    }

}
