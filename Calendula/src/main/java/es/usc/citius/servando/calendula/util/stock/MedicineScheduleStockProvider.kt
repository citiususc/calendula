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

package es.usc.citius.servando.calendula.util.stock

import es.usc.citius.servando.calendula.database.DB
import es.usc.citius.servando.calendula.persistence.Medicine
import es.usc.citius.servando.calendula.persistence.Schedule
import org.joda.time.LocalDate

class MedicineScheduleStockProvider(val m: Medicine) : StockForDayProvider {

    val schedules: List<Schedule>? by lazy { DB.schedules().findByMedicine(m) }

    override fun stockNeededForDay(date: LocalDate): Float {
        var neededStock = 0F

        schedules
            ?.filter { it.enabledForDate(date) }
            ?.forEach { schedule: Schedule ->
                if (schedule.repeatsHourly()) {
                    val intakes = schedule.hourlyItemsAt(date.toDateTimeAtStartOfDay()).size
                    neededStock += intakes * schedule.dose()
                } else {
                    for (item in schedule.items()) {
                        neededStock += item.dose
                    }
                }
            }

        return neededStock
    }

}