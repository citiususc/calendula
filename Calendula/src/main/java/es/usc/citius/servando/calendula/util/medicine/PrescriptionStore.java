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

package es.usc.citius.servando.calendula.util.medicine;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.j256.ormlite.misc.TransactionManager;

import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.services.PopulatePrescriptionDBService;

/**
 * Created by joseangel.pineiro
 */
public class PrescriptionStore {

    private static final String TAG = "PrescriptionStore";
    private static final String MEDS_CSV = "meds.csv";
    private static final String CSV_SPACER = "\\|";

    public static void updatePrescriptionsFromCSV(final Context ctx, final boolean truncateBefore, final int newVersionCode) {

        final AssetManager assetManager = ctx.getAssets();

        try {

            TransactionManager.callInTransaction(DB.helper().getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    // TODO: 14/11/16 fix this
//                    if (truncateBefore && !DB.prescriptions().empty()) {
//                        Log.d(TAG, "Truncating prescriptions database...");
//                        // truncate prescriptions table
//                        DB.prescriptions().executeRaw("DELETE FROM Prescriptions;");
//
//                    }
//
//                    Prescription p = null;
//
//                    InputStream csvStream = assetManager.open(MEDS_CSV);
//                    BufferedReader br = new BufferedReader(new InputStreamReader(csvStream));
//                    // step first line (headers)
//                    br.readLine();
//                    // read prescriptions and save them
//                    String line;
//                    int i = 0;
//                    while ((line = br.readLine()) != null) {
//                        if (i % 1000 == 0) {
//                            Log.d(TAG, " Reading line " + i + "...");
//                        }
//                        i++;
//                        p = Prescription.fromCsv(line, CSV_SPACER);
//                        DB.prescriptions().save(p);
//                        // cn | id | name | dose | units | content
//                        //DB.prescriptions().executeRaw("INSERT INTO Prescriptions (Cn, Pid, Name, Dose, Packaging, Content, Generic, Prospect, Affectdriving) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);",
//                        //       new String[]{p.cn, p.pid, p.name, p.dose, String.valueOf(p.packagingUnits), p.content, String.valueOf(p.generic), String.valueOf(p.hasProspect), String.valueOf(p.affectsDriving)});
//                    }
//                    br.close();
//                    // update preferences version
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
                    prefs.edit().putInt(PopulatePrescriptionDBService.DB_VERSION_KEY, newVersionCode).apply();
                    return null;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error while saving prescription data", e);
        }

        // clear all allocated spaces
        Log.d(TAG, "Finish saving " + DB.drugDB().prescriptions().count() + " prescriptions!");

        try {
            DB.drugDB().prescriptions().executeRaw("VACUUM;");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
