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

package es.usc.citius.servando.calendula.database.migrationHelpers;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import es.usc.citius.servando.calendula.drugdb.model.persistence.ActiveIngredient;
import es.usc.citius.servando.calendula.drugdb.model.persistence.ContentUnit;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Excipient;
import es.usc.citius.servando.calendula.drugdb.model.persistence.HomogeneousGroup;
import es.usc.citius.servando.calendula.drugdb.model.persistence.PackageType;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;
import es.usc.citius.servando.calendula.drugdb.model.persistence.PrescriptionActiveIngredient;
import es.usc.citius.servando.calendula.drugdb.model.persistence.PrescriptionExcipient;
import es.usc.citius.servando.calendula.drugdb.model.persistence.PresentationForm;

/**
 * Helper that performs the migration to the new drug model
 */
public class DrugModelMigrationHelper {

    private static final String TAG = "DrugModelMigration";

    public static void migrateDrugModel(SQLiteDatabase db, ConnectionSource connectionSource) throws SQLException {

        // drug model classes that are persisted to db
        Class<?>[] drugDbClasses = new Class<?>[]{
                ActiveIngredient.class,
                ContentUnit.class,
                Excipient.class,
                HomogeneousGroup.class,
                PackageType.class,
                Prescription.class,
                PresentationForm.class,
                PrescriptionActiveIngredient.class,
                PrescriptionExcipient.class
        };

        // drop deprecated db tables
        Log.d(TAG, "Dropping deprecated tables...");
        db.execSQL("DROP TABLE IF EXISTS Prescriptions;");
        db.execSQL("DROP TABLE IF EXISTS Groups;");
        // create new db tables
        Log.d(TAG, "Creating new drug model tables...");
        for(Class<?> c : drugDbClasses){
            TableUtils.createTable(connectionSource, c);
        }

    }
}
