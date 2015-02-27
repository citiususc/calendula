package es.usc.citius.servando.calendula.util.medicine;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import java.util.List;

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

    public static Prescription fromCsv(String csvLine, String separator) {
        String[] values = csvLine.split(separator);

        //Log.d("Prescription", values.length + "members: " + Arrays.toString(values));

        if (values.length != 6) {
            throw new RuntimeException("Invalid CSV. Input string must contain exactly 6 members. " + csvLine);
        }

        Prescription p = new Prescription();
        p.cn = values[0];
        p.pid = values[1];
        p.name = values[2];
        p.dose = values[3];
        p.content = values[5];

        try {
            p.packagingUnits = Float.valueOf(values[4].replaceAll(",", "."));
        } catch (Exception e) {
            Log.e("Prescription.class", "Unable to parse med " + p.pid + " packagingUnits", e);
            p.packagingUnits = -1f;
        }
        return p;
    }


    public static int count() {
        return new Select().from(Prescription.class).count();
    }

    public static boolean empty() {
        return count() <= 0;
    }

    public static List<Prescription> findByName(String name, int limit) {
        return SQLiteUtils.rawQuery(
                Prescription.class,
                "SELECT * FROM Prescriptions WHERE Name LIKE '%" + name + "%' LIMIT 20", new String[]{});
    }

    public String shortName() {
        try {
            String s = name.split(" ")[0].toLowerCase();
            return s.substring(0, 1).toUpperCase() +
                    s.substring(1).toLowerCase();
        } catch (Exception e) {
            return name;
        }
    }
}
