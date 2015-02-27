package es.usc.citius.servando.calendula.util.medicine;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.util.SQLiteUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by joseangel.pineiro
 */
public class PrescriptionStore {

    private static final String TAG = "PrescriptionStore";
    private static final String MEDS_CSV = "meds.csv";
    private static final String CSV_SPACER = "\\|";

    public static void fillDatabaseFromCsv(Context ctx) {
        AssetManager assetManager = ctx.getAssets();
        try {
            ActiveAndroid.beginTransaction();
            Prescription p = null;
            Log.d(TAG, "FillDatabaseFromCsv. Database is empty, saving data...");
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
                SQLiteUtils.execSql("INSERT INTO Prescriptions (Cn, Pid, Name, Dose, Packaging, Content) VALUES (?, ?, ?, ?, ?, ?);",
                        new Object[]{p.cn, p.pid, p.name, p.dose, p.packagingUnits, p.content});
            }
            br.close();
            ActiveAndroid.setTransactionSuccessful();
            Log.d(TAG, "Finish saving " + Prescription.count() + " prescriptions!");
        } catch (Exception e) {
            Log.e(TAG, "Error while saving prescription data", e);
        } finally {
            if (ActiveAndroid.inTransaction()) {
                ActiveAndroid.endTransaction();
            }
        }
    }
}
