package es.usc.citius.servando.calendula.persistence;

import com.google.gson.Gson;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "PatientAlerts")
public class PatientAlert<T> {

    public enum AlertType {
        // TODO: 16/11/16 add alert types
    }

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DETAILS = "Details";
    public static final String COLUMN_PATIENT = "Patient";
    public static final String COLUMN_TYPE = "Type";
    public static final String COLUMN_EXTRA_ID = "ExtraID";

    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    @DatabaseField(columnName = COLUMN_TYPE)
    private AlertType type;

    @DatabaseField(columnName = COLUMN_PATIENT, foreign = true, foreignAutoRefresh = true)
    private Patient patient;

    @DatabaseField(columnName = COLUMN_DETAILS)
    private String jsonDetails;

    @DatabaseField(columnName = COLUMN_EXTRA_ID)
    private String extraID;


    public PatientAlert() {
    }

    public PatientAlert(T details, Patient patient, AlertType type) {
        setDetails(details);
        this.patient = patient;
        this.type = type;
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

    public String getExtraID() {
        return extraID;
    }

    public void setExtraID(String extraID) {
        this.extraID = extraID;
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

    public Class<?> getDetailsType() {
        throw new RuntimeException("This method must be overriden by subclasses");
    }

    public final boolean hasDetails() {
        return getDetailsType() != null;
    }


    @Override
    public String toString() {
        return "PatientAlert{" +
                "id=" + id +
                ", type=" + type +
                ", jsonDetails='" + jsonDetails + '\'' +
                '}';
    }
}
