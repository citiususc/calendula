package es.usc.citius.servando.calendula.allergies;

import java.util.List;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.PatientAlert;
import es.usc.citius.servando.calendula.persistence.alerts.AllergyPatientAlert;

/**
 * Created by alvaro.brey.vilas on 17/11/16.
 */

public class AllergyAlertUtil {

    /**
     * Removes all allergy alerts for the given medicine.
     *
     * @param m the medicine
     */
    public static void removeAllergyAlerts(Medicine m) {
        List<PatientAlert> alerts = DB.alerts().findBy(PatientAlert.COLUMN_TYPE, PatientAlert.AlertType.ALLERGY_ALERT);
        for (PatientAlert alert : alerts) {
            AllergyPatientAlert.AllergyAlertInfo details = (AllergyPatientAlert.AllergyAlertInfo) alert.getDetails();
            if (details.getMedicine().cn() != null && details.getMedicine().cn().equals(m.cn())
                    && details.getMedicine().patient().id().equals(m.patient().id()))
                DB.alerts().remove(alert);
        }
    }
}
