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

package es.usc.citius.servando.calendula.allergies;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.model.persistence.ActiveIngredient;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Excipient;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;
import es.usc.citius.servando.calendula.drugdb.model.persistence.PrescriptionActiveIngredient;
import es.usc.citius.servando.calendula.drugdb.model.persistence.PrescriptionExcipient;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.util.LogUtil;


public class AllergenFacade {

    private static final long ALLERGEN_SEARCH_LIMIT = 60;
    private static final String TAG = "AllergenFacade";


    public static List<AllergenVO> searchForAllergens(final String name) {
        LogUtil.d(TAG, "searchForAllergens() called with: name = [" + name + "]");

        final String pattern = "%" + name + "%";

        List<AllergenVO> ret = new ArrayList<>();
        List<ActiveIngredient> activeIngredients = DB.drugDB().activeIngredients().like(ActiveIngredient.COLUMN_NAME, pattern, ALLERGEN_SEARCH_LIMIT);
        LogUtil.v(TAG, "Received " + activeIngredients.size() + " active ingredients");
        for (ActiveIngredient activeIngredient : activeIngredients) {
            ret.add(new AllergenVO(activeIngredient));
        }
        List<Excipient> excipients = DB.drugDB().excipients().like(ActiveIngredient.COLUMN_NAME, pattern, ALLERGEN_SEARCH_LIMIT - activeIngredients.size());
        LogUtil.v(TAG, "Received " + excipients.size() + " excipients");
        for (Excipient excipient : excipients) {
            ret.add(new AllergenVO(excipient));
        }

        LogUtil.d(TAG, "searchForAllergens() returned: " + ret);
        return ret;
    }

    public static List<AllergenVO> findAllergensForPrescription(final Prescription p) {
        String code = p.getCode();
        return findAllergensForPrescription(code);
    }

    public static List<AllergenVO> findAllergensForPrescription(final String code) {
        LogUtil.d(TAG, "getAllergensForPrescription() called with: p = [" + code + "]");
        List<AllergenVO> ret = new ArrayList<>();
        // active ingredients
        List<PrescriptionActiveIngredient> pais = DB.drugDB().prescriptionActiveIngredients().findBy(PrescriptionActiveIngredient.COLUMN_PRESCRIPTION_CODE, code);
        for (PrescriptionActiveIngredient pai : pais) {
            List<ActiveIngredient> dbai = DB.drugDB().activeIngredients().findBy(ActiveIngredient.COLUMN_ACTIVE_INGREDIENT_CODE, pai.getActiveIngredientID());
            if (dbai.size() != 1) {
                LogUtil.e(TAG, "findAllergensForPrescription: wrong AI: " + pai);
            } else {
                ret.add(new AllergenVO(dbai.get(0)));
            }
        }

        //excipients
        List<PrescriptionExcipient> pes = DB.drugDB().prescriptionExcipients().findBy(PrescriptionExcipient.COLUMN_PRESCRIPTION_CODE, code);
        for (PrescriptionExcipient pe : pes) {
            List<Excipient> dbe = DB.drugDB().excipients().findBy(Excipient.COLUMN_EXCIPIENT_ID, pe.getExcipientID());
            if (dbe.size() != 1) {
                LogUtil.e(TAG, "findAllergensForPrescription: wrong AI: " + pe);
            } else {
                ret.add(new AllergenVO(dbe.get(0)));
            }
        }


        LogUtil.d(TAG, "getAllergensForPrescription() returned: " + ret);
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

    /**
     * Returns a list of medicines for the current patient which contain the given allergen.
     *
     * @param ctx         the context
     * @param newAllergen the allergen
     * @return the medicines
     */
    public static List<Medicine> checkNewMedicineAllergies(Context ctx, AllergenVO newAllergen) {
        LogUtil.d(TAG, "checkNewMedicineAllergies() called with: ctx = [" + ctx + "], newAllergen = [" + newAllergen + "]");

        List<Medicine> medicines = new ArrayList<>();

        List<Medicine> patientMedicines = DB.medicines().findAllForActivePatient(ctx);
        for (Medicine m : patientMedicines) {
            if (m.isBoundToPrescription()) {
                List<AllergenVO> prescriptionAllergens = findAllergensForPrescription(m.getCn());
                if (prescriptionAllergens.contains(newAllergen))
                    medicines.add(m);
            }
        }

        LogUtil.d(TAG, "checkNewMedicineAllergies() returned: " + medicines);
        return medicines;
    }

}
