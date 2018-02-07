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

import org.joda.time.LocalDate;

import java.util.List;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.events.StockRunningOutEvent;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.PatientAlert;
import es.usc.citius.servando.calendula.persistence.alerts.StockRunningOutAlert;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;
import es.usc.citius.servando.calendula.util.medicine.StockUtils;

/**
 * Created by joseangel.pineiro on 1/23/18.
 */

public class StockAlertHandler {

    public static void checkStockAlerts(Medicine m) {
        if (m.stockManagementEnabled()) {
            Long days = StockUtils.getEstimatedStockDays(m);
            int stock_alert_days = Integer.parseInt(PreferenceUtils.getString(PreferenceKeys.SETTINGS_STOCK_ALERT_DAYS, "-1"));
            List<PatientAlert> alerts = DB.alerts().findByMedicineAndType(m, StockRunningOutAlert.class.getCanonicalName());

            if (days != null && days < stock_alert_days) {
                if (alerts.isEmpty()) {
                    AlertManager.createAlert(new StockRunningOutAlert(m, LocalDate.now()));
                    CalendulaApp.eventBus().post(new StockRunningOutEvent(m, days));
                }
            } else if (days == null || days > stock_alert_days) {
                for (PatientAlert a : alerts) {
                    DB.alerts().remove(a);
                }
            }
        }
    }
}
