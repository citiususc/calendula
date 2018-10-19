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

package es.usc.citius.servando.calendula.database

import es.usc.citius.servando.calendula.CalendulaApp
import es.usc.citius.servando.calendula.persistence.*
import org.codehaus.plexus.util.FileUtils
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File
import java.time.Clock


@RunWith(RobolectricTestRunner::class)
class DBTest {

    companion object {
        private const val MED_NAME = "TestMed"
        private const val ROUTINE_NAME = "Test"
        private const val FROM_DATE = "2015-01-01"
        private const val TO_DATE = "2015-02-05"
    }

    @Before
    fun setUp() {
        CalendulaApp.disableReceivers = true
        DB.init(RuntimeEnvironment.application)
        DB.dropAndCreateDatabase()
    }

    @Test
    fun testDaoSave() {
        val r = Routine(LocalTime(0, 0), ROUTINE_NAME)
        val m = Medicine(MED_NAME, Presentation.CAPSULES)
        val s = Schedule(m)
        val i = ScheduleItem(s, r)
        val d = DailyScheduleItem(i)
        d.takenToday = true

        val pk = PickupInfo()
        pk.from = LocalDate.parse(FROM_DATE)
        pk.to = LocalDate.parse(TO_DATE)
        pk.medicine = m

        // save some stuff
        DB.routines().save(r)
        DB.medicines().save(m)
        DB.schedules().save(s)
        DB.scheduleItems().save(i)
        DB.dailyScheduleItems().save(d)
        DB.pickups().save(pk)

        // verify id property created
        assertNotNull(r.id)
        assertNotNull(m.id)
        assertNotNull(s.id)
        assertNotNull(i.id)
        assertNotNull(d.id)
        assertNotNull(pk.id)

        assertEquals(DB.pickups().findByMedicine(m)[0].from, LocalDate.parse(FROM_DATE))
    }

    @Test
    fun testDaoFind() {

        testDaoSave()
        assertEquals(
            DB.routines().findOneBy(Routine.COLUMN_NAME, ROUTINE_NAME).time,
            LocalTime(0, 0)
        )
        assertEquals(DB.medicines().findAll()[0].name, MED_NAME)
        assertEquals(DB.schedules().findAll()[0].items().size.toLong(), 1)
    }

    /**
     * Abuse test framework to export database into a project folder, allowing further analysis of database schema
     */
    @Test
    fun exportDatabase() {
        val db = DB.helper().writableDatabase;
        DB.helper().close();
        val destination = File("../generated/db")
        destination.mkdirs()
        System.out.println(String.format("Exporting application database %s to %s", db.path, destination.absolutePath))
        FileUtils.copyFileToDirectory(File(db.path), destination)
        assertNotNull(db.path);
    }
}
