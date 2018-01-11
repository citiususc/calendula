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

package es.usc.citius.servando.calendula.database.migrationHelpers

import android.database.sqlite.SQLiteDatabase
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import es.usc.citius.servando.calendula.database.DB
import es.usc.citius.servando.calendula.drugdb.DBRegistry
import es.usc.citius.servando.calendula.drugdb.model.persistence.*
import es.usc.citius.servando.calendula.util.LogUtil
import es.usc.citius.servando.calendula.util.PreferenceKeys
import es.usc.citius.servando.calendula.util.PreferenceUtils
import java.sql.SQLException

/**
 * Helper that performs the migration to the new drug model
 */
object DrugModelMigrationHelper {

    private val TAG = "DrugModelMigration"

    @Throws(SQLException::class)
    @JvmStatic
    fun migrateDrugModel(db: SQLiteDatabase, connectionSource: ConnectionSource) {

        // drug model classes that are persisted to db
        val drugDbClasses = arrayOf(
                ActiveIngredient::class.java,
                ContentUnit::class.java,
                Excipient::class.java,
                HomogeneousGroup::class.java,
                PackageType::class.java,
                Prescription::class.java,
                PresentationForm::class.java,
                PrescriptionActiveIngredient::class.java,
                PrescriptionExcipient::class.java)

        // drop deprecated db tables
        LogUtil.d(TAG, "Dropping deprecated tables...")
        db.execSQL("DROP TABLE IF EXISTS Prescriptions;")
        db.execSQL("DROP TABLE IF EXISTS Groups;")
        // create new db tables
        LogUtil.d(TAG, "Creating new drug model tables...")
        for (c in drugDbClasses) {
            TableUtils.createTable(connectionSource, c)
        }

    }

    @JvmStatic
    fun linkMedsAfterUpdate() {
        val applicableMeds = DB.medicines().findAll().filter { it.database.isNullOrEmpty() && !it.cn.isNullOrEmpty() }
        val currentDB = PreferenceUtils.getString(PreferenceKeys.DRUGDB_CURRENT_DB, null)

        if (applicableMeds.isNotEmpty()) {
            LogUtil.d(TAG, "linkMedsAfterUpdate: found ${applicableMeds.size} meds that can be re-linked.")
            DB.transaction {
                applicableMeds.forEach { med ->
                    val linkablePrescription = DB.drugDB().prescriptions().findByCn(med.cn)
                    if (linkablePrescription != null) {
                        med.database=currentDB
                        med.name = linkablePrescription.shortName()
                        med.presentation = DBRegistry.instance().current().expectedPresentation(linkablePrescription)
                        DB.medicines().update(med)
                        LogUtil.d(TAG, "linkMedsAfterUpdate: linked med $med to prescription $linkablePrescription")
                    }
                }
            }
        }
    }
}
