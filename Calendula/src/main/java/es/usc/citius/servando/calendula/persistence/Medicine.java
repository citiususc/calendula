package es.usc.citius.servando.calendula.persistence;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.List;

import es.usc.citius.servando.calendula.database.DB;


/**
 * Created by joseangel.pineiro on 12/5/13.
 */
@DatabaseTable(tableName = "Medicines")
public class Medicine implements Comparable<Medicine> {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_PRESENTATION = "Presentation";
    public static final String COLUMN_CN = "cn";

    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    @DatabaseField(columnName = COLUMN_NAME)
    private String name;

    @DatabaseField(columnName = COLUMN_PRESENTATION)
    private Presentation presentation;

    @DatabaseField(columnName = COLUMN_CN)
    private String cn;


    public Medicine() {

    }

    public Medicine(String name) {
        this.name = name;
    }

    public Medicine(String name, Presentation presentation) {
        this.name = name;
        this.presentation = presentation;
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


    // Static

    public static List<Medicine> findAll() {
        return DB.Medicines.findAll();
    }

    public static Medicine findById(long id) {
        try {
            return DB.Medicines.queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Medicine findByName(String name) {
        return DB.Medicines.findOneBy(COLUMN_NAME, name);
    }

    public void deleteCascade() {
        List<Schedule> schedules = Schedule.findByMedicine(this);
        for (Schedule s : schedules) {
            s.deleteCascade();
        }
        DB.Medicines.remove(this);
    }

    public Long save() {
        DB.Medicines.save(this);
        return this.id;
    }


    @Override
    public int compareTo(Medicine another) {
        return name.compareTo(another.name);
    }

    public String cn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
