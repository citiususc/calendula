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

import es.usc.citius.servando.calendula.kotlinAny
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class StockCalculatorWithoutEndTest {

    companion object {
        private const val STOCK_PER_DAY = 1F
        private const val INITIAL_STOCK = 5000F
    }

    @Mock
    lateinit var stockForDayProvider: StockForDayProvider

    private val baseDate = DateTime(2018, 1, 1, 0, 0).toLocalDate()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        Mockito.`when`(stockForDayProvider.stockNeededForDay(kotlinAny())).thenReturn(STOCK_PER_DAY)
    }

    @Test
    fun testStockOverMax() {

        val calculateStockEnd =
            StockCalculator.calculateStockEnd(baseDate, stockForDayProvider,
                INITIAL_STOCK
            )

        Assert.assertTrue(
            "OverMax result expected when calculating stock",
            calculateStockEnd == StockCalculator.StockEnd.OverMax
        )

    }
}