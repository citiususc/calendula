package es.usc.citius.servando.calendula.util.medicine;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.j256.ormlite.misc.TransactionManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.HomogeneousGroup;
import es.usc.citius.servando.calendula.persistence.Prescription;
import es.usc.citius.servando.calendula.services.PopulatePrescriptionDBService;

/**
 * Created by joseangel.pineiro
 */
public class HomogeneousGroupStore {

    private static final String TAG = "HomogeneousGroupStore";
    private static final String GROUPS_CSV = "groups.csv";
    private static final String CSV_SPACER = "\\|";

    public static void updateGroupsFromCSV(final Context ctx, final boolean truncateBefore, final int newVersionCode) {

        final AssetManager assetManager = ctx.getAssets();

        try {

            TransactionManager.callInTransaction(DB.helper().getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {

                    if (truncateBefore && !HomogeneousGroup.empty()) {
                        Log.d(TAG, "Truncating groups database...");
                        // truncate groups table
                        DB.groups().executeRaw("DELETE FROM Prescriptions;");

                    }

                    HomogeneousGroup g = null;

                    InputStream csvStream = assetManager.open(GROUPS_CSV);
                    BufferedReader br = new BufferedReader(new InputStreamReader(csvStream));
                    // read prescriptions and save them
                    String line;
                    int i = 0;
                    while ((line = br.readLine()) != null) {
                        if (i % 1000 == 0) {
                            Log.d(TAG, " Reading line " + i + "...");
                        }
                        i++;
                        g = HomogeneousGroup.fromCsv(line, CSV_SPACER);
                        DB.groups().save(g);
                        // group, name
                        //DB.groups().executeRaw("INSERT INTO Groups (Group, Name) VALUES (?, ?);",new String[]{g.group, g.name});
                    }
                    br.close();
                    // update preferences version
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
                    prefs.edit().putInt(PopulatePrescriptionDBService.DB_VERSION_KEY, newVersionCode).commit();
                    return null;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error while saving prescription data", e);
        }

        // clear all allocated spaces
        Log.d(TAG, "Finish saving " + Prescription.count() + " prescriptions!");

        try {
            DB.prescriptions().executeRaw("VACUUM;");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
