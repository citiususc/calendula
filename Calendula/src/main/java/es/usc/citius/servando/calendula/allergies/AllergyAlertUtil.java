package es.usc.citius.servando.calendula.allergies;

import android.util.Log;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.PatientAlert;

/**
 * Created by alvaro.brey.vilas on 17/11/16.
 */

public class AllergyAlertUtil {


    private static final String TAG = "AllergyAlertUtil";


    public static List<PatientAlert> getAlertsForMedicine(final Medicine m) throws SQLException {
        Log.d(TAG, "getAlertsForMedicine() called with: m = [" + m + "]");
        // TODO: 18/11/16 check if we need to escape quotes (queryForFieldValuesArgs)
        HashMap<String, Object> query = new HashMap<String, Object>() {{
            put(PatientAlert.COLUMN_TYPE, PatientAlert.AlertType.ALLERGY_ALERT);
            put(PatientAlert.COLUMN_PATIENT, m.patient());
            put(PatientAlert.COLUMN_EXTRA_ID, m.getId());
        }};
        return DB.alerts().queryForFieldValues(query);
    }

    /**
     * Removes all allergy alerts for the given medicine.
     *
     * @param m the medicine
     */
    public static void removeAllergyAlerts(final Medicine m) throws SQLException {

        Log.d(TAG, "removeAllergyAlerts() called with: m = [" + m + "]");
        final List<PatientAlert> alerts = getAlertsForMedicine(m);
        DB.transaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (PatientAlert alert : alerts) {
                    DB.alerts().remove(alert);
                }
                Log.d(TAG, "removeAllergyAlerts: Removed " + alerts.size() + " alerts");
                return null;
            }
        });

    }


    /**
     * Checks if any allergy alerts are present for a given medicine.
     *
     * @param m the medicine
     * @return <code>true</code> if there are any allergy alerts, <code>false</code> otherwise.
     * @throws SQLException
     */
    public static boolean hasAllergyAlerts(final Medicine m) throws SQLException {
        Log.d(TAG, "hasAllergyAlerts() called with: m = [" + m + "]");

        return getAlertsForMedicine(m).size() > 0;
    }
}
