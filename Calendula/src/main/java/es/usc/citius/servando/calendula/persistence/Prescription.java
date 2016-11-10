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

package es.usc.citius.servando.calendula.persistence;

import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.List;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.util.Strings;

/**
 * Models a prescription:
 * cn | id | name | dose | units | content
 */
@DatabaseTable(tableName = "Prescriptions")
public class Prescription {

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
    @DatabaseField(columnName = COLUMN_PID)
    public String pid;
    @DatabaseField(columnName = COLUMN_CN)
    public String cn;
    @DatabaseField(columnName = COLUMN_NAME)
    public String name;
    @DatabaseField(columnName = COLUMN_DOSE)
    public String dose;
    @DatabaseField(columnName = COLUMN_CONTENT)
    public String content;
    @DatabaseField(columnName = COLUMN_PACK_UNITS)
    public Float packagingUnits;
    @DatabaseField(columnName = COLUMN_GENERIC)
    public boolean generic;
    @DatabaseField(columnName = COLUMN_PROSPECT)
    public boolean hasProspect;
    @DatabaseField(columnName = COLUMN_AFFECT_DRIVING)
    public boolean affectsDriving;
    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    public static Prescription fromCsv(String csvLine, String separator) {
        String[] values = csvLine.split(separator);

        if (values.length != 9) {
            throw new RuntimeException("Invalid CSV. Input string must contain exactly 9 members. " + csvLine);
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
            Log.w("Prescription.class", "Unable to parse med " + p.pid + " packagingUnits");
            p.packagingUnits = -1f;
        }
        return p;
    }

    private static boolean getBoolean(String s) {
        return s.contains("1");
    }

    public static int count() {
        return DB.prescriptions().count();
    }

    public static boolean empty() {
        return count() <= 0;
    }

    public static List<Prescription> findByName(String name, int limit) {
        Log.d("Prescription", "Query by name: " + name);
        return DB.prescriptions().like(COLUMN_NAME, name + "%", Long.valueOf(limit));
    }

    public static Prescription findByCn(String cn) {
        return DB.prescriptions().findOneBy(COLUMN_CN, cn);
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

//    public Presentation expectedPresentation() {
//        return Presentation.expected(name, content);
//    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    @Override
    public String toString() {
        return "Prescription{" +
                "id=" + id +
                ", pid='" + pid + '\'' +
                ", cn='" + cn + '\'' +
                ", name='" + name + '\'' +
                ", dose='" + dose + '\'' +
                ", content='" + content + '\'' +
                ", packagingUnits=" + packagingUnits +
                ", generic=" + generic +
                ", hasProspect=" + hasProspect +
                ", affectsDriving=" + affectsDriving +
                '}';
    }
}
