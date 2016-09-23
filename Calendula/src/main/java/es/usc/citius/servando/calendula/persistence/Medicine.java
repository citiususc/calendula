package es.usc.citius.servando.calendula.persistence;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import es.usc.citius.servando.calendula.database.DB;

import static java.util.Collections.sort;


/**
 * Created by joseangel.pineiro
 */
@DatabaseTable(tableName = "Medicines")
public class Medicine implements Comparable<Medicine> {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_PRESENTATION = "Presentation";
    public static final String COLUMN_CN = "cn";
    public static final String COLUMN_HG = "hg";
    public static final String COLUMN_PATIENT = "Patient";

    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    @DatabaseField(columnName = COLUMN_NAME)
    private String name;

    @DatabaseField(columnName = COLUMN_PRESENTATION)
    private Presentation presentation;

    @DatabaseField(columnName = COLUMN_CN)
    private String cn;

    @DatabaseField(columnName = COLUMN_HG)
    private Long homogeneousGroup;

    @DatabaseField(columnName = COLUMN_PATIENT, foreign = true, foreignAutoRefresh = true)
    private Patient patient;

    public Medicine() {
    }

    public Medicine(String name) {
        this.name = name;
    }

    public Medicine(String name, Presentation presentation) {
        this.name = name;
        this.presentation = presentation;
    }

    public String cn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public Long homogeneousGroup() {
        return homogeneousGroup;
    }

    public void setHomogeneousGroup(Long homogeneousGroup) {
        this.homogeneousGroup = homogeneousGroup;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Presentation presentation() {
        return presentation;
    }

    public void setPresentation(Presentation presentation) {
        this.presentation = presentation;
    }

    public Collection<PickupInfo> pickups() {
        return DB.pickups().findByMedicine(this);
    }

    public Patient patient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    @Override
    public int compareTo(Medicine another) {
        return name.compareTo(another.name);
    }

    // *************************************
    // DB queries
    // *************************************

    public static List<Medicine> findAll() {
        return DB.medicines().findAll();
    }

    public static Medicine findById(long id) {
        return DB.medicines().findById(id);
    }

    public static Medicine findByName(String name) {
        return DB.medicines().findOneBy(COLUMN_NAME, name);
    }


    public void deleteCascade() {
        DB.medicines().deleteCascade(this, false);
    }

    public void save() {
        DB.medicines().save(this);
    }

    public static Medicine fromPrescription(Prescription p) {
        Medicine m = new Medicine();
        m.setCn(p.cn);
        m.setName(p.shortName());
        Presentation pre = p.expectedPresentation();
        m.setPresentation(pre != null ? pre : Presentation.PILLS);
        return m;
    }

    public LocalDate nextPickupDate() {

        List<PickupInfo> pickupList = new ArrayList<>();
        for (PickupInfo pickupInfo : pickups()) {
            if (!pickupInfo.taken())
                pickupList.add(pickupInfo);
        }

        if (!pickupList.isEmpty()) {
            sort(pickupList, new PickupInfo.PickupComparator());
            return pickupList.get(0).from();
        }

        return null;
    }

    public String nextPickup() {
        LocalDate np = nextPickupDate();
        return np != null ? np.toString("dd MMMM") : null;
    }
}
