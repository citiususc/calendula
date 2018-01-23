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
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.util.alerts;

import java.util.List;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.PatientAlert;
import es.usc.citius.servando.calendula.persistence.alerts.DrivingCautionAlert;

/**
 * Created by joseangel.pineiro on 1/23/18.
 */

public class DrivingAlertHandler {

    public static void checkDrivingAlerts(Medicine m, Prescription p) {
        if (p != null && p.isAffectsDriving()) {
            final List<PatientAlert> drivingAlerts = DB.alerts().findByMedicineAndType(m, DrivingCautionAlert.class.getCanonicalName());
            if (drivingAlerts == null || drivingAlerts.isEmpty()) {
                AlertManager.createAlert(new DrivingCautionAlert(m));
            }
        }
    }
}
