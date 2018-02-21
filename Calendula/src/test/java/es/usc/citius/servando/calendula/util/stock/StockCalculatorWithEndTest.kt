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
import es.usc.citius.servando.calendula.util.stock.StockCalculator
import es.usc.citius.servando.calendula.util.stock.StockForDayProvider
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@RunWith(Parameterized::class)
class StockCalculatorWithEndTest(
    private val initialStock: Float,
    private val stockPerDay: Float,
    private val expectedDays: Int
) {

    companion object {
        @Parameters
        @JvmStatic
        fun getParameters() = listOf(
            arrayOf(0F, 1F, 0),
            arrayOf(1F, 1F, 1),
            arrayOf(1F, 1.1F, 0),
            arrayOf(1F, 2F, 0),
            arrayOf(3F, 1.5F, 2),
            arrayOf(3F, 2.5F, 1),
            arrayOf(7F, 1F, 7),
            arrayOf(1000F, 500F, 2)
        )
    }

    private val baseDate = DateTime(2018, 1, 1, 0, 0).toLocalDate()


    @Mock
    private lateinit var stockForDayProvider: StockForDayProvider


    @Before
    fun setUp(){
        println("Testing with initial stock: $initialStock, stock per day=$stockPerDay, expected duration: $expectedDays days")

        MockitoAnnotations.initMocks(this)

        // setup stock provider mock
        Mockito.`when`(stockForDayProvider.stockNeededForDay(kotlinAny())).thenReturn(stockPerDay)
    }

    @Test
    fun testStockDurationWithEnd() {

        val estimatedStockEnd = StockCalculator.calculateStockEnd(
            baseDate,
            stockForDayProvider,
            initialStock
        )

        Assert.assertTrue(
            "Stock end is not of the adequate class. Expected OnDate.",
            estimatedStockEnd is StockCalculator.StockEnd.OnDate
        )
        val stockEnd = estimatedStockEnd as StockCalculator.StockEnd.OnDate
        Assert.assertEquals(
            "Estimated end date isn't right",
            stockEnd.date,
            baseDate.plusDays(expectedDays)
        )
        Assert.assertEquals(
            "Estimated stock duration isn't right",
            stockEnd.days.toInt(),
            expectedDays
        )

    }
}