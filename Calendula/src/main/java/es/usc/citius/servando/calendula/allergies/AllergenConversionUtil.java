package es.usc.citius.servando.calendula.allergies;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.persistence.PatientAllergen;

/**
 * Created by alvaro.brey.vilas on 15/11/16.
 */

public class AllergenConversionUtil {

    public static List<AllergenVO> toVO(List<PatientAllergen> allergens) {
        List<AllergenVO> allergenVOs = new ArrayList<>();
        for (PatientAllergen allergen : allergens) {
            allergenVOs.add(new AllergenVO(allergen));
        }
        return allergenVOs;
    }
}
