/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2016 CITIUS - USC
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
 *    along with this software.  If not, see <http://www.gnu.org/licenses>.
 */

package es.usc.citius.servando.calendula.allergies;

import com.j256.ormlite.stmt.PreparedQuery;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.PatientAlert;
import es.usc.citius.servando.calendula.persistence.PatientAllergen;
import es.usc.citius.servando.calendula.persistence.alerts.AllergyPatientAlert;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.alerts.AlertManager;

/**
 * Created by alvaro.brey.vilas on 17/11/16.
 */

public class AllergyAlertUtil {


    private static final String TAG = "AllergyAlertUtil";


    public static List<PatientAlert> getAlertsForMedicine(final Medicine m) throws SQLException {
        LogUtil.d(TAG, "getAlertsForMedicine() called with: m = [" + m + "]");
        HashMap<String, Object> query = new HashMap<String, Object>() {{
            put(PatientAlert.COLUMN_TYPE, AllergyPatientAlert.class.getCanonicalName());
            put(PatientAlert.COLUMN_PATIENT, m.getPatient());
            put(PatientAlert.COLUMN_MEDICINE, m);
        }};
        return DB.alerts().queryForFieldValuesArgs(query);
    }

    /**
     * Removes all allergy alerts for the given medicine.
     *
     * @param m the medicine
     */
    public static void removeAllergyAlerts(final Medicine m) throws SQLException {

        LogUtil.d(TAG, "removeAllergyAlerts() called with: m = [" + m + "]");
        final List<PatientAlert> alerts = getAlertsForMedicine(m);
        DB.transaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (PatientAlert alert : alerts) {
                    DB.alerts().remove(alert);
                }
                LogUtil.d(TAG, "removeAllergyAlerts: Removed " + alerts.size() + " alerts");
                return null;
            }
        });

    }

    public static void removeAllergyAlerts(final PatientAllergen allergen) throws SQLException {
        LogUtil.d(TAG, "removeAllergyAlerts() called with: allergen = [" + allergen + "]");
        final AllergenVO vo = new AllergenVO(allergen);

        final List<PatientAlert> removed = new ArrayList<>();

        DB.transaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final PreparedQuery<PatientAlert> query = DB.alerts().queryBuilder().where()
                        .eq(PatientAlert.COLUMN_TYPE, AllergyPatientAlert.class.getCanonicalName())
                        .and().eq(PatientAlert.COLUMN_PATIENT, allergen.getPatient()).prepare();
                final List<PatientAlert> alerts = DB.alerts().query(query);

                for (PatientAlert alert : alerts) {
                    AllergyPatientAlert a = (AllergyPatientAlert) alert.map();
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
        LogUtil.d(TAG, "hasAllergyAlerts() called with: m = [" + m + "]");

        return getAlertsForMedicine(m).size() > 0;
    }
}
