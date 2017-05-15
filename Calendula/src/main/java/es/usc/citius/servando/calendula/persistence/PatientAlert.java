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

package es.usc.citius.servando.calendula.persistence;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import es.usc.citius.servando.calendula.util.LogUtil;

/**
 * An abstract representation of an alert, that allows a simplified alarm
 * persistence strategy, using the same table for different alert types
 *
 * @param <P> Type of the class that models an specific alert type
 * @param <T> Type that encapsulates de specific alarm details
 */
@SuppressWarnings("unused")
@DatabaseTable(tableName = "PatientAlerts")
public class PatientAlert<P extends PatientAlert<P, T>, T> {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DETAILS = "Details";
    public static final String COLUMN_PATIENT = "Patient";
    public static final String COLUMN_TYPE = "Type";
    public static final String COLUMN_MEDICINE = "Medicine";
    public static final String COLUMN_LEVEL = "Level";
    private static final Gson gson = Converters.registerAll(new GsonBuilder()).create();
    private static final String TAG = "PatientAlert";

    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;
    @DatabaseField(columnName = COLUMN_TYPE)
    private String type;
    @DatabaseField(columnName = COLUMN_PATIENT, foreign = true, foreignAutoRefresh = true)
    private Patient patient;
    @DatabaseField(columnName = COLUMN_MEDICINE, foreign = true, foreignAutoRefresh = true)
    private Medicine medicine;
    @DatabaseField(columnName = COLUMN_LEVEL)
    private int level;
    @DatabaseField(columnName = COLUMN_DETAILS)
    private String jsonDetails;

    public PatientAlert() {
    }

    public Long id() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    protected void setType(String type) {
        this.type = type;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public T getDetails() {
        if (getDetailsType() != null) {
            return (T) gson.fromJson(jsonDetails, getDetailsType());
        }
        return null;
    }

    public void setDetails(T details) {
        this.jsonDetails = gson.toJson(details);
    }

    public String getJsonDetails() {
        return jsonDetails;
    }

    public void setJsonDetails(String jsonDetails) {
        this.jsonDetails = jsonDetails;
    }

    public final boolean hasDetails() {
        return getDetailsType() != null;
    }

    // Methods to be overridden by subclasses
    public Class<?> getDetailsType() {
        throw new RuntimeException("This method must be overridden by subclasses");
    }

    public Class<?> viewProviderType() {
        return null;
    }

    public P map() {
        try {
            P result = (P) Class.forName(getType()).newInstance();
            result.setId(id);
            result.setPatient(patient);
            result.setMedicine(medicine);
            result.setJsonDetails(jsonDetails);
            result.setLevel(level);
            result.setType(type);
            return result;
        } catch (Exception e) {
            LogUtil.e(TAG, "Unable to map alert to an specific alert type", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "PatientAlert{" +
                "id=" + id +
                ", type=" + type +
                ", patient=" + patient +
                ", medicine=" + medicine +
                ", level=" + level +
                ", jsonDetails='" + jsonDetails + '\'' +
                '}';
    }

    public final static class Level {

        public static final int LOW = 1;
        public static final int MEDIUM = 2;
        public static final int HIGH = 3;

        private Level() {
        }
    }
}
