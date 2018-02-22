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

package es.usc.citius.servando.calendula.util.alerts

import es.usc.citius.servando.calendula.CalendulaApp
import es.usc.citius.servando.calendula.database.DB
import es.usc.citius.servando.calendula.events.StockRunningOutEvent
import es.usc.citius.servando.calendula.persistence.Medicine
import es.usc.citius.servando.calendula.persistence.alerts.StockRunningOutAlert
import es.usc.citius.servando.calendula.util.PreferenceKeys
import es.usc.citius.servando.calendula.util.PreferenceUtils
import es.usc.citius.servando.calendula.util.stock.MedicineScheduleStockProvider
import es.usc.citius.servando.calendula.util.stock.StockCalculator
import org.joda.time.LocalDate


object StockAlertHandler {

    @JvmStatic
    fun checkStockAlerts(m: Medicine) {
        if (m.stockManagementEnabled()) {

            val stockEnd = StockCalculator.calculateStockEnd(
                LocalDate.now(),
                MedicineScheduleStockProvider(m),
                m.stock!!
            )

            val stockAlertPref = Integer.parseInt(
                PreferenceUtils.getString(
                    PreferenceKeys.SETTINGS_STOCK_ALERT_DAYS,
                    "-1"
                )
            )
            val alerts =
                DB.alerts().findByMedicineAndType(m, StockRunningOutAlert::class.java.canonicalName)

            if (stockEnd is StockCalculator.StockEnd.OnDate && stockEnd.days < stockAlertPref && alerts.isEmpty()) {
                // if there are no alerts but the stock is under the preference, create them
                AlertManager.createAlert(StockRunningOutAlert(m, LocalDate.now()))
                CalendulaApp.eventBus().post(StockRunningOutEvent(m, stockEnd.days))
            } else if (alerts.isNotEmpty() && ((stockEnd is StockCalculator.StockEnd.OnDate && stockEnd.days >= stockAlertPref) || stockEnd == StockCalculator.StockEnd.OverMax)) {
                // if there are alerts but the stock is over the pref (or over the max) remove them
                for (a in alerts) {
                    AlertManager.removeAlert(a)
                }
            }
        }

    }
}

