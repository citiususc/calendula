package es.usc.citius.servando.calendula.allergies;

import android.util.Log;

import com.j256.ormlite.stmt.PreparedQuery;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.PatientAlert;
import es.usc.citius.servando.calendula.persistence.PatientAlert.AlertType;
import es.usc.citius.servando.calendula.persistence.PatientAllergen;
import es.usc.citius.servando.calendula.persistence.alerts.AllergyPatientAlert;
import es.usc.citius.servando.calendula.util.alerts.AlertManager;

/**
 * Created by alvaro.brey.vilas on 17/11/16.
 */

public class AllergyAlertUtil {


    private static final String TAG = "AllergyAlertUtil";


    public static List<PatientAlert> getAlertsForMedicine(final Medicine m) throws SQLException {
        Log.d(TAG, "getAlertsForMedicine() called with: m = [" + m + "]");
        // TODO: 18/11/16 check if we need to escape quotes (queryForFieldValuesArgs)
        HashMap<String, Object> query = new HashMap<String, Object>() {{
            put(PatientAlert.COLUMN_TYPE, AlertType.ALLERGY_ALERT);
            put(PatientAlert.COLUMN_PATIENT, m.patient());
            put(PatientAlert.COLUMN_MEDICINE, m);
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

    public static void removeAllergyAlerts(final PatientAllergen allergen) throws SQLException {
        Log.d(TAG, "removeAllergyAlerts() called with: allergen = [" + allergen + "]");
        final AllergenVO vo = new AllergenVO(allergen);

        final List<PatientAlert> removed = new ArrayList<>();

        DB.transaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final PreparedQuery<PatientAlert> query = DB.alerts().queryBuilder().where()
                        .eq(PatientAlert.COLUMN_TYPE, AlertType.ALLERGY_ALERT)
                        .and().eq(PatientAlert.COLUMN_PATIENT, allergen.getPatient()).prepare();
                final List<PatientAlert> alerts = DB.alerts().query(query);

                for (PatientAlert alert : alerts) {
                    AllergyPatientAlert a = new AllergyPatientAlert(alert);
                    final AllergyPatientAlert.AllergyAlertInfo details = a.getDetails();
                    if (details.getAllergens().contains(vo)) {
                        details.getAllergens().remove(vo);
                        if (details.getAllergens().isEmpty()) {
                            AlertManager.removeAlert(alert);
                            removed.add(a);
                        } else {
                            a.setDetails(details);
                            DB.alerts().save(a);
                        }
                    }
                }
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
