package es.usc.citius.servando.calendula.persistence;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.LocalDate;

import es.usc.citius.servando.calendula.persistence.typeSerializers.LocalDatePersister;


/**
 * Created by joseangel.pineiro on 4/9/15.
 */
@DatabaseTable(tableName = "Pickups")
public class PickupInfo {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FROM = "From";
    public static final String COLUMN_TO = "To";
    public static final String COLUMN_TAKEN = "Taken";
    public static final String COLUMN_MEDICINE = "Medicine";


    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    @DatabaseField(columnName = COLUMN_FROM, persisterClass = LocalDatePersister.class)
    private LocalDate from;

    @DatabaseField(columnName = COLUMN_TO, persisterClass = LocalDatePersister.class)
    private LocalDate to;

    @DatabaseField(columnName = COLUMN_TAKEN)
    private boolean taken;

    @DatabaseField(columnName = COLUMN_MEDICINE, foreign = true, foreignAutoRefresh = true)
    private Medicine medicine;


    public Long id() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate from() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate to() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }

    public boolean taken() {
        return taken;
    }

    public void taken(boolean taken) {
        this.taken = taken;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }


    //
    // DB QUERIES
    //


}
