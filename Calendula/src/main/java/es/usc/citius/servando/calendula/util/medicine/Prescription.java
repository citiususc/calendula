package es.usc.citius.servando.calendula.util.medicine;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import java.io.File;
import java.util.List;

import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.util.Strings;

/**
 * Models a prescription:
 * cn | id | name | dose | units | content
 */
@Table(name = "Prescriptions", id = Prescription.COLUMN_ID)
public class Prescription extends Model {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PID = "Pid";
    public static final String COLUMN_CN = "Cn";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_DOSE = "Dose";
    public static final String COLUMN_CONTENT = "Content";
    public static final String COLUMN_PACK_UNITS = "Packaging";
    public static final String COLUMN_GENERIC = "Generic";
    public static final String COLUMN_PROSPECT = "Prospect";
    public static final String COLUMN_AFFECT_DRIVING = "Affectdriving";

    @Column(name = COLUMN_PID)
    public String pid;
    @Column(name = COLUMN_CN)
    public String cn;
    @Column(name = COLUMN_NAME)
    public String name;
    @Column(name = COLUMN_DOSE)
    public String dose;
    @Column(name = COLUMN_CONTENT)
    public String content;
    @Column(name = COLUMN_PACK_UNITS)
    public Float packagingUnits;

    @Column(name = COLUMN_GENERIC)
    public boolean generic;

    @Column(name = COLUMN_PROSPECT)
    public boolean hasProspect;

    @Column(name = COLUMN_AFFECT_DRIVING)
    public boolean affectsDriving;

    public static Prescription fromCsv(String csvLine, String separator) {
        String[] values = csvLine.split(separator);

        //Log.d("Prescription", values.length + "members: " + Arrays.toString(values));

        if (values.length != 9) {
            throw new RuntimeException("Invalid CSV. Input string must contain exactly 6 members. " + csvLine);
        }

        Prescription p = new Prescription();
        p.cn = values[0];
        p.pid = values[1];
        p.name = values[2];
        p.dose = values[3];
        p.content = values[5];
        p.generic = getBoolean(values[6]);
        p.affectsDriving = getBoolean(values[7]);
        p.hasProspect = getBoolean(values[8]);

        try {
            p.packagingUnits = Float.valueOf(values[4].replaceAll(",", "."));
        } catch (Exception e) {
            Log.e("Prescription.class", "Unable to parse med " + p.pid + " packagingUnits", e);
            p.packagingUnits = -1f;
        }
        return p;
    }

    private static boolean getBoolean(String s) {
        if (s.contains("1")) {
            return true;
        }
        return false;
    }


    public static int count() {
        return new Select().from(Prescription.class).count();
    }

    public static boolean empty() {
        return count() <= 0;
    }


    public String shortName() {
        try {
            String[] parts = name.split(" ");
            String s = parts[0].toLowerCase();
            if ((s.contains("acido") || s.contains("ácido")) && parts.length > 1) {
                return Strings.toCamelCase(s + " " + parts[1], " ");
            }
            return Strings.toProperCase(s);
                    
        } catch (Exception e) {
            return name;
        }
    }

    public Presentation expectedPresentation() {
        return Presentation.expected(name, content);
    }

    public static List<Prescription> findByName(String name, int limit) {

        Log.d("Prescription", "Query by name: " + name);

        return SQLiteUtils.rawQuery(
                Prescription.class,
                "SELECT * FROM Prescriptions WHERE Name LIKE '" + name + "%' ORDER BY Name DESC LIMIT " + limit + ";", new String[]{});
    }

    public static Prescription findByCn(String cn) {
        return new Select().from(Prescription.class)
                .where(COLUMN_CN + " LIKE ? ", cn).executeSingle();
    }

    public boolean isProspectDownloaded(Context ctx) {
        File f = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/prospects/" + pid + ".pdf");
        return f.exists();
    }
}
