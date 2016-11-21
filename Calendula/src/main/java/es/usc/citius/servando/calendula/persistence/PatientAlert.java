package es.usc.citius.servando.calendula.persistence;

import com.google.gson.Gson;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@SuppressWarnings("unused")
@DatabaseTable(tableName = "PatientAlerts")
public class PatientAlert<T> {


    public enum AlertType {
        STOCK_ALERT
    }

    public final static class Level {

        private Level() {
        }

        public static final int LOW = 1;
        public static final int MEDIUM = 2;
        public static final int HIGH = 3;
    }

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DETAILS = "Details";
    public static final String COLUMN_PATIENT = "Patient";
    public static final String COLUMN_TYPE = "Type";
    public static final String COLUMN_MEDICINE = "Medicine";
    public static final String COLUMN_LEVEL = "Level";

    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    @DatabaseField(columnName = COLUMN_TYPE)
    private AlertType type;

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


    public AlertType getType() {
        return type;
    }

    protected void setType(AlertType type) {
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
            return (T) new Gson().fromJson(jsonDetails, getDetailsType());
        }
        return null;
    }

    public void setDetails(T details) {
        this.jsonDetails = new Gson().toJson(details);
    }

    public String getJsonDetails() {
        return jsonDetails;
    }

    public void setJsonDetails(String jsonDetails) {
        this.jsonDetails = jsonDetails;
    }

    public Class<?> getDetailsType() {
        throw new RuntimeException("This method must be overridden by subclasses");
    }

    public final boolean hasDetails() {
        return getDetailsType() != null;
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
}
