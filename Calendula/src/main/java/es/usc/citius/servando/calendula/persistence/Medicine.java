package es.usc.citius.servando.calendula.persistence;


import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;


/**
 * Created by joseangel.pineiro on 12/5/13.
 */
@Table(name = "Medicines")
public class Medicine extends Model {

    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_PRESENTATION = "Presentation";

    @Column(name = COLUMN_NAME)
    private String name;

    @Column(name = COLUMN_PRESENTATION)
    private Presentation presentation;

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

    public static Medicine findById(long id) {
        return new Select().from(Medicine.class)
                .where("id = ?", id)
                .executeSingle();
    }


}
