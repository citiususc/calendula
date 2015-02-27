package es.usc.citius.servando.calendula.persistence;


import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;


/**
 * Created by joseangel.pineiro on 12/5/13.
 */
@Table(name = "Medicines", id = Medicine.COLUMN_ID)
public class Medicine extends Model implements Comparable<Medicine> {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_PRESENTATION = "Presentation";
    public static final String COLUMN_CN = "cn";

    @Column(name = COLUMN_NAME)
    private String name;

    @Column(name = COLUMN_PRESENTATION)
    private Presentation presentation;

    @Column(name = COLUMN_CN)
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
        return new Select().from(Medicine.class)
                .orderBy(COLUMN_NAME + " DESC")
                .execute();
    }

    public static String[] findAllMedicineNames() {
        List<Medicine> ms = findAll();
        String[] names = new String[ms.size()];
        for (int i = 0; i < ms.size(); i++) {
            names[i] = ms.get(i).name();
        }
        return names;
    }


    public static Medicine findById(long id) {
        return new Select().from(Medicine.class)
                .where(COLUMN_ID + " = ?", id)
                .executeSingle();
    }


    public static Medicine findByName(String name) {
        return new Select().from(Medicine.class)
                .where(COLUMN_NAME + " = ?", name)
                .executeSingle();
    }

    public void deleteCascade() {
        List<Schedule> schedules = Schedule.findByMedicine(this);
        for (Schedule s : schedules) {
            s.deleteCascade();
        }
        this.delete();
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
}
