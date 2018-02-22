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

import org.joda.time.Duration
import org.joda.time.LocalDate

object StockCalculator {

    const val MAX_STOCK_DURATION_MONTHS = 3


    /**
     * Calculates how long the stock in [currentStock] will last, starting from [startDate]
     * and with the stock consumption given by [stockProvider], up to a maximum of [MAX_STOCK_DURATION_MONTHS]
     *
     * @param startDate the start date for the calculation
     * @param stockProvider a [StockForDayProvider] that specifies how much stock would be consumed for a given date
     * @param currentStock the base quantity of stock
     * @return a [StockEnd.OnDate] when the result is calculable, a [StockEnd.OverMax] if it's ouside the simulation limits
     */
    @JvmStatic
    fun calculateStockEnd(
        startDate: LocalDate,
        stockProvider: StockForDayProvider,
        currentStock: Float
    ): StockEnd {
        val endDate = startDate.plusMonths(MAX_STOCK_DURATION_MONTHS)

        var virtualDate = LocalDate(startDate)
        var virtualStock = currentStock

        // simulate daily intakes until the stock runs out (or we reach the max duration)
        do {
            val stockForDay = stockProvider.stockNeededForDay(virtualDate)
            virtualStock -= stockForDay

            if (virtualStock < 0) {
                return StockEnd.OnDate(
                    date = virtualDate,
                    days = Duration(
                        startDate.toDateTimeAtStartOfDay(),
                        virtualDate.toDateTimeAtStartOfDay()
                    ).standardDays
                )
            }

            virtualDate = virtualDate.plusDays(1)
        } while (virtualDate.isBefore(endDate))

        return StockEnd.OverMax
    }

    /**
     * Result of stock duration estimates
     */
    sealed class StockEnd {
        /**
         * The estimation overstepped the maximum simulation time
         */
        object OverMax : StockEnd()

        /**
         * The stock ends on `date`
         * @param date the stock end date
         * @param days the duration of the stock in days
         */
        class OnDate(val date: LocalDate, val days: Long) : StockEnd()
    }
}