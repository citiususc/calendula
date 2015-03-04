package es.usc.citius.servando.calendula.util.medicine;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.util.SQLiteUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import es.usc.citius.servando.calendula.services.PopulatePrescriptionDBService;

/**
 * Created by joseangel.pineiro
 */
public class PrescriptionStore {

    private static final String TAG = "PrescriptionStore";
    private static final String MEDS_CSV = "meds.csv";
    private static final String CSV_SPACER = "\\|";

    public static void updatePrescriptionsFromCSV(Context ctx, boolean truncateBefore, int newVersionCode) {
        AssetManager assetManager = ctx.getAssets();
        try {
            ActiveAndroid.beginTransaction();

            if (truncateBefore && !Prescription.empty()) {
                Log.d(TAG, "Truncating prescriptions database...");
                // truncate prescriptions table
                SQLiteUtils.execSql("DELETE FROM Prescriptions;");

            }

            Prescription p = null;

            InputStream csvStream = assetManager.open(MEDS_CSV);
            BufferedReader br = new BufferedReader(new InputStreamReader(csvStream));
            // step first line (headers)
            br.readLine();
            // read prescriptions and save them
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                if (i % 1000 == 0) {
                    Log.d(TAG, " Reading line " + i + "...");
                }
                i++;
                p = Prescription.fromCsv(line, CSV_SPACER);
                // cn | id | name | dose | units | content
                SQLiteUtils.execSql("INSERT INTO Prescriptions (Cn, Pid, Name, Dose, Packaging, Content, Generic, Prospect, Affectdriving) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);",
                        new Object[]{p.cn, p.pid, p.name, p.dose, p.packagingUnits, p.content, p.generic, p.hasProspect, p.affectsDriving});
            }
            br.close();

            // update preferences version
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            prefs.edit().putInt(PopulatePrescriptionDBService.DB_VERSION_KEY, newVersionCode).commit();

            ActiveAndroid.setTransactionSuccessful();
            // clear all allocated spaces            
            Log.d(TAG, "Finish saving " + Prescription.count() + " prescriptions!");
        } catch (Exception e) {
            Log.e(TAG, "Error while saving prescription data", e);
        } finally {
            if (ActiveAndroid.inTransaction()) {
                ActiveAndroid.endTransaction();
            }
        }
        SQLiteUtils.execSql("VACUUM;");
    }
}
